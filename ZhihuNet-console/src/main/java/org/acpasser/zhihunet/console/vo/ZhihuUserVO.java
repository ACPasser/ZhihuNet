package org.acpasser.zhihunet.console.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserDTO;
import org.springframework.beans.BeanUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserVO {
    // 基本信息
    private String token;
    private String name;
    private String indexUrl;

    // 其他展示信息
    private String headline;    // 签名
    private String business;    // 所在行业
    private String employments; // 职业-公司
    private String educations;  // 教育经历-学校

    public static ZhihuUserVO convert(ZhihuUserDTO userDTO) {
        ZhihuUserVO zhihuUserVO = new ZhihuUserVO();
        BeanUtils.copyProperties(userDTO, zhihuUserVO);
        return zhihuUserVO;
    }
}
