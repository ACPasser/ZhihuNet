package org.acpasser.zhihunet.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseEnum {
    SUCCESS(1000000, "SUCCESS"),
    BUSSINESS_EXCEPTION(2000000, "BUSSINESS_EXCEPTION"),
    BAD_PARAMS(3000000, "BAD_PARAMS"),
    SIGN_UNVALID(3000001, "SIGN_UNVALID"),
    SYSTEM_EXCEPTION(4000000, "SYSTEM_EXCEPTION"),
    SOCKET_TIMEOUT(4000001, "SOCKET_TIMEOUT"),
    OPTIMISTIC_LOCK_EXCEPTION(4000002, "OPTIMISTIC_LOCK_EXCEPTION"),
    SERVICE_HAD_NOT_READY(4000003, "SERVICE_HAD_NOT_READY"),
    RATE_LIMIT_EXCEED_EXCEPTION(4000004, "RATE_LIMIT_EXCEED_EXCEPTION");

    private int code;
    private String desc;

    public ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Code not exit.");
    }
}
