package org.acpasser.zhihunet.console.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.enums.ErrorCode;
import org.acpasser.zhihunet.common.enums.RespCode;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.common.response.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleBusinessException(BusinessException ex) {
        log.error(ex.getMessage(), ex);
        return BaseResponse.newFailResponse()
                .errorCode(ex.getCode())
                .errorMsg(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error(ex.getMessage(), ex);
        return BaseResponse.newFailResponse()
                .errorCode(RespCode.MISSING_REQUIRED_PARAM.getCode())
                .errorMsg(String.format(RespCode.MISSING_REQUIRED_PARAM.getDesc(), ex.getParameterName()))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        String errMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + "：" + error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return BaseResponse.newFailResponse()
                .errorCode(RespCode.MISSING_REQUIRED_PARAM.getCode())
                .errorMsg(errMsg)
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleRuntimeException(HttpServletRequest request, RuntimeException ex) {
        log.error(ex.toString(), ex);
        return BaseResponse.newFailResponse()
                .errorCode(ErrorCode.SYSTEM_EXCEPTION.getCode())
                .errorMsg(getDefaultErrorMsg(request, ex))
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse handleAny(HttpServletRequest request, Throwable t) {
        log.error(t.toString(), t);
        return BaseResponse.newFailResponse()
                .errorCode(ErrorCode.SYSTEM_EXCEPTION.getCode())
                .errorMsg(getDefaultErrorMsg(request, t))
                .build();
    }

    private String getDefaultErrorMsg(HttpServletRequest request, Throwable t) {
        String defaultErrorMsg = (String) request.getAttribute(Constant.REQUEST_DEFAULT_ERROR_MSG);
        return StringUtils.isBlank(defaultErrorMsg) ? t.getMessage() : defaultErrorMsg;
    }
}
