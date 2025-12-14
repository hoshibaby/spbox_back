package org.jyr.postbox.security;

public class TokenUtil {

    private TokenUtil() {}

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다. (Authorization 헤더 없음)");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization 형식이 올바르지 않습니다. (Bearer 토큰 필요)");
        }
        return authorizationHeader.substring(7).trim();
    }
}
