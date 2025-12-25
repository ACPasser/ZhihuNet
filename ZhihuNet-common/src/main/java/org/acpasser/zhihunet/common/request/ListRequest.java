package org.acpasser.zhihunet.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.common.constant.Constant;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListRequest implements Serializable {
    private Integer pageNo;
    private Integer pageSize;

    public Integer getPageNo() {
        return pageNo == null ? Constant.DEFAULT_PAGE_NO : pageNo;
    }

    public Integer getPageSize() {
        return pageSize == null ? Constant.DEFAULT_PAGE_SIZE : pageSize;
    }
}
