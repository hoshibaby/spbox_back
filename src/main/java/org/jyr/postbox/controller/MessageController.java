package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.message.MessageCreateDTO;
import org.jyr.postbox.dto.message.MessageDetailDTO;
import org.jyr.postbox.dto.message.MessagePageDTO;
import org.jyr.postbox.dto.message.MessageUpdateRequestDTO;
import org.jyr.postbox.service.MessageService;
import org.jyr.postbox.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    // =========================
    // 1. 메시지 작성 (익명 / 로그인 / 박스 주인)
    // =========================
    @PostMapping("/message")
    public ResponseEntity<Long> createMessage(
            @RequestBody MessageCreateDTO dto,
            @RequestParam(value = "userId", required = false) Long userId // 로그인 유저 PK (선택)
    ) {

        User loginUserOrNull = null;
        if (userId != null) {
            loginUserOrNull = userService.findById(userId);
        }
        Long messageId = messageService.createMessage(dto, loginUserOrNull);
        // 201 + 생성된 메시지 PK 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(messageId);
    }

    // =========================
    // 1-1. 공개 박스 - 메시지 목록 조회 (익명/회원 모두 접근)
    // =========================
    @GetMapping("/boxes/{boxUrlKey}/messages")
    public ResponseEntity<MessagePageDTO> getPublicMessages(
            @PathVariable String boxUrlKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MessagePageDTO dto = messageService.getPublicMessages(boxUrlKey, page, size);
        return ResponseEntity.ok(dto);
    }


    // =========================
    // 2. MyBox - 메시지 목록 조회
    // =========================
    @GetMapping("/me/messages")
    public ResponseEntity<MessagePageDTO> myMessages(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User owner = userService.findById(userId);
        MessagePageDTO dto = messageService.getMessagesForOwner(owner, page, size);
        return ResponseEntity.ok(dto);
    }

    // =========================
    // 2-1. MyBox - "답변 있는 메시지" 목록 조회
    // =========================
    @GetMapping("/me/messages/answered")
    public ResponseEntity<MessagePageDTO> myAnsweredMessages(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User owner = userService.findById(userId);
        MessagePageDTO dto = messageService.getAnsweredMessagesForOwner(owner, page, size);
        return ResponseEntity.ok(dto);
    }




    // =========================
    // 3. MyBox - 메시지 상세 조회
    // =========================
    @GetMapping("/me/messages/{id}")
    public ResponseEntity<MessageDetailDTO> getDetail(
            @PathVariable Long id,
            @RequestParam("userId") Long userId
    ) {
        User owner = userService.findById(userId);
        MessageDetailDTO dto = messageService.getMessageDetailForOwner(id, owner);
        return ResponseEntity.ok(dto);
    }

    // =========================
    // 4. 답장 달기 / 삭제하기
    //    - body 가 비어 있으면 "답장 삭제"
    // =========================
    @PatchMapping(
            value = "/me/messages/{id}/reply",
            consumes = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<?> reply(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestBody(required = false) String replyContent
    ) {
        User owner = userService.findById(userId);

        String trimmed = (replyContent == null) ? "" : replyContent.trim();

        // 빈 문자열이면 답장 삭제
        if (trimmed.isEmpty()) {
            messageService.clearReply(id, owner);
            return ResponseEntity.noContent().build();
        }

        // 내용이 있으면 답장 저장/수정
        messageService.replyToMessage(id, trimmed, owner);
        return ResponseEntity.ok("답변 완료!");
    }

    // =========================
    // 5. 메시지 숨김 처리 (박스 주인 전용)
    // =========================
    @PatchMapping("/me/messages/{id}/hide")
    public ResponseEntity<?> hide(
            @PathVariable Long id,
            @RequestParam("userId") Long userId
    ) {
        User owner = userService.findById(userId);
        messageService.hideMessage(id, owner);
        return ResponseEntity.ok("숨김 처리 완료!");
    }

    // =========================
    // 6. 블랙리스트 + 숨김 (로그인 회원만 블랙리스트 가능)
    // =========================
    @PostMapping("/me/messages/{id}/blacklist")
    public ResponseEntity<?> blacklistByMessage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId
    ) {
        User owner = userService.findById(userId);
        messageService.blacklistUserByMessage(id, owner);
        return ResponseEntity.ok("블랙리스트 설정 및 메시지 숨김 완료!");
    }

    // =========================
    // 7. 원본 메시지 수정
    //    - 내 박스에 내가 쓴 메시지만 수정 가능
    // =========================
    @PutMapping("/me/messages/{id}")
    public ResponseEntity<?> updateMessage(
            @PathVariable Long id,
            @RequestBody MessageUpdateRequestDTO dto
    ) {
        User loginUser = userService.findById(dto.getUserId()); // dto.userId도 Long PK
        messageService.updateMessage(id, dto.getContent(), loginUser);
        return ResponseEntity.ok("메시지 수정 완료");
    }

    // =========================
    // 8. 원본 메시지 삭제
    //    - 내 박스에 내가 쓴 메시지만 삭제 가능
    // =========================
    @DeleteMapping("/me/messages/{id}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId
    ) {
        User loginUser = userService.findById(userId);
        messageService.deleteMessage(id, loginUser);
        return ResponseEntity.ok("메시지 삭제 완료");
    }


}
