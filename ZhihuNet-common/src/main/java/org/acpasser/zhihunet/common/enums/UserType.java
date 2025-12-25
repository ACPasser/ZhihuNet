package org.acpasser.zhihunet.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserType {
    ADMIN("ADMIN", "管理员"),
    USER("USER", "普通用户"),
    GUEST("GUEST", "访客"),
    MEMBER("MEMBER", "会员");

    private final String code;
    private final String desc;

    public static UserType getByCode(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
