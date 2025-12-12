// src/main/java/org/jyr/postbox/controller/NotificationController.java
package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.dto.NotificationDTO;
import org.jyr.postbox.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록 조회 (userId 쿼리 파라미터)
    @GetMapping
    public List<NotificationDTO> list(@RequestParam Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    // 알림 읽음 처리
    @PostMapping("/{id}/read")
    public void markRead(
            @RequestParam Long userId,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(userId, id);
    }

    // 안 읽은 알림 개수 조회
    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestParam Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    // ✅ 내 알림 전체 읽음 처리
    @PostMapping("/read-all")
    public void markAllRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
    }
}
