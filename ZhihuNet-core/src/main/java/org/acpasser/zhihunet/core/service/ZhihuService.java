package org.acpasser.zhihunet.core.service;


import org.acpasser.zhihunet.common.dto.ListDataDTO;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserDTO;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserInteractionDTO;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserActivityCrawlRequest;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserListRequest;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;

import java.util.List;

public interface ZhihuService {

    ListDataDTO<ZhihuUserDTO> listUsers(ZhihuUserListRequest params);

    List<ZhihuUserInteractionDTO> listInteractions(String token);

    ActivityCrawlResult crawlActivity(ZhihuUserActivityCrawlRequest activityCrawlRequest);

}
