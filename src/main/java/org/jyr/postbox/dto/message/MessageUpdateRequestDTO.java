package org.jyr.postbox.dto.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageUpdateRequestDTO {

    private Long userId;
    private String content;
}
