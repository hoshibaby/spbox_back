package org.jyr.postbox.repository;

import org.jyr.postbox.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // targetUser.id 로 조회
    List<Notification> findByTargetUser_IdOrderByCreatedAtDesc(Long userId);
    long countByTargetUser_IdAndReadFalse(Long userId);

    List<Notification> findByTargetUser_IdAndReadFalse(Long userId);

    // 알림에서 메시지 id 리스트로 한 번에 삭제
    void deleteAllByMessage_IdIn(List<Long> messageIds);
}
