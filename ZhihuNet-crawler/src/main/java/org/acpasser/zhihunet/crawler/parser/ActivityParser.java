package org.acpasser.zhihunet.crawler.parser;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.activity.ActivityDTO;
import org.acpasser.zhihunet.contract.dto.activity.AnswerDTO;
import org.acpasser.zhihunet.contract.dto.activity.ArticleDTO;
import org.acpasser.zhihunet.contract.dto.activity.CollectionDTO;
import org.acpasser.zhihunet.contract.dto.activity.InteractionDTO;
import org.acpasser.zhihunet.contract.dto.activity.PinDTO;
import org.acpasser.zhihunet.contract.dto.activity.QuestionDTO;
import org.acpasser.zhihunet.contract.dto.activity.TopicDTO;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.crawler.config.CrawlerConfigProperties;
import org.acpasser.zhihunet.crawler.dto.ZhihuUserActivityParserDTO;
import org.acpasser.zhihunet.crawler.enums.TargetType;
import org.acpasser.zhihunet.crawler.request.ZhihuRequest;
import org.acpasser.zhihunet.crawler.util.BatchUtils;
import org.acpasser.zhihunet.crawler.util.HttpClientUtil;
import org.acpasser.zhihunet.crawler.util.JsonUtils;
import org.acpasser.zhihunet.crawler.util.ParseUtil;
import org.acpasser.zhihunet.crawler.util.TimeUtil;
import org.acpasser.zhihunet.model.ZhihuAnswer;
import org.acpasser.zhihunet.model.ZhihuArticle;
import org.acpasser.zhihunet.model.ZhihuCollection;
import org.acpasser.zhihunet.model.ZhihuPin;
import org.acpasser.zhihunet.model.ZhihuQuestion;
import org.acpasser.zhihunet.model.ZhihuTopic;
import org.acpasser.zhihunet.model.ZhihuUserInteraction;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuAnswerRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuArticleRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuCollectionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuPinRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuQuestionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuTopicRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserInteractionRepository;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
public class ActivityParser {
    private static final String ACTIVITY_URL_TEMPLATE = "https://www.zhihu.com/api/v3/moments/%s/activities?limit=5&desktop=true&ws_qiangzhisafe=0";
    private static final String ERROR_JSON_CONVERT = "JSON转换为%s失败，原始数据: %s";
    // 全局线程池（避免频繁创建/销毁）
    private ExecutorService crawlerExecutor;
    // 有界缓冲区，避免OOM（存储待入库数据）
    private BlockingQueue<ActivityDTO> batchBuffer;
    private final Object batchBufferLock = new Object();
    // 计数器（记录已爬取数量）
    private final AtomicInteger crawledCount = new AtomicInteger(0);
    private final ConcurrentHashMap<String, ActivityDTO> activityDTOMap = new ConcurrentHashMap<>();
    // <FailedToken, Reason>
    private final ConcurrentHashMap<String, String> failedTokenMap = new ConcurrentHashMap<>();

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private CrawlerConfigProperties config;

