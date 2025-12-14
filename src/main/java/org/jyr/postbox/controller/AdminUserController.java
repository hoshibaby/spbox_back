package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 1️⃣ 유저 전체 조회
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    // 2️⃣ 유저 정지
    @PatchMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId) {
        adminUserService.banUser(userId);
        return ResponseEntity.ok().build();
    }

    // 3️⃣ 유저 정지 해제
    @PatchMapping("/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        adminUserService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }

    //admin 부여
    @PatchMapping("/{userId}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        adminUserService.promoteToAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/demote")
    public ResponseEntity<?> demoteToUser(@PathVariable Long userId) {
        adminUserService.demoteToUser(userId);
        return ResponseEntity.ok().build();
    }



}
