package org.jyr.postbox.dto.message;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessagePageDTO {

    private int page;              // 현재 페이지 번호
    private int size;              // 한 페이지당 사이즈
    private int totalPages;        // 전체 페이지 수
    private long totalElements;    // 전체 데이터 개수

    private boolean allowAnonymous; //로그인 한 사람만 글쓰기

    // 화면에 뿌릴 메시지 요약 리스트
    private List<MessageSummaryDTO> content;
}