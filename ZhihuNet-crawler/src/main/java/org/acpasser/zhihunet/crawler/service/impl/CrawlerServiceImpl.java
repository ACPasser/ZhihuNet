package org.acpasser.zhihunet.crawler.service.impl;

import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.contract.service.CrawlerService;
import org.acpasser.zhihunet.crawler.parser.ActivityParser;
import org.acpasser.zhihunet.crawler.parser.UserInfoParser;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@DubboService(version = "1.0.0", group = "zhihu-crawler")
@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private UserInfoParser userInfoParser;

    @Autowired
    private ActivityParser activityParser;

    @Override
    public BaseResponse<UserCrawlResult> batchCrawlUsers(UserInfoCrawlRequest request) {
        return userInfoParser.batchCrawl(request);
    }

    @Override
    public BaseResponse<ActivityCrawlResult> batchCrawlRecentActivity(ActivityCrawlRequest request) {
        return activityParser.batchCrawl(request);
    }
}
