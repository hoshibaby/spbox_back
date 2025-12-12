package org.jyr.postbox.dto.user;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String userId;
    private String password;
}
