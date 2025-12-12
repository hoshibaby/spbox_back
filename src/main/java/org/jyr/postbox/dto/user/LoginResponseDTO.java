package org.jyr.postbox.dto.user;

import lombok.Data;
import org.jyr.postbox.domain.User;

@Data
public class LoginResponseDTO {

    private Long id;
    private String userId;
    private String email;
    private String nickname;
    private String role;
    private String token;
    private String addressId;
    private String boxUrlKey;


    public LoginResponseDTO(User user, String token) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.token = token;
        this.role = user.getRole().name();
        this.addressId = user.getAddressId();
        this.boxUrlKey = boxUrlKey;
    }
}