package org.jyr.postbox.dto.user;

import lombok.Data;

@Data
public class UserSignupDTO {
    private String userId;
    private String email;
    private String password;
    private String passwordCheck;
    private String nickname;
    private String addressId;
}
