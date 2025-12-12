// src/main/java/org/jyr/postbox/service/NotificationServiceImpl.java
package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.*;
import org.jyr.postbox.dto.NotificationDTO;
import org.jyr.postbox.dto.message.MessageCreateDTO;
import org.jyr.postbox.repository.BoxRepository;
import org.jyr.postbox.repository.MessageRepository;
import org.jyr.postbox.repository.NotificationRepository;
import org.jyr.postbox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final BoxRepository boxRepository;


    // (1) 공통 알림 생성
    @Override
    public void createNotification(
            Long targetUserId,
            NotificationType type,
            String alertMessage,
            String linkUrl,
            Long messageId
    ) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("알림 대상 유저가 없습니다. id=" + targetUserId));

        Message message = null;
        if (messageId != null) {
            message = messageRepository.findById(messageId).orElse(null);
        }

        System.out.println("[NOTI-CREATE] targetUser=" + targetUser.getNickname()
                + ", type=" + type
                + ", linkUrl=" + linkUrl
                + ", messageId=" + messageId);

        Notification notification = Notification.builder()
                .targetUser(targetUser)
                .type(type)
                .alertMessage(alertMessage)
                .linkUrl(linkUrl)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    // 시스템알림_관리상자
    @Override
    public void sendSystemNotice(Long targetUserId, String alertMessage, String linkUrl) {
                createNotification(
                targetUserId,
                NotificationType.SYSTEM_NOTICE, // 시스템용 타입
                alertMessage,
                linkUrl,
                null // 메시지와 연결되지 않은 순수 시스템 안내
        );
    }

    // 알림 목록 조회
    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByTargetUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDTO::from)
                .toList();
    }

    // ✅ 단일 알림 읽음 처리
    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 없습니다. id=" + notificationId));

        // 본인 알림인지 확인
        if (!n.getTargetUser().getId().equals(userId)) {
            throw new IllegalStateException("본인 알림만 읽음 처리할 수 있습니다.");
        }

        n.markAsRead();              // 엔티티에 있는 메서드 (아래 참고)
        notificationRepository.save(n);
    }

    //  전체 알림 읽음 처리
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> list =
                notificationRepository.findByTargetUser_IdAndReadFalse(userId);

        for (Notification n : list) {
            n.markAsRead();
        }

        notificationRepository.saveAll(list);
    }

    // ✅ 안 읽은 개수
    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByTargetUser_IdAndReadFalse(userId);
    }





}
