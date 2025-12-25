package org.acpasser.zhihunet.console.aop.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.dto.user.UserDTO;
import org.acpasser.zhihunet.common.enums.RespCode;
import org.acpasser.zhihunet.common.response.BaseResponse;
import org.acpasser.zhihunet.common.utils.JwtUtil;
import org.acpasser.zhihunet.console.util.AuthUtils;
import org.acpasser.zhihunet.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(2)
public class UserFilter extends GenericFilter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, String> redis;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (AuthUtils.passAuthCheck(request)) {
            chain.doFilter(req, res);
            return;
        }

        String username = request.getHeader(Constant.GATEWAY_USER_NAME);
        String token = request.getHeader(Constant.GATEWAY_USER_TOKEN);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(token)
                || !JwtUtil.validateToken(token)
                || !token.equals(redis.opsForValue().get(String.format(Constant.REDIS_USER_KEY, username)))) {
            response.getWriter().write(buildNotLoginResponseJson());
            response.getWriter().flush();
            return;
        }
        UserDTO user = userService.getUserByName(username);
        request.getSession().setAttribute(Constant.SESSION_KEY_CONSOLE_USER, user);
        chain.doFilter(req, res);
    }

    private String buildNotLoginResponseJson() {
        BaseResponse baseResponse = BaseResponse.newFailResponse()
                .errorCode(RespCode.NOT_LOGIN.getCode())
                .errorMsg(RespCode.NOT_LOGIN.getDesc())
                .build();
        try {
            return objectMapper.writeValueAsString(baseResponse);
        } catch (IOException e) {
            log.error("转换json字符串失败", e);
            return "";
        }
    }
}
