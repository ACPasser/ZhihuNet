package org.acpasser.zhihunet.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.acpasser.zhihunet.common.request.email.EmailVerifyRequest;
import org.acpasser.zhihunet.common.response.BaseResponse;
import org.acpasser.zhihunet.core.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email")
@Tag(name = "邮件模块")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/verification_code")
    @Operation(summary = "发送验证码")
    public BaseResponse sendVerifyCode(@Valid @RequestBody EmailVerifyRequest request) {
        emailService.sendVerifyCode(request.getEmail());
        return BaseResponse.newSuccResponse().result("验证码已发送").build();
    }

    @PostMapping("/verify")
    @Operation(summary = "校验验证码")
    public BaseResponse verifyCode(@Valid @RequestBody EmailVerifyRequest request) {
        emailService.verifyCode(request.getEmail(), request.getCode());
        return BaseResponse.newSuccResponse().result("校验成功").build();
    }
}
