package org.acpasser.zhihunet.common.response;

import org.acpasser.zhihunet.common.constant.Constant;

import java.util.List;

public class ListResponseUtils {
    public static <T> BaseResponse buildSucListResponse(long total, Integer pageNo, Integer pageSize, List<T> data) {
        ListResponse<T> response = ListResponse.<T>builder()
                .total(total)
                .pageNo(pageNo == null ? Constant.DEFAULT_PAGE_NO : pageNo)
                .pageSize(pageSize == null ? Constant.DEFAULT_PAGE_SIZE : pageSize)
                .data(data)
                .build();
        return BaseResponse.newSuccResponse().result(response).build();
    }

}
