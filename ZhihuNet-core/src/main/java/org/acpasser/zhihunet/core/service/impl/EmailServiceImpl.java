package org.acpasser.zhihunet.core.service.impl;


import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.core.service.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 生成6位数字验证码
    public static String genVerifyCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public String checkAndGenCode(String toEmail) {
        String key = String.format(Constant.REDIS_EMAIL_KEY, toEmail);
        String code = genVerifyCode();
        // 保留1分钟
        Boolean canSend = redisTemplate.opsForValue().setIfAbsent(key, code,
                Constant.REDIS_EMAIL_KEY_EXPIRATION_MIN, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(canSend)) {
            Long remainingSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new BusinessException(String.format("发送过于频繁，请%d秒后再试", remainingSeconds));
        }
        return code;
    }

    // 发送HTML格式邮件（每日最多100封）
    public void sendVerifyCode(String toEmail) {
        try {
            // 1. check
            String code = checkAndGenCode(toEmail);

            // 2. 构建
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【ZhihuNet - 邮箱验证码】");

            // HTML内容
            String htmlContent = "<div style='font-size: 14px; color: #333;'>" +
                    "<p>您好！</p>" +
                    "<p>您的邮箱验证码是：<span style='font-size: 18px; color: #ff4d4f; font-weight: bold;'>"
                        + code + "</span></p>" +
                    "<p>验证码有效期：1分钟，请尽快完成验证。</p>" +
                    "<p style='color: #999; font-size: 12px;'>如非本人操作，请忽略此邮件。</p>" +
                    "</div>";
            helper.setText(htmlContent, true); // 第二个参数true表示是HTML格式

            // 3. 发送
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new BusinessException("邮件发送失败：" + e.getMessage());
        }
    }

    // 校验验证码（成功后删除缓存，防止重复使用）
    public void verifyCode(String toEmail, String code) {
        if (StringUtils.isBlank(toEmail) || StringUtils.isBlank(code)) {
            throw new BusinessException("邮箱或验证码不能为空！");
        }
        String key = String.format(Constant.REDIS_EMAIL_KEY, toEmail);
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null) {
            throw new BusinessException("验证码未发送或已过期！");
        }
        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误！");
        }
        redisTemplate.delete(key);
    }
}
