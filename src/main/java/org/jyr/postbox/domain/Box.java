package org.jyr.postbox.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 박스 주인 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    // 링크에 사용할 고유 키 (ex: abc123ef)
    @Column(nullable = false, unique = true, length = 50)
    private String urlKey;

    // 박스 제목
    @Column(nullable = false, length = 100)
    private String title;

    private LocalDateTime createdAt;

    // 박스에 달린 메시지들
    @OneToMany(mappedBy = "box")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean allowAnonymous = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean aiMode = false;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.title == null || this.title.isBlank()) {
            this.title = "익명 메시지함";
        }
    }
}
