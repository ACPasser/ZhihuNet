package org.acpasser.zhihunet.crawler.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.dto.user.UserDTO;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.crawler.config.CrawlerConfigProperties;
import org.acpasser.zhihunet.crawler.dto.ZhihuUserInfoParserDTO;
import org.acpasser.zhihunet.crawler.request.ZhihuRequest;
import org.acpasser.zhihunet.crawler.util.BatchUtils;
import org.acpasser.zhihunet.crawler.util.HttpClientUtil;
import org.acpasser.zhihunet.crawler.util.ParseUtil;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserRepository;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;


@Component
@Slf4j
public class UserInfoParser {
    private static final String USER_PROFILE_URL_TEMPLATE = "https://www.zhihu.com/people/%s";
    private static final String ORG_PROFILE_URL_TEMPLATE = "https://www.zhihu.com/org/%s";
    // 全局线程池（避免频繁创建/销毁）
    private ExecutorService crawlerExecutor;
    // 有界缓冲区，避免OOM（存储待入库数据）
    private BlockingQueue<UserDTO> batchBuffer;
    private final Object batchBufferLock = new Object();
    // 计数器（记录已爬取数量）
    private final AtomicInteger crawledCount = new AtomicInteger(0);
    private final ConcurrentHashMap<String, UserDTO> userDTOMap = new ConcurrentHashMap<>();
    // <FailedToken, Reason>
    private final ConcurrentHashMap<String, String> failedTokenMap = new ConcurrentHashMap<>();
    // 重试队列（存储爬取失败的 token）
    // P.S.这个设计没啥用，之所以失败大概率是被反爬了，这时候重试也不可能成功，仅在UserInfoParser中出现。
    private final Queue<String> retryQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private CrawlerConfigProperties config;

    @Autowired
    private ZhihuUserRepository zhihuUserRepository;

