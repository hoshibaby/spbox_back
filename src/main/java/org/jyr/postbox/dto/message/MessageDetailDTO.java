package org.jyr.postbox.dto.message;

import lombok.Builder;
import lombok.Data;
import org.jyr.postbox.dto.box.BoxHeaderDTO;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageDetailDTO {

    private Long id;                // 메시지 PK
    private String content;         // 전체 메시지 내용
    private boolean fromOwner;      // 작성자가 박스 주인인지?
    private boolean hidden;         // 숨김 여부
    private LocalDateTime createdAt; // 작성 시각

    // 답장 정보
    private String replyContent;       // 답변 내용 (없으면 null)
    private LocalDateTime replyCreatedAt;
    private BoxHeaderDTO box;
    private Long authorUserId;   // null 가능
    private String authorType;   // OWNER / ANONYMOUS
    private Long boxOwnerId;
}
