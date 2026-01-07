package org.acpasser.zhihunet.console.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.dto.ListDataDTO;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserDTO;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserInteractionDTO;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserActivityCrawlRequest;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserCrawlRequest;
import org.acpasser.zhihunet.common.request.zhihu.ZhihuUserListRequest;
import org.acpasser.zhihunet.common.response.BaseResponse;
import org.acpasser.zhihunet.common.response.ListResponseUtils;
import org.acpasser.zhihunet.console.vo.ZhihuUserInteractionVO;
import org.acpasser.zhihunet.console.vo.ZhihuUserVO;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.core.service.ZhihuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/zhihu")
@Tag(name = "知乎模块")
public class ZhihuController {

    @Autowired
    private ZhihuService zhihuService;

    @PostMapping("/user/list")
    @Operation(summary = "用户信息查询")
    public BaseResponse listZhihuUser(@RequestBody ZhihuUserListRequest params) {
        ListDataDTO<ZhihuUserDTO> zhihuUserDTOList = zhihuService.listUsers(params);
        List<ZhihuUserVO> zhihuUserVOList = zhihuUserDTOList.getPagedList().stream()
            .map(ZhihuUserVO::convert).toList();
        return ListResponseUtils.buildSucListResponse(zhihuUserDTOList.getTotal(), params.getPageNo(),
            params.getPageSize(), zhihuUserVOList);
    }

    @GetMapping("/user/interaction/{token}")
    @Operation(summary = "用户动态查询")
    public BaseResponse listUserInteraction(@PathVariable("token") String token) {
        List<ZhihuUserInteractionDTO> userInteractionDTOS = zhihuService.listInteractions(token);
        Map<LocalDate, List<ZhihuUserInteractionDTO>> dayToInteractionsMap = userInteractionDTOS.stream()
            .collect(Collectors.groupingBy(
                // 分组键：按互动时间的日期分组
                dto -> dto.getInteractionTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                // 收集器：对每个分组的列表按interactTime升序排序
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        list.sort(Comparator.comparing(ZhihuUserInteractionDTO::getInteractionTime));
                        return list;
                    }
                )
            ));

        List<ZhihuUserInteractionVO> userInteractionVOS = dayToInteractionsMap.entrySet().stream()
            .map(entry -> new ZhihuUserInteractionVO(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ZhihuUserInteractionVO::getDate))
            .toList();

        return BaseResponse.newSuccResponse().result(userInteractionVOS).build();
    }

    @PostMapping("/user/info/crawl")
    @Operation(summary = "爬取知乎用户")
    public BaseResponse crawlUserInteraction(@RequestBody ZhihuUserCrawlRequest userCrawlRequest) {
        UserCrawlResult user = zhihuService.crawlUser(userCrawlRequest);
        return BaseResponse.newSuccResponse().result(user).build();
    }

    @PostMapping("/user/interaction/crawl")
    @Operation(summary = "爬取用户动态")
    public BaseResponse crawlUserInteraction(@RequestBody ZhihuUserActivityCrawlRequest activityCrawlRequest) {
        ActivityCrawlResult activity = zhihuService.crawlActivity(activityCrawlRequest);
        return BaseResponse.newSuccResponse().result(activity).build();
    }
}
