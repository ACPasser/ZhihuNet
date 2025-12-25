package org.acpasser.zhihunet.common.request.zhihu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZhihuUserActivityCrawlRequest implements Serializable {

    /**
     * 走Dubbo接口，每次仅允许爬一个用户
     */
    String token;

    /**
     * 动态页数（1页是7条）
     */
    Integer limit;

    /**
     * 时间范围（最多1年）
     */
    Integer duration;

    /**
     * 时间单位
     */
    TimeUnit timeUnit;
}
