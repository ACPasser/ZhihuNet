package org.acpasser.zhihunet.common.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.common.constant.Constant;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Length(max = Constant.DEFAULT_USERNAME_LENGTH, message = "用户名过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(max = Constant.DEFAULT_PASSWORD_LENGTH, message = "密码过长")
    private String password;
}
