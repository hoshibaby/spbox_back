package org.jyr.postbox.dto.box;

import lombok.*;
import org.jyr.postbox.dto.message.MessageSummaryDTO;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class MyBoxResponseDTO {

    private String nickname;
    private String profileImageUrl;
    private String headerImageUrl;
    private BoxHeaderDTO box;                       // 박스 헤더 정보
    private List<MessageSummaryDTO> messages; // 메시지 요약 리스트
    private boolean allowAnonymous;
}
