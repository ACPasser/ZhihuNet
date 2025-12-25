package org.acpasser.zhihunet.contract.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCrawlRequest implements Serializable {

    /**
     * 待爬用户token列表
     */
    List<String> tokens;

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
