package org.acpasser.zhihunet.contract.service;


import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;

public interface CrawlerService {

    /**
     * 批量爬取知乎用户
     */
    BaseResponse<UserCrawlResult> batchCrawlUsers(UserInfoCrawlRequest request);

    /**
     * 批量爬取用户最近的动态
     */
    BaseResponse<ActivityCrawlResult> batchCrawlRecentActivity(ActivityCrawlRequest request);
}