    @Autowired
    private ZhihuUserInteractionRepository interactionRepository;
    @Autowired
    private ZhihuQuestionRepository questionRepository;
    @Autowired
    private ZhihuAnswerRepository answerRepository;
    @Autowired
    private ZhihuArticleRepository articleRepository;
    @Autowired
    private ZhihuPinRepository pinRepository;
    @Autowired
    private ZhihuTopicRepository topicRepository;
    @Autowired
    private ZhihuCollectionRepository collectionRepository;

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
            r -> new Thread(r, "activity-crawler-" + Thread.currentThread().threadId()),
            new ThreadPoolExecutor.CallerRunsPolicy() // 任务满时提交者执行，避免丢失
        );
        log.info("ActivityParser初始化完成，线程池核心数: {}，缓冲区容量: {}",
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
     * 爬取某个用户最近的动态
     * @param token 待爬用户token
     * @param limit 限制页数
     * @param seconds 偏移秒数
     * @return  用户动态（包含关联的问题、回答、文章、想法）
     */
    public ActivityDTO crawl(String token, Integer limit, Long seconds) {
        ActivityDTO activityDTO = new ActivityDTO();
        String nextUrl = String.format(ACTIVITY_URL_TEMPLATE, token);
        int pageCnt = 1;

        while (true) {
            ActivityCrawlOnceResult onceResult = crawlOnce(token, nextUrl);
            activityDTO.merge(onceResult.getActivityDTO());

            // STOP
            // 1. 无更多动态
            if (onceResult.getPaging() == null || onceResult.getPaging().getIsEnd()) {
                break;
            } else {
                nextUrl = onceResult.getPaging().getNext();
            }

            // 2. 达到指定页数
            if (limit != null && pageCnt >= limit) {
                break;
            }

            // 3. 超出时间范围（动态按时间倒序，最后一条是最早的）
            if (seconds != null && TimeUtil.isBeforeNow(onceResult.getActivityDTO().getInteractionDTOS().getLast().getInteractionTime(), seconds)) {
                break;
            }
            pageCnt++;
        }
        log.info("成功爬取到用户[{}]最近的{}条动态", token, activityDTO.getInteractionDTOS().size());
        return activityDTO;
    }

    /**
     * 爬取用户单页动态，知乎的接口每页包含7个动态
     * @param token  用户token
     * @param url    动态URL
     * @return 单页结果
     */
    private ActivityCrawlOnceResult crawlOnce(String token, String url) {
        try {
            ZhihuRequest request = buildActivityRequest(url);
            Document doc = httpClientUtil.doGetWithRetry(request);
            String responseBody = doc.body().text();
            Map<String, Object> responseMap = JsonUtils.jsonToMap(responseBody);

            // data
            List<ZhihuUserActivityParserDTO> activityList = parseActivityList(responseMap);

            // paging
            Paging paging = parsePaging(responseMap);

            return convertToCrawOncelResult(activityList, paging);
        } catch (Exception e) {
            log.error("用户动态爬取失败：{}，url: {}", token, url, e);
            throw new BusinessException("爬取用户动态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求对象（封装请求头逻辑）
     */
    private ZhihuRequest buildActivityRequest(String url) {
        // 请求需携带：（1）Cookie；（2）x-zse-93；（3）x-zse-96
        Map<String, String> headers = new HashMap<>(4); // 预设容量，减少扩容
        String zse96 = ParseUtil.parseZse96(url, config.getXZse93(), config.getDC0(), 2);
        if (StringUtils.isBlank(zse96)) {
            throw new BusinessException("x-zse-96解析失败...");
        }
        headers.put("x-zse-96", zse96);
        headers.put("User-Agent", config.getUserAgent());
        headers.put("Cookie", config.getCookie());
        headers.put("x-zse-93", config.getXZse93());
        // x-zst-81 一般不需要，只有被限制之后，登录账号可能出现
        // headers.put("x-zst-81", "3_2.0VhnTj77m-qofgh3TxTP0EiUZQ6nxeX2Zq9O97Qr0EMYxc4f18wNBUgpTQ6nxERFZ1_Y0-4Lm-h3_tufIwJS8gcxTgJS_AuPZNcXCTwxI78YxEM20s4PGDwN8gGcYAupMWufIoLVqr4gxrRPOI0cY7HL8qun9g93mFukyigcmebS_FwOYPRP0E4rZUrN9DDom3hnynAUMnAVPF_PhaueTFROqWqVmougfbwgMFGO8JC3OxGHMG_FfcUULcHpLrLL0nuCCwgo967pKo82TV03f9gSYbCFX-q21M9SCeHNfiDO_qCgObwFM3uVYYGpMhGHLugcBHcH9UGc8OvHLKqeCwDrYTwcXTBemncL1ZupY6vH8SUpf1gLYwGSMZvHOSCL_lCL0Og_zeefKuCxB1R2_V9HfS8xLZwHMQ9C9BDuCb0eLjCCqqqHYyvLZcGcsf9LBDGtfHGSVjJO9e_c_f0H8bCoMqDwfqcnqWDVZW9OCZvxKSMHOdBY1CUxC68ws");
        return ZhihuRequest.builder()
                .url(url)
                .headers(headers)
                .build();
    }

    /**
     * 解析动态列表数据
     */
    private List<ZhihuUserActivityParserDTO> parseActivityList(Map<String, Object> responseMap) {
        if (CollectionUtils.isEmpty(responseMap)) {
            return new ArrayList<>();
        }
        String dataJson = JsonUtils.toJson(responseMap.get("data"));
        List<ZhihuUserActivityParserDTO> activityList = JsonUtils.fromJson(dataJson, new TypeReference<>() {});
        if (activityList == null) {
            throw new BusinessException(String.format(ERROR_JSON_CONVERT, "List<ZhihuUserActivityParserDTO>", dataJson));
        }
        return activityList;
    }

    /**
     * 解析分页信息
     */
    private Paging parsePaging(Map<String, Object> responseMap) {
        if (CollectionUtils.isEmpty(responseMap)) {
            return null;
        }
        String pagingJson = JsonUtils.toJson(responseMap.get("paging"));
        Paging paging = JsonUtils.fromJson(pagingJson, new TypeReference<>() {});
        if (paging == null) {
            throw new BusinessException(String.format(ERROR_JSON_CONVERT, "Paging", pagingJson));
        }
        return paging;
    }

    private ActivityCrawlOnceResult convertToCrawOncelResult(List<ZhihuUserActivityParserDTO> activityList, Paging paging) {
        ActivityDTO activityDTO = new ActivityDTO();
        List<InteractionDTO> interactions = activityDTO.getInteractionDTOS();
        List<QuestionDTO> questions = activityDTO.getQuestionDTOS();
        List<AnswerDTO> answers = activityDTO.getAnswerDTOS();
        List<ArticleDTO> articles = activityDTO.getArticleDTOS();
        List<PinDTO> pins = activityDTO.getPinDTOS();
        List<TopicDTO> topics = activityDTO.getTopicDTOS();
        List<CollectionDTO> collections = activityDTO.getCollectionDTOS();

        for (ZhihuUserActivityParserDTO activity : activityList) {
            interactions.add(activity.getInteractionDTO());
            TargetType targetType = activity.getTarget().getType();
            switch (targetType) {
                case ANSWER -> {
                    answers.add(activity.getAnswerDTO());
                    questions.add(activity.getQuestionDTO());
                }
                case QUESTION -> questions.add(activity.getQuestionDTO());
                case ARTICLE -> articles.add(activity.getArticleDTO());
                case PIN -> {
                    pins.add(activity.getPinDTO());
                    topics.addAll(activity.getTopicDTOS());
                }
                case COLLECTION -> collections.add(activity.getCollectionDTO());
                default -> log.warn("忽略未知的目标类型: {}", targetType);
            }
        }

        return ActivityCrawlOnceResult.builder()
                .activityDTO(activityDTO)
                .paging(paging)
                .build();
    }

    private List<String> validateAndGetTokens(ActivityCrawlRequest request) {
        List<String> tokens = request.getTokens().stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tokens)) {
            throw new BusinessException("token列表不能为空");
        }

        if (request.getLimit() == null && request.getDuration() == null) {
            throw new BusinessException("请求需携带页数或时间范围");
        }
        if (request.getLimit() != null && request.getLimit() < 1) {
            throw new BusinessException("页数需为大于等于1的整数");
        }
        if (request.getDuration() != null) {
            TimeUtil.validateTimeParameters(request.getDuration(), request.getTimeUnit());
        }
        return tokens;
    }

    private void clearHistoryData() {
        activityDTOMap.clear();
        crawledCount.set(0);
        batchBuffer.clear();
        failedTokenMap.clear();
    }

    /**
     * 批量爬取用户最近的动态
     */
    public BaseResponse<ActivityCrawlResult> batchCrawl(ActivityCrawlRequest request) {
        List<String> tokens = validateAndGetTokens(request);
        Long seconds = request.getDuration() == null ? null : request.getTimeUnit().toSeconds(request.getDuration());

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
                        TimeUnit.MILLISECONDS.sleep(1000);
                        crawlAndBuffer(token, request.getLimit(), seconds);
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

            // 2. 等待所有爬取任务完成（动态等待：0=无限等待，非0=按配置超时等待）
            int awaitMinutes = config.getCrawlAwaitMinutes();
            try {
                boolean isTimeout;
                if (awaitMinutes == 0) {
                    crawlLatch.await();
                    isTimeout = false;
                } else {
                    isTimeout = !crawlLatch.await(awaitMinutes, TimeUnit.MINUTES);
                }

                if (isTimeout) {
                    log.warn("等待超时（>{}分钟），剩余未完成任务数: {}", awaitMinutes, crawlLatch.getCount());
                    for (String timeoutToken : pendingTokens.keySet()) {
                        failedTokenMap.put(timeoutToken, String.format("批量爬取超时（>%d分钟），任务未完成", awaitMinutes));
                    }
                }
            } catch (InterruptedException e) {
                log.error("等待爬取任务完成时线程被中断", e);
                Thread.currentThread().interrupt(); // 恢复中断状态，符合线程规范

                // 中断时将未完成任务加入失败名单
                for (String interruptedToken : pendingTokens.keySet()) {
                    failedTokenMap.put(interruptedToken, "爬取任务被中断，任务未完成");
                }
            }

            // 3. 处理剩余缓冲区数据
            flushBuffer();

            String message = String.format("总任务：%d | 成功：%d | 失败：%d",
                tokens.size(), crawledCount.get(), failedTokenMap.size());
            log.info(message);
            if (!failedTokenMap.isEmpty()) {
                String failedTokenJson = JSON.toJSONString(failedTokenMap, PrettyFormat);
                log.error("失败名单：\n{}", failedTokenJson);
            }
            ActivityCrawlResult crawlResult = ActivityCrawlResult.builder()
                .activityDTOMap(Collections.unmodifiableMap(new HashMap<>(activityDTOMap)))
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


    /**
     * 爬取单个用户动态并加入缓冲区，达到阈值则批量入库
     */
    private void crawlAndBuffer(String token, Integer limit, Long seconds) {
        try {
            ActivityDTO activityDTO = crawl(token, limit, seconds);
            activityDTOMap.put(token, activityDTO);

            // 使用offer避免队列满时阻塞
            if (!batchBuffer.offer(activityDTO)) {
                flushBuffer();
                // 二次尝试入队，检查结果
                boolean offerSuccess = batchBuffer.offer(activityDTO);
                if (!offerSuccess) {
                    log.error("缓冲区强制入库后仍满，token: {} 兜底单条入库", token);
                    // 兜底：直接单条入库，避免数据丢失
                    saveActivities(Collections.singletonList(activityDTO));
                }
            }
            crawledCount.incrementAndGet();

            if (batchBuffer.size() >= config.getBatchSize()) {
                flushBuffer();
            }
        } catch (Exception e) {
            log.error("用户动态爬取失败（不重试），token: {}", token, e);
            failedTokenMap.put(token, e.getMessage());
        }
    }

    /**
     * 将缓冲区数据批量入库
     */
    private void flushBuffer() {
        List<ActivityDTO> batchList = new ArrayList<>();
        batchBuffer.drainTo(batchList);
        if (!batchList.isEmpty()) {
            try {
                saveActivities(batchList);
            } catch (Exception e) {
                log.error("批量入库失败，重新加入缓冲区", e);
                // 保证批量添加原子性
                synchronized (batchBufferLock) {
                    batchBuffer.addAll(batchList);
                }
            }
        }
    }

    public void saveActivities(List<ActivityDTO> activityDTOS) {
        if (CollectionUtils.isEmpty(activityDTOS)) {
            return;
        }

        List<InteractionDTO> toAddInteractionDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getInteractionDTOS);
        List<QuestionDTO> toAddQuestionDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getQuestionDTOS);
        List<AnswerDTO> toAddAnswerDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getAnswerDTOS);
        List<ArticleDTO> toAddArticleDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getArticleDTOS);
        List<PinDTO> toAddPinDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getPinDTOS);
        List<TopicDTO> toAddTopicDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getTopicDTOS);
        List<CollectionDTO> toAddCollectionDTOS = BatchUtils.mergeChildLists(activityDTOS, ActivityDTO::getCollectionDTOS);

        // 1. 交互记录入库（已存在的无需更新）
        Set<Long> existInteractionIds = interactionRepository.listAll().stream()
            .map(ZhihuUserInteraction::getInteractionId)
            .collect(Collectors.toSet());
        BatchUtils.convertAndBatchSave(toAddInteractionDTOS, ZhihuUserInteraction::new,
            interactionRepository::batchUpsert, "interaction",
            BatchUtils.getNotExistIdFilter(existInteractionIds, InteractionDTO::getInteractionId) // 过滤规则
        );
        // 2. 关联的问题入库/更新
        BatchUtils.convertAndBatchSave(toAddQuestionDTOS, ZhihuQuestion::new,
            questionRepository::batchUpsert, TargetType.QUESTION.getType());
        // 3. 关联的回答入库/更新
        BatchUtils.convertAndBatchSave(toAddAnswerDTOS, ZhihuAnswer::new,
            answerRepository::batchUpsert, TargetType.ANSWER.getType());
        // 4. 关联的文章入库/更新
        BatchUtils.convertAndBatchSave(toAddArticleDTOS, ZhihuArticle::new,
            articleRepository::batchUpsert, TargetType.ARTICLE.getType());
        // 5. 关联的想法入库/更新
        BatchUtils.convertAndBatchSave(toAddPinDTOS, ZhihuPin::new,
            pinRepository::batchUpsert, TargetType.PIN.getType());
        // 6. 关联的话题入库/更新
        BatchUtils.convertAndBatchSave(toAddTopicDTOS, ZhihuTopic::new,
            topicRepository::batchUpsert, "topic");
        // 7. 关联的收藏夹入库/更新
        BatchUtils.convertAndBatchSave(toAddCollectionDTOS, ZhihuCollection::new,
            collectionRepository::batchUpsert, TargetType.COLLECTION.getType());
    }

    @Builder
    @Getter
    public static class ActivityCrawlOnceResult {
        private ActivityDTO activityDTO;
        private Paging paging;
    }

    @Getter
    public static class Paging {
        private Boolean isEnd;  // 其实不该以isXXX来命名，这里仅为了与知乎字段保持一致，需注意框架解析是否会引起序列化错误
        private Boolean needForceLogin;
        private String next;
    }
}
