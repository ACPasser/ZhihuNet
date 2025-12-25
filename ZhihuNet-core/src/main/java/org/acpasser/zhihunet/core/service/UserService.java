package org.acpasser.zhihunet.core.service;

import org.acpasser.zhihunet.common.dto.user.UserDTO;
import org.acpasser.zhihunet.common.request.user.UserRegisterRequest;
import org.acpasser.zhihunet.common.request.user.UserInfoUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    String register(UserRegisterRequest userRegisterRequest);

    String login(UserRegisterRequest userRegisterRequest);

    UserDTO getUserByName(String userName);

    void update(UserDTO user, UserInfoUpdateRequest updateRequest);

    String uploadAvatar(UserDTO user, MultipartFile file);
}
