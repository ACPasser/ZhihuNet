package org.acpasser.zhihunet.common.request.zhihu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZhihuUserCrawlRequest implements Serializable {

    /**
     * 走Dubbo接口，每次仅允许爬一个用户
     */
    String token;

}
