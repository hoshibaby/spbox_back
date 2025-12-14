package org.jyr.postbox.dto.admin;

import lombok.Builder;
import lombok.Getter;
import org.jyr.postbox.domain.UserStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserListItemDTO {
    private Long id;
    private String userId;
    private String email;
    private String nickname;
    private String addressId;
    private String role;      // "USER" / "ADMIN"
    private UserStatus status; // ACTIVE / BANNED
    private LocalDateTime createdAt;
}
