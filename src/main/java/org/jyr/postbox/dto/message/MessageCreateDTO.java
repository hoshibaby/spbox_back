package org.jyr.postbox.dto.message;

import lombok.Data;

@Data
public class MessageCreateDTO {

    // 어떤 박스에 쓰는지 (예: abc123ef)
    private String boxUrlKey;
    // 본문 내용
    private String content;
    private Boolean fromOwner;
}