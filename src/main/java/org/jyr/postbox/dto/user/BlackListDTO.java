package org.jyr.postbox.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlackListDTO {

    private Long id;                 // 블랙리스트 엔트리 PK
    private Long blockedUserId;      // 차단된 유저 PK
    private String blockedNickname;  // 차단된 유저 닉네임
    private String blockedEmail;     // 차단된 유저 이메일
}
