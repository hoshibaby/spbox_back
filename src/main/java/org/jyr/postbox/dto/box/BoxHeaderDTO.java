package org.jyr.postbox.dto.box;

import lombok.*;

// 박스 상단 정보
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoxHeaderDTO {

    private Long boxId;
    private String boxTitle;
    private String urlKey;

    private String ownerName;       // ex) 열시
    private String profileImageUrl; // Firestore URL
    private String headerImageUrl;
    private long totalMessageCount;   // 전체 메시지 수
    private long unreadMessageCount;  // 숨김 아니고 답장 안 된 메시지 수
    private long replyCount;          // 계정주가 작성한 답장 수
    private boolean allowAnonymous;
    private boolean aiMode;
}
