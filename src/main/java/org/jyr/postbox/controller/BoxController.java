package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.box.BoxHeaderDTO;
import org.jyr.postbox.dto.box.MyBoxResponseDTO;
import org.jyr.postbox.dto.message.MessagePageDTO;
import org.jyr.postbox.service.BoxService;
import org.jyr.postbox.service.MessageService;
import org.jyr.postbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class BoxController {

    private final UserService userService;
    private final BoxService boxService;
    private final MessageService messageService;


    // 내 박스 전체 정보 + 메시지 요약 리스트
    @GetMapping("/me/box")
    public ResponseEntity<MyBoxResponseDTO> getMyBox(@RequestParam("userId") String userId) {
        User owner = userService.findByUserId(userId);
        MyBoxResponseDTO dto = messageService.getMyBox(owner);
        return ResponseEntity.ok(dto);
    }

     //공개 박스 헤더 정보
     // GET /api/q/{urlKey}/header
    @GetMapping("/q/{urlKey}/header")
    public ResponseEntity<BoxHeaderDTO> getBoxHeader(@PathVariable String urlKey) {
        BoxHeaderDTO dto = boxService.getBoxHeaderByUrlKey(urlKey);
        return ResponseEntity.ok(dto);
    }


    //공개 박스 메세지 목록(숨김제외)
    //    GET /api/q/{urlKey}/messages?page=&size=
    //    → MessageService.getPublicMessages(urlKey, page, size) 재사용

    @GetMapping("/q/{urlKey}/messages")
    public ResponseEntity<MessagePageDTO> getPublicMessages(
            @PathVariable String urlKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MessagePageDTO dto = messageService.getPublicMessages(urlKey, page, size);
        return ResponseEntity.ok(dto);
    }

    // =============================
    // 1) @userId 로 박스 헤더 조회
    //     GET /api/boxes/user/{userId}/header
    // =============================
    @GetMapping("/boxes/user/{userId}/header")
    public ResponseEntity<BoxHeaderDTO> getBoxHeaderByUserId(
            @PathVariable String userId
    ) {
        BoxHeaderDTO dto = boxService.getBoxHeaderByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    // =============================
    // 2) @userId 로 공개 메시지 목록 조회
    //     GET /api/boxes/user/{userId}/messages?page=&size=
    // =============================
    @GetMapping("/boxes/user/{userId}/messages")
    public ResponseEntity<MessagePageDTO> getPublicMessagesByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // userId -> Box -> urlKey 얻어서 기존 서비스 재사용
        var box = boxService.getBoxByOwnerUserId(userId);
        MessagePageDTO dto = messageService.getPublicMessages(box.getUrlKey(), page, size);
        return ResponseEntity.ok(dto);
    }

    // ✅ 박스: 익명(비회원) 글쓰기 허용 토글
    // PUT /api/me/box/anonymous?userId=ororong1&allowAnonymous=true
    @PutMapping("/me/box/anonymous")
    public ResponseEntity<Void> updateAllowAnonymous(
            @RequestParam("userId") String userId,
            @RequestParam("allowAnonymous") boolean allowAnonymous
    ) {
        boxService.updateAllowAnonymous(userId, allowAnonymous);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/settings/ai")
    public ResponseEntity<Void> updateAiMode(
            @RequestParam String userId,
            @RequestParam boolean enabled
    ) {
        boxService.updateAiMode(userId, enabled);
        return ResponseEntity.ok().build();
    }





}