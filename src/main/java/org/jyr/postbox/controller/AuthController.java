// src/main/java/org/jyr/postbox/controller/AuthController.java
package org.jyr.postbox.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jyr.postbox.dto.user.LoginRequestDTO;
import org.jyr.postbox.dto.user.LoginResponseDTO;
import org.jyr.postbox.dto.user.UserSignupDTO;
import org.jyr.postbox.repository.UserRepository;
import org.jyr.postbox.service.AdminUserService;
import org.jyr.postbox.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    // ✅ 아이디 중복확인  GET /api/auth/check-userid?userId=aaa
    @GetMapping("/check-userid")
    public ResponseEntity<Map<String, Object>> checkUserId(@RequestParam String userId) {
        boolean exists = userRepository.existsByUserId(userId);

        return ResponseEntity.ok(Map.of(
                "available", !exists,
                "message", exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다."
        ));
    }

    // ✅ 이메일 중복확인  GET /api/auth/check-email?email=a@a.com
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);

        return ResponseEntity.ok(Map.of(
                "available", !exists,
                "message", exists ? "이미 가입된 이메일입니다." : "사용 가능한 이메일입니다."
        ));
    }

    // 🔹 회원가입  POST /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody @Valid UserSignupDTO dto) {
        Long userId = userService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    // 🔹 로그인  POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = userService.login(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 정지 계정 같은 "상태" 문제
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 아이디 없음/비번 틀림 같은 입력 문제
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // 🔹 아이디 찾기  POST /api/auth/find-id
    @PostMapping("/find-id")
    public ResponseEntity<Map<String, String>> findIdByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String userId = userService.findUserIdByEmail(email);
        return ResponseEntity.ok(Map.of("userId", userId));
    }

    // 🔹 비밀번호 찾기  POST /api/auth/find-password
    @PostMapping("/find-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String email = request.get("email");

        String tempPassword = userService.resetPassword(userId, email);
        return ResponseEntity.ok(Map.of("tempPassword", tempPassword));
    }




}
