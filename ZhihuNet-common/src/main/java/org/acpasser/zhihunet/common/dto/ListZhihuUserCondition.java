package org.acpasser.zhihunet.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListZhihuUserCondition {
    private String nameQuery;
    private Integer offset;
    private Integer pageSize;
}
