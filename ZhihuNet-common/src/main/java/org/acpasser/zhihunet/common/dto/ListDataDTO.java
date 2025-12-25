package org.acpasser.zhihunet.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListDataDTO<T> {
    private long total;
    private List<T> pagedList;
}
