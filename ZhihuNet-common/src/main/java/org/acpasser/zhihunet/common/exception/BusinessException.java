package org.acpasser.zhihunet.common.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.acpasser.zhihunet.common.enums.ErrorCode;
import org.acpasser.zhihunet.common.enums.RespCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = RespCode.FAIL.getCode();
    }

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = RespCode.FAIL.getCode();
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.code = errorCode.getCode();
    }
}
