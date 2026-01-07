package org.acpasser.zhihunet.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.dto.ListDataDTO;
import org.acpasser.zhihunet.common.dto.ListZhihuUserCondition;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserDTO;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserInteractionDTO;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserActivityCrawlRequest;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserCrawlRequest;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserListRequest;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.contract.service.CrawlerService;
import org.acpasser.zhihunet.core.service.ZhihuService;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.acpasser.zhihunet.model.ZhihuUserInteraction;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserInteractionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserRepository;
import org.acpasser.zhihunet.repository.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class ZhihuUserServiceImpl implements ZhihuService {

    @Autowired
    private ZhihuUserRepository zhihuUserRepository;

    @Autowired
    private ZhihuUserInteractionRepository userInteractionRepository;

    @DubboReference(version = "1.0.0", group = "zhihu-crawler")
    private CrawlerService crawlerService;

    @Override
    public ListDataDTO<ZhihuUserDTO> listUsers(ZhihuUserListRequest params) {
        ListZhihuUserCondition condition = ListZhihuUserCondition.builder()
                .offset((params.getPageNo() - 1) * params.getPageSize())
                .pageSize(params.getPageSize())
                .nameQuery(params.getNameQuery()).build();
        condition.setNameQuery(SqlUtils.like(condition.getNameQuery()));
        List<ZhihuUserDTO> zhihuUserDTOS = zhihuUserRepository.list(condition).stream().map(ZhihuUserDTO::convert).toList();
        long count = zhihuUserRepository.count(condition);
        return new ListDataDTO<>(count, zhihuUserDTOS);
    }

    @Override
    public List<ZhihuUserInteractionDTO> listInteractions(String token) {
        checkTokenExist(token);
        List<ZhihuUserInteraction> userInteractions = userInteractionRepository.listByToken(token);
        return userInteractions.stream()
            .map(ZhihuUserInteractionDTO::convert)
            .toList();
    }

    public UserCrawlResult crawlUser(ZhihuUserCrawlRequest userCrawlRequest) {
        UserInfoCrawlRequest crawlRequest = UserInfoCrawlRequest.builder()
            .tokens(Collections.singletonList(userCrawlRequest.getToken()))
            .build();
        BaseResponse<UserCrawlResult> response = crawlerService.batchCrawlUsers(crawlRequest);
        if (response.isSuccess()) {
            return response.getResult();
        } else {
            log.error(response.getmessage());
            throw new BusinessException(response.getmessage());
        }
    }

    public ActivityCrawlResult crawlActivity(ZhihuUserActivityCrawlRequest activityCrawlRequest) {
        ActivityCrawlRequest crawlRequest = ActivityCrawlRequest.builder()
            .tokens(Collections.singletonList(activityCrawlRequest.getToken()))
            .limit(activityCrawlRequest.getLimit())
            .duration(activityCrawlRequest.getDuration())
            .timeUnit(activityCrawlRequest.getTimeUnit())
            .build();
        BaseResponse<ActivityCrawlResult> response = crawlerService.batchCrawlRecentActivity(crawlRequest);
        if (response.isSuccess()) {
            return response.getResult();
        } else {
            log.error(response.getmessage());
            throw new BusinessException(response.getmessage());
        }
    }

    public void checkTokenExist(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BusinessException("用户token不能为空");
        }
        ZhihuUser user = zhihuUserRepository.getByToken(token);
        if (user == null) {
            throw new BusinessException(String.format("数据源无此用户：%s", token));
        }
    }

}
