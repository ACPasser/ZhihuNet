package org.acpasser.zhihunet.common.request.zhihu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.common.request.ListRequest;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserListRequest extends ListRequest {
    private String nameQuery;

    public String getNameQuery() {
        return nameQuery == null ? "" : nameQuery;
    }
}
