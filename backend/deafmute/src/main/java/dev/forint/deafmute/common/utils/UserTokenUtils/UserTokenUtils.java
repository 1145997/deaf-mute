package dev.forint.deafmute.common.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTokenUtils {

    private final JwtUtils jwtUtils;
    private final HttpServletRequest request;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;
    private String getTokenFromRequest() {
        String tokenHeader = request.getHeader(header);
        if (tokenHeader == null || tokenHeader.isBlank()) {
            throw new RuntimeException("未登录或token不存在");
        }

        if (tokenHeader.startsWith(tokenPrefix)) {
            tokenHeader = tokenHeader.substring(tokenPrefix.length());
        }

        return tokenHeader.trim();
    }

    public Long getCurrentUserId() {
        String token = getTokenFromRequest();
        Claims claims = jwtUtils.parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getCurrentRole() {
        String token = getTokenFromRequest();
        Claims claims = jwtUtils.parseToken(token);
        return claims.get("role", String.class);
    }

    public void checkUserLogin() {
        String tokenHeader = request.getHeader(header);
        if (tokenHeader == null || !tokenHeader.startsWith(tokenPrefix)) {
            throw new RuntimeException("用户未登录或token不存在");
        }

        String token = tokenHeader.substring(tokenPrefix.length()).trim();
        Claims claims = jwtUtils.parseToken(token);

        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new RuntimeException("用户token角色信息缺失");
        }

        if (!"user".equals(role)) {
            throw new RuntimeException("无用户权限");
        }
    }
}