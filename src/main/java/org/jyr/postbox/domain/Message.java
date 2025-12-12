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

    // 박스 주인의 답변 (있을 수도, 없을 수도)
    @Column(length = 1000)
    private String replyContent;

    private LocalDateTime replyCreatedAt;

    // 작성자 타입: OWNER(박스 주인) / ANONYMOUS(익명)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorType authorType;



    // OWNER일 때만 채워지는 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private User authorUser;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.hidden = false;
        this.systemMessage = false;
        this.hasAnyAnswer = false;
        if (this.authorType == null) {
            this.authorType = AuthorType.ANONYMOUS;
        }
    }

    // 답변 달 때 사용할 헬퍼 메서드
    public void writeReply(String replyContent) {
        this.replyContent = replyContent;
        this.replyCreatedAt = LocalDateTime.now();
        this.hasAnyAnswer = true;
    }

    // 숨김 처리
    public void hide() {
        this.hidden = true;
    }
}
