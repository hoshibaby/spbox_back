package org.jyr.postbox.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 박스에 달린 메시지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    // 본문
    @Column(nullable = false, length = 1000)
    private String content;

    // 작성 시간
    private LocalDateTime createdAt;

    // 어떤 형태든 "답변이 하나라도 있다"는 플래그
    @Column(nullable = false)
    private boolean hasAnyAnswer;

    // 숨김 여부 (박스 주인이 가릴 때)
    @Column(nullable = false)
    private boolean hidden;

    // 일반 메시지인지, 시스템 로깅용 메시지인지
    @Column(nullable = false)
    private boolean systemMessage;

    // 답변 내용
    @Column(length = 1000)
    private String replyContent;

    private LocalDateTime replyCreatedAt;

    // 작성자 타입: OWNER(박스 주인) / ANONYMOUS(익명)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorType authorType;

    // 답변 작성자 타입: OWNER / AI
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReplyAuthorType replyAuthorType;

    // OWNER일 때만 채워지는 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private User authorUser;

    // 비공개 메시지 여부 (박스 주인만 보기)
    @Column(nullable = false)
    private boolean privateMessage;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.hidden = false;
        this.systemMessage = false;
        this.hasAnyAnswer = false;
        this.privateMessage = false;
        if (this.authorType == null) {
            this.authorType = AuthorType.ANONYMOUS;
        }
    }

    // 기본 답변 (기존 로직용: OWNER)
    public void writeReply(String replyContent) {
        writeReply(replyContent, ReplyAuthorType.OWNER);
    }

    // 답변 타입을 명시하는 진짜 메서드
    public void writeReply(String replyContent, ReplyAuthorType type) {
        this.replyContent = replyContent;
        this.replyCreatedAt = LocalDateTime.now();
        this.hasAnyAnswer = true;
        this.replyAuthorType = type;
    }

    // 숨김 처리
    public void hide() {
        this.hidden = true;
    }
}
