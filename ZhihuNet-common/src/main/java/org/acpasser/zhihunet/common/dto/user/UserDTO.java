package org.acpasser.zhihunet.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.model.User;
import org.springframework.beans.BeanUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String userName;
    private String userType;
    private String email;
    private String avatarUrl;

    public static UserDTO convert(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .userType(user.getUserType())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public User toUser() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }

}
