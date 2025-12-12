package org.jyr.postbox.dto.user;

import lombok.Builder;
import lombok.Getter;
import org.jyr.postbox.domain.User;

@Getter
@Builder
public class UserDTO {

    private Long id;

    private String email;

    private String userId;      // 로그인 ID
    private String addressId;   // 박스 주소 ID

    private String nickname;

    private String profileImageUrl;
    private String headerImageUrl;

    private boolean aiConsultingEnabled;

    // 필요하면 counselorMode / counselorName 도 추가 가능
    // private CounselorMode counselorMode;
    // private String counselorName;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userId(user.getUserId())
                .addressId(user.getAddressId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .headerImageUrl(user.getHeaderImageUrl())
                .aiConsultingEnabled(user.isAiConsultingEnabled())
                .build();
    }
}
