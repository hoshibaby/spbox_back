package org.jyr.postbox.service;

import org.jyr.postbox.domain.NotificationType;
import org.jyr.postbox.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {



    void createNotification(
            Long targetUserId,
            NotificationType type,
            String alertMessage,
            String linkUrl,
            Long messageId
    );
    // 시스템 알림 공지 보내기
    void sendSystemNotice(Long targetUserId, String alertMessage, String linkUrl);

    // 알림 목록 조회
    List<NotificationDTO> getUserNotifications(Long userId);
    // 알림 읽음 처리
    void markAsRead(Long userId, Long notificationId);

    long getUnreadCount(Long userId);

    void markAllAsRead(Long userId);

}
