package org.acpasser.zhihunet.console.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class AuthUtils {
    private static final List<String> NO_AUTH_URI_LIST = List.of("/user/register", "/user/login");

    private static final List<String> NO_AUTH_URI_PREFIX_LIST = List.of("/avatars", "/swagger-ui", "/api-docs");

    public static boolean passAuthCheck(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (NO_AUTH_URI_LIST.contains(uri)) {
            return true;
        }

        return NO_AUTH_URI_PREFIX_LIST.stream().anyMatch(uri::startsWith);
    }
}
