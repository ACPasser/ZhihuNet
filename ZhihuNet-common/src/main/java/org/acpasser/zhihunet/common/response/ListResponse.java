package org.acpasser.zhihunet.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListResponse<T> {
    private long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> data;
}
