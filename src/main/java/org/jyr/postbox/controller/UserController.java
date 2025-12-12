// src/main/java/org/jyr/postbox/controller/UserController.java
package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.user.UserDTO;
import org.jyr.postbox.dto.user.ChangePasswordRequestDTO;
import org.jyr.postbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    // 🔹 내 정보 조회 (UserDTO)
    @GetMapping
    public ResponseEntity<UserDTO> getMyInfo(
            @RequestParam("userId") String userId   // 🔥 String userId로 받기
    ) {
        User user = userService.findByUserId(userId);
        UserDTO dto = UserDTO.from(user);
        return ResponseEntity.ok(dto);
    }

    // 🔹 AI 상담 토글
    @PutMapping("/settings/ai")
    public ResponseEntity<Void> toggleAiConsulting(
            @RequestParam("userId") String userId,
            @RequestParam("enabled") boolean enabled
    ) {
        userService.updateAiConsultingByUserId(userId, enabled);
        return ResponseEntity.ok().build();
    }

    // 🔹 계정 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(
            @RequestParam("userId") String userId
    ) {
        userService.deleteUserByUserId(userId);
        return ResponseEntity.ok().build();
    }

    // ⭐ 🔹 비밀번호 변경
    //    PUT /api/me/password?userId=ororong1
    //    Body: { "currentPassword": "...", "newPassword": "..." }
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestParam("userId") String userId,
            @RequestBody ChangePasswordRequestDTO dto
    ) {
        userService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
