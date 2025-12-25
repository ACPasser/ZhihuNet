package org.acpasser.zhihunet.common.constant;

public class Constant {
    public static final int DEFAULT_USERNAME_LENGTH = 20;
    public static final int DEFAULT_PASSWORD_LENGTH = 20;

    public static final int DEFAULT_PAGE_NO = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String GATEWAY_USER_NAME = "gateway-username";
    public static final String GATEWAY_USER_TOKEN = "gateway-user-token";

    public static final String SESSION_KEY_CONSOLE_USER = "console_user";

    public static final String REQUEST_DEFAULT_ERROR_MSG = "default_error_msg";

    public static final String REDIS_USER_KEY = "redis_user_key: %s";
    public static final Integer REDIS_USER_KEY_EXPIRATION_HOUR = 24;
    public static final String REDIS_EMAIL_KEY = "redis_email_key: %s";
    public static final Integer REDIS_EMAIL_KEY_EXPIRATION_MIN = 1;
}
