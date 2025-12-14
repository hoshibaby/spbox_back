package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.dto.user.MyProfileResponseDTO;
import org.jyr.postbox.dto.user.MyProfileUpdateRequestDTO;
import org.jyr.postbox.security.JwtTokenProvider;
import org.jyr.postbox.security.TokenUtil;
import org.jyr.postbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 내 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<MyProfileResponseDTO> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String token = TokenUtil.extractBearerToken(authorization);

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalStateException("토큰이 만료되었거나 유효하지 않습니다.");
        }

        String loginUserId = jwtTokenProvider.getUserId(token); // subject == userId
        MyProfileResponseDTO dto = userService.getMyProfile(loginUserId);
        return ResponseEntity.ok(dto);
    }

    // ✅ 내 프로필 수정
    @PatchMapping("/profile")
    public ResponseEntity<MyProfileResponseDTO> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody MyProfileUpdateRequestDTO dto
    ) {
        String token = TokenUtil.extractBearerToken(authorization);

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalStateException("토큰이 만료되었거나 유효하지 않습니다.");
        }

        String loginUserId = jwtTokenProvider.getUserId(token);
        MyProfileResponseDTO updated = userService.updateMyProfile(loginUserId, dto);
        return ResponseEntity.ok(updated);
    }
}
