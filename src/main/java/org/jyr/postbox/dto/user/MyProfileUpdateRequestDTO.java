package org.jyr.postbox.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyProfileUpdateRequestDTO {

    private String nickname;
    private String profileImageUrl;
    private String headerImageUrl;

    private String todayMessage;          // ✅ 추가: 오늘 한마디

    private Boolean aiConsultingEnabled;
}
