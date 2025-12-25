package org.acpasser.zhihunet.core.service;

public interface EmailService {

    void sendVerifyCode(String toEmail);

    void verifyCode(String toEmail, String code);

}
