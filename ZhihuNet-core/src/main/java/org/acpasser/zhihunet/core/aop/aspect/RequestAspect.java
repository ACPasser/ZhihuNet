package org.acpasser.zhihunet.core.aop.aspect;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.dto.user.UserDTO;
import org.acpasser.zhihunet.common.utils.FileSizeFormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Aspect
@Order(3)
@Component
public class RequestAspect {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getRequestAspect() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postRequestAspect() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putRequestAspect() {
    }

    @Before("getRequestAspect()")
    public void handleGetRequest(JoinPoint point) {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        logGetRequest(request);
        setDefaultErrorMessage(request);
    }

    @Before("postRequestAspect()")
    public void handlePostRequest(JoinPoint point) {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        logPostRequest(request, point);
        setDefaultErrorMessage(request);
    }

    @Before("putRequestAspect()")
    public void handlePutRequest(JoinPoint point) {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        logPutRequest(request, point);
        setDefaultErrorMessage(request);
    }

    private void logGetRequest(HttpServletRequest request) {
        log.info("GET REQUEST: {}, user: {}", getUrl(request), getUserName(request));
    }

    private void logPutRequest(HttpServletRequest request, JoinPoint point) {
        log.info("PUT REQUEST: {}, body: {}, user: {}",
                getUrl(request), getRequestBody(point), getUserName(request));
    }

    private void logPostRequest(HttpServletRequest request, JoinPoint point) {
        log.info("POST REQUEST: {}, body: {}, user: {}",
                getUrl(request), getRequestBody(point), getUserName(request));
    }

    private String getUserName(HttpServletRequest request) {
        UserDTO user = (UserDTO) request.getSession().getAttribute(Constant.SESSION_KEY_CONSOLE_USER);
        return user == null ? "user_not_login" : user.getUserName();
    }

    private String getUrl(HttpServletRequest request) {
        if (StringUtils.isBlank(request.getQueryString())) {
            return request.getRequestURI();
        }
        return String.format("%s?%s", request.getRequestURI(), request.getQueryString());
    }

    private Object getRequestBody(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();

        // 查找 @RequestBody 参数
        Annotation[][] annotations = signature.getMethod().getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotation = annotations[i];
            if (Arrays.stream(annotation).anyMatch(RequestBody.class::isInstance)) {
                return point.getArgs()[i];
            }
        }

        // 2. 查找 MultipartFile 参数（文件上传）
        Class<?>[] parameterTypes = signature.getParameterTypes();
        Object[] args = point.getArgs();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (MultipartFile.class.isAssignableFrom(parameterTypes[i])) {
                MultipartFile file = (MultipartFile) args[i];
                return String.format("MultipartFile{name: %s, size: %s}",
                        file.getOriginalFilename(), FileSizeFormatUtil.format(file.getSize()));
            }
        }
        return null;
    }

    private void setDefaultErrorMessage(HttpServletRequest request) {
        request.setAttribute(Constant.REQUEST_DEFAULT_ERROR_MSG, "请求失败");
    }
}
