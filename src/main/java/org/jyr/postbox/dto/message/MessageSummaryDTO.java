package org.jyr.postbox.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageSummaryDTO {

    private Long id;             // 메시지 PK
    private String shortContent; // 앞 20글자 요약
    private boolean fromOwner;   // 주인 글인지
    private boolean hasReply;    // 답장 여부
    private boolean hidden;      // 숨김 여부
    private LocalDateTime createdAt; // 작성 시간
    private String content;
    // 작성자 타입 정보
    private String authorType;      // "ANONYMOUS" / "OWNER" / "COUNSELOR" / "AI"
    private String authorLabel;     // 화면에 보여줄 이름
    private Long boxOwnerId;
}