    @PostConstruct
    public void init() {
        // 初始化有界缓冲区（容量=批量大小*10）
        batchBuffer = new LinkedBlockingQueue<>(config.getBatchSize() * 10);

        // 初始化全局线程池（核心参数可配置）
        crawlerExecutor = new ThreadPoolExecutor(
            config.getThreadCount(),
            config.getThreadCount() * 2, // 最大线程数=核心数*2
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), // 有界队列，避免OOM
            r -> new Thread(r, "user-crawler-" + Thread.currentThread().threadId()),
            new ThreadPoolExecutor.CallerRunsPolicy() // 任务满时提交者执行，避免丢失
        );
        log.info("UserInfoParser初始化完成，线程池核心数: {}，缓冲区容量: {}",
            config.getThreadCount(), batchBuffer.remainingCapacity());
    }

    /**
     * 销毁：优雅关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (crawlerExecutor != null) {
            crawlerExecutor.shutdown();
            try {
                if (!crawlerExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                    crawlerExecutor.shutdownNow();
                    log.warn("全局线程池强制关闭");
                }
            } catch (InterruptedException e) {
                crawlerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("关闭线程池时被中断", e);
            }
            log.info("全局线程池已关闭");
        }
    }

    /**
     * 爬取单个用户的基本信息，请求需携带：（1）User-Agent；（2）Cookie
     * @param token 待爬用户token
     * @return ZhihuUserDTO
     */
    private UserDTO crawl(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BusinessException("Blank token");
        }

        String userUrl = String.format(USER_PROFILE_URL_TEMPLATE, token);
        ZhihuRequest request = ZhihuRequest.builder().url(userUrl).build();
        Document doc = httpClientUtil.doGetWithRetry(request);
        Elements userInfoElements = doc.select("script[id=js-initialData]");
        if (userInfoElements.isEmpty()) {
            // 机构号重试
            String orgUrl = String.format(ORG_PROFILE_URL_TEMPLATE, token);
            request = ZhihuRequest.builder().url(orgUrl).build();
            doc = httpClientUtil.doGetWithRetry(request); // 机构号URL重试
            userInfoElements = doc.select("script[id=js-initialData]");
            if (userInfoElements.isEmpty()) {
                log.error("用户/机构解析失败：{}，请检查cookie是否过期", token);
                throw new BusinessException("Cookie已过期");
            }
        }
        // 解析JSON数据
        return parseUserInfoFromJson(userInfoElements.getFirst().html(), token);
    }

    private UserDTO parseUserInfoFromJson(String jsonStr, String token) {
        JSONObject userInfoJsonObject = JSON.parseObject(jsonStr);
        JSONObject initialState = userInfoJsonObject.getJSONObject("initialState");
        JSONObject entities = initialState.getJSONObject("entities");
        JSONObject users = entities.getJSONObject("users");
        JSONObject tokenInfos = users.getJSONObject(token);

        if (tokenInfos == null) {
            log.error("用户/机构解析失败: {}，tokenInfos is null", token);
            throw new BusinessException("TokenInfos is null");
        } else if (StringUtils.isNotBlank((String) tokenInfos.get("errorMessage"))) {
            String errorMessage = (String) tokenInfos.get("errorMessage");
            log.error("用户/机构解析失败: {}，errorMessage: {}", token, errorMessage);
            throw new BusinessException(errorMessage);
        }
        // log.info("用户/机构解析成功，token: {}，detail: {}", token, tokenInfos);

        ZhihuUserInfoParserDTO userInfoParserDTO = JSON.parseObject(tokenInfos.toString(), ZhihuUserInfoParserDTO.class);
        UserDTO userInfo = new UserDTO();
        BeanUtils.copyProperties(userInfoParserDTO, userInfo);
        userInfo.setToken(token);
        userInfo.setIndexUrl(String.format(USER_PROFILE_URL_TEMPLATE, token));
        userInfo.setBusiness(userInfoParserDTO.getBusiness().getName());
        userInfo.setLocations(userInfoParserDTO.getLocations().stream()
                .map(ZhihuUserInfoParserDTO.Entity::getName).collect(Collectors.joining(",")));
        userInfo.setEmployments(userInfoParserDTO.getFormattedEmployments());
        userInfo.setEducations(userInfoParserDTO.getFormattedEducations());
        // 个人简介，有的用户放的是链接，需要解析
        userInfo.setDescription(ParseUtil.parseDesc(userInfoParserDTO.getDescription()));
        return userInfo;
    }

    /**
     * 批量爬取知乎用户个人信息
     */
    public BaseResponse<UserCrawlResult> batchCrawl(UserInfoCrawlRequest request) {
        List<String> tokens = request.getTokens().stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tokens)) {
            log.warn("批量爬取失败：token列表为空");
            return BaseResponse.newFailResponse().message("token列表不能为空").build();
        }

        // 清空历史数据
        clearHistoryData();

        ConcurrentHashMap<String, Boolean> pendingTokens = new ConcurrentHashMap<>();
        for (String token : tokens) {
            pendingTokens.put(token, true); // 初始化：所有token标记为待爬取
        }

        CountDownLatch crawlLatch = new CountDownLatch(tokens.size());
        try {
            // 1. 提交所有爬取任务
            for (String token : tokens) {
                crawlerExecutor.submit(() -> {
                    try {
                        // 反爬：避免请求过快
                        TimeUnit.MILLISECONDS.sleep(500);
                        crawlAndBuffer(token);
                        pendingTokens.remove(token);
                    } catch (InterruptedException e) {
                        log.warn("爬取任务被中断，token: {}", token);
                        Thread.currentThread().interrupt();
                        failedTokenMap.put(token, "任务被中断，未完成爬取");
                        pendingTokens.remove(token);
                    } finally {
                        crawlLatch.countDown();
                    }
                });
            }
            // 2. 等待所有爬取任务完成（超时时间可配置）
            if (!crawlLatch.await(config.getCrawlAwaitMinutes(), TimeUnit.MINUTES)) {
                log.warn("批量爬取超时，剩余未完成任务数: {}", crawlLatch.getCount());
                // 超时的任务加入失败名单
                for (String timeoutToken : pendingTokens.keySet()) {
                    failedTokenMap.put(timeoutToken, String.format("批量爬取超时（%d分钟），任务未完成（大概率反爬/网络异常）",
                        config.getCrawlAwaitMinutes()));
                }
            }

            // 3. 处理剩余缓冲区数据
            flushBuffer();

            // 4. 重试失败的任务（可选）
            if (config.getMaxRetries() > 0 && !CollectionUtils.isEmpty(retryQueue)) {
                retryFailedTasks();
            }

            String message = String.format("总任务：%d | 成功：%d | 失败：%d",
                tokens.size(), crawledCount.get(), failedTokenMap.size());
            log.info(message);
            if (!failedTokenMap.isEmpty()) {
                String failedTokenJson = JSON.toJSONString(failedTokenMap, PrettyFormat);
                log.error("失败名单：\n{}", failedTokenJson);
            }
            UserCrawlResult crawlResult = UserCrawlResult.builder()
                .userDTOMap(Collections.unmodifiableMap(new HashMap<>(userDTOMap)))
                .failedTokenMap(Collections.unmodifiableMap(new HashMap<>(failedTokenMap)))
                .build();
            return BaseResponse.newSuccResponse()
                .message(message)
                .result(crawlResult)
                .build();
        } catch (Exception e) {
            log.error("批量爬取失败：{}", tokens, e);
            return BaseResponse.newFailResponse().message(e.getClass().getSimpleName()).build();
        }
    }

    private void clearHistoryData() {
        userDTOMap.clear();
        crawledCount.set(0);
        retryQueue.clear();
        batchBuffer.clear();
        failedTokenMap.clear();
    }

    /**
     * 爬取单个用户并加入缓冲区，达到阈值则批量入库
     */
    private void crawlAndBuffer(String token) {
        try {
            UserDTO zhihuUserDTO = crawl(token);
            userDTOMap.put(token, zhihuUserDTO);
            // 使用offer避免队列满时阻塞
            if (!batchBuffer.offer(zhihuUserDTO)) {
                flushBuffer();
                // 二次尝试入队，检查结果
                boolean offerSuccess = batchBuffer.offer(zhihuUserDTO);
                if (!offerSuccess) {
                    log.error("缓冲区强制入库后仍满，token: {} 兜底单条入库", token);
                    // 兜底：直接单条入库，避免数据丢失
                    BatchUtils.convertAndBatchSave(Collections.singletonList(zhihuUserDTO), ZhihuUser::new,
                        zhihuUserRepository::batchUpsert, "user");
                }
            }
            crawledCount.incrementAndGet();

            if (batchBuffer.size() >= config.getBatchSize()) {
                flushBuffer();
            }
        } catch (BusinessException e) {
            log.error("用户信息爬取失败（业务异常，不重试），token: {}", token, e);
            failedTokenMap.put(token, e.getMessage());
        } catch (Exception e) {
            log.error("用户信息爬取失败（待重试），token: {}", token, e);
            retryQueue.add(token);
        }
    }

    /**
     * 将缓冲区数据批量入库
     */
    private void flushBuffer() {
        List<UserDTO> batchList = new ArrayList<>();
        batchBuffer.drainTo(batchList);
        if (!batchList.isEmpty()) {
            try {
                BatchUtils.convertAndBatchSave(batchList, ZhihuUser::new,
                    zhihuUserRepository::batchUpsert, "user");
            } catch (Exception e) {
                log.error("批量入库失败，重新加入缓冲区", e);
                // 保证批量添加原子性
                synchronized (batchBufferLock) {
                    batchBuffer.addAll(batchList);
                }
            }
        }
    }

    /**
     * 重试失败的任务
     */
    private void retryFailedTasks() {
        int retryCount = 0;
        while (retryCount < config.getMaxRetries() && !retryQueue.isEmpty()) {
            log.info("开始第 {} 次重试，剩余失败任务: {}", retryCount + 1, retryQueue.size());

            List<String> currentRetryList = new ArrayList<>();
            String curr;
            while ((curr = retryQueue.poll()) != null) {
                currentRetryList.add(curr);
            }

            if (currentRetryList.isEmpty()) {
                break;
            }

            // 追踪重试阶段的待处理token
            ConcurrentHashMap<String, Boolean> pendingRetryTokens = new ConcurrentHashMap<>();
            for (String token : currentRetryList) {
                pendingRetryTokens.put(token, true);
            }
            CountDownLatch retryLatch = new CountDownLatch(currentRetryList.size());
            try {
                for (String token : currentRetryList) {
                    crawlerExecutor.submit(() -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                            crawlAndBuffer(token);
                            pendingRetryTokens.remove(token);
                        } catch (InterruptedException e) {
                            log.warn("重试任务被中断，token: {}", token);
                            Thread.currentThread().interrupt();
                            failedTokenMap.put(token, "重试任务被中断（反爬/网络异常）");
                            pendingRetryTokens.remove(token);
                        } finally {
                            retryLatch.countDown();
                        }
                    });
                }

                // 等待重试任务完成
                if (!retryLatch.await(config.getCrawlRetryAwaitMinutes(), TimeUnit.MINUTES)) {
                    log.warn("第 {} 次重试超时，剩余未完成数: {}", retryCount + 1, retryLatch.getCount());
                    for (String timeoutToken : pendingRetryTokens.keySet()) {
                        failedTokenMap.put(timeoutToken, String.format("第%d次重试超时（%d分钟），任务未完成（反爬/网络异常）",
                            retryCount + 1, config.getCrawlRetryAwaitMinutes()));
                    }
                    pendingRetryTokens.clear();
                }

                flushBuffer();
            } catch (Exception e) {
                for (String token : currentRetryList) {
                    failedTokenMap.put(token, e.getClass().getSimpleName());
                }
            }
            retryCount++;
        }

        if (!retryQueue.isEmpty()) {
            log.error("重试次数达上限（{}次），token列表: {}", config.getMaxRetries(), retryQueue);
            // 最终未重试成功的加入失败名单
            for (String token : retryQueue) {
                failedTokenMap.put(token, String.format("重试次数达上限（%d次），仍失败（大概率反爬拦截）",
                    config.getMaxRetries()));
            }
            retryQueue.clear(); // 清空重试队列，避免重复处理
        }
    }
}
