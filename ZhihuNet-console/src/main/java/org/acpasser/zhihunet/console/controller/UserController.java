package org.acpasser.zhihunet.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.dto.user.UserDTO;
import org.acpasser.zhihunet.common.request.user.UserInfoUpdateRequest;
import org.acpasser.zhihunet.common.request.user.UserRegisterRequest;
import org.acpasser.zhihunet.common.response.BaseResponse;
import org.acpasser.zhihunet.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册")
    public BaseResponse register(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        String msg = userService.register(userRegisterRequest);
        return BaseResponse.newSuccResponse().result(msg).build();
    }

    @GetMapping("/info")
    @Operation(summary = "个人信息")
    public BaseResponse info(HttpServletRequest request) {
        UserDTO user = (UserDTO) request.getSession().getAttribute(Constant.SESSION_KEY_CONSOLE_USER);
        return BaseResponse.newSuccResponse().result(user).build();
    }

    @PostMapping("/login")
    @Operation(summary = "登陆")
    public BaseResponse login(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        String msg = userService.login(userRegisterRequest);
        return BaseResponse.newSuccResponse().result(msg).build();
    }

    @PutMapping
    @Operation(summary = "更新邮箱/密码")
    public BaseResponse update(HttpServletRequest request, @RequestBody @Valid UserInfoUpdateRequest updateRequest) {
        UserDTO user = (UserDTO) request.getSession().getAttribute(Constant.SESSION_KEY_CONSOLE_USER);
        userService.update(user, updateRequest);
        return BaseResponse.newSuccResponse().result(true).build();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public BaseResponse uploadAvatar(HttpServletRequest request, @RequestParam("image") MultipartFile image) {
        UserDTO user = (UserDTO) request.getSession().getAttribute(Constant.SESSION_KEY_CONSOLE_USER);
        String imageUrl = userService.uploadAvatar(user, image);
        return BaseResponse.newSuccResponse().result(imageUrl).build();
    }
}