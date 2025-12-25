package org.acpasser.zhihunet.common.request.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoUpdateRequest {
    @Email
    String email;

    String oldPassword;
    String newPassword;
}
