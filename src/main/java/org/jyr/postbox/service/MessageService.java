package org.jyr.postbox.service;

import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.box.MyBoxResponseDTO;
import org.jyr.postbox.dto.message.MessageCreateDTO;
import org.jyr.postbox.dto.message.MessageDetailDTO;
import org.jyr.postbox.dto.message.MessagePageDTO;

public interface MessageService {

    // 1) 메시지 작성 (익명 or 주인)
    Long createMessage(MessageCreateDTO dto, User loginUserOrNull);


    // 2) MyBox - 메시지 리스트(페이지)
    MessagePageDTO getMessagesForOwner(User owner, int page, int size);

    // 2-1) MyBox - "답변 있는 메시지" 리스트(페이지)
    MessagePageDTO getAnsweredMessagesForOwner(User owner, int page, int size);

    // 3) 공개 메시지 리스트(페이지)
    MessagePageDTO getPublicMessages(String boxUrlKey, int page, int size);

    // 4) 답장 달기
    void replyToMessage(Long messageId, String replyContent, User owner);
    void clearReply(Long messageId, User owner);

    // 5) 숨김 처리
    void hideMessage(Long messageId, User owner);

    // 6) 블랙리스트 + 숨김
    void blacklistUserByMessage(Long messageId, User owner);

    // 7) MyBox - 메시지 상세 보기
    MessageDetailDTO getMessageDetailForOwner(Long messageId, User owner);

    // 8) MyBox 통합 응답 (박스 정보 + 메시지 리스트)
    MyBoxResponseDTO getMyBox(User owner);

    // 9) 로그인 유저가 타 계정에 남긴 댓글 수정 삭제
    void updateMessage(Long messageId, String newContent, User loginUser);
    void deleteMessage(Long messageId, User loginUser);




}
