package org.acpasser.zhihunet.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RespCode implements BaseEnum {
    SUCCESS(0, "SUCCESS"),
    FAIL(1, "FAIL"),
    MISSING_REQUIRED_PARAM(2, "'%s' is required'"),
    NO_PERMISSION(3, "当前用户没有权限"),
    NOT_LOGIN(401, "not login yet");

    private int code;
    private String desc;
}
