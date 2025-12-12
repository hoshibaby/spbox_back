// src/main/java/org/jyr/postbox/dto/user/ChangePasswordRequestDTO.java
package org.jyr.postbox.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    private String currentPassword;
    private String newPassword;
}
