package org.acpasser.zhihunet.contract.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoCrawlRequest implements Serializable {

    /**
     * 待爬用户token列表
     */
    List<String> tokens;

}
