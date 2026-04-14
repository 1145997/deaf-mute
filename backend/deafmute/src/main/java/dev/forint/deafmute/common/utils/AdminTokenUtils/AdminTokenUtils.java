package dev.forint.deafmute.common.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminTokenUtils {

    private final JwtUtils jwtUtils;
    private final HttpServletRequest request;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    public Long getCurrentAdminId() {
        String tokenHeader = request.getHeader(header);
        if (tokenHeader == null || !tokenHeader.startsWith(tokenPrefix)) {
            throw new RuntimeException("未登录或token不存在");
        }

        String token = tokenHeader;
        if (token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length());
        }
        token = token.trim();

        Claims claims = jwtUtils.parseToken(token);

        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new RuntimeException("token角色信息缺失");
        }

        return Long.valueOf(claims.getSubject());
    }

    public String getCurrentAdminRole() {
        String tokenHeader = request.getHeader(header);
        if (tokenHeader == null || !tokenHeader.startsWith(tokenPrefix)) {
            throw new RuntimeException("未登录或token不存在");
        }

        String token = tokenHeader;
        if (token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length());
        }
        token = token.trim();
        Claims claims = jwtUtils.parseToken(token);
        return claims.get("role", String.class);
    }

    public void checkAdminLogin() {
        String tokenHeader = request.getHeader(header);
        if (tokenHeader == null || !tokenHeader.startsWith(tokenPrefix)) {
            throw new RuntimeException("管理员未登录或token不存在");
        }

        String token = tokenHeader.substring(tokenPrefix.length()).trim();
        Claims claims = jwtUtils.parseToken(token);

        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new RuntimeException("管理员token角色信息缺失");
        }

        if (!"super_admin".equals(role) && !"admin".equals(role)) {
            throw new RuntimeException("无管理员权限");
        }
    }
}