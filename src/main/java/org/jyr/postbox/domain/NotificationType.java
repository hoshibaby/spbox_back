package org.jyr.postbox.domain;

public enum NotificationType {
    // --- 유저/박스 관련 ---
    COMMENT,    // 익명 메시지 도착
    OWNER_REPLY,          // 박스 주인의 답장 도착
    AI_REPLY,             // 상담모드 답변 도착

    // --- 시스템/관리자 ---
    SYSTEM_NOTICE,        // 공지/안내/업데이트 메시지
    SYSTEM_ALERT          // 긴급 안내 등

    }
