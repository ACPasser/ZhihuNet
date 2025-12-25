package org.acpasser.zhihunet.common.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyRequest {
    @NotBlank
    @Email(message = "邮箱格式不正确")
    private String email;

    // 用于验证
    private String code;
}
