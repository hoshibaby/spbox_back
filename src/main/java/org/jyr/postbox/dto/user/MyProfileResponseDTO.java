package org.jyr.postbox.dto.user;

import lombok.Builder;
import lombok.Getter;
import org.jyr.postbox.domain.User;

@Getter
@Builder
public class MyProfileResponseDTO {

    private Long id;
    private String email;
    private String userId;           // 로그인 ID
    private String addressId;        // @로 쓰는 ID

    private String nickname;
    private String profileImageUrl;
    private String headerImageUrl;

    private String todayMessage;     // ✅ 오늘 한마디
    private boolean aiConsultingEnabled;

    public static MyProfileResponseDTO from(User user) {
        return MyProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userId(user.getUserId())
                .addressId(user.getAddressId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .headerImageUrl(user.getHeaderImageUrl())
                .todayMessage(user.getTodayMessage()) //
                .aiConsultingEnabled(user.isAiConsultingEnabled())
                .build();
    }
}

