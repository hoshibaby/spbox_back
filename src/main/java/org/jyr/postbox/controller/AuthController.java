// src/main/java/org/jyr/postbox/controller/AuthController.java
package org.jyr.postbox.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jyr.postbox.dto.user.LoginRequestDTO;
import org.jyr.postbox.dto.user.LoginResponseDTO;
import org.jyr.postbox.dto.user.UserSignupDTO;
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

    // 🔹 회원가입  POST /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody @Valid UserSignupDTO dto) {

        Long userId = userService.signup(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userId);
    }

    // 🔹 로그인  POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        LoginResponseDTO response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    // 🔹 아이디 찾기  POST /api/auth/find-id
    //    요청: { "email": "test1@test.com" }
    //    응답: { "userId": "ororong1" }
    @PostMapping("/find-id")
    public ResponseEntity<Map<String, String>> findIdByEmail(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        String userId = userService.findUserIdByEmail(email);

        return ResponseEntity.ok(Map.of("userId", userId));
    }

    // 🔹 비밀번호 찾기  POST /api/auth/find-password
    //    요청: { "userId": "ororong1", "email": "test1@test.com" }
    //    응답: { "tempPassword": "랜덤임시비번" }
    @PostMapping("/find-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {

        String userId = request.get("userId");
        String email = request.get("email");

        String tempPassword = userService.resetPassword(userId, email);

        return ResponseEntity.ok(Map.of("tempPassword", tempPassword));
    }
}
