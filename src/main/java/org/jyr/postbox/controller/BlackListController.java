package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.user.BlackListDTO;
import org.jyr.postbox.service.BlackListService;
import org.jyr.postbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/blacklist")
public class BlackListController {

    private final UserService userService;
    private final BlackListService blackListService;

    // 1) 내 블랙리스트 목록 조회
    @GetMapping
    public ResponseEntity<List<BlackListDTO>> getMyBlackList(
            @RequestParam("ownerEmail") String ownerEmail
    ) {
        User owner = userService.findByEmail(ownerEmail);
        List<BlackListDTO> list = blackListService.getBlackListForOwner(owner);
        return ResponseEntity.ok(list);
    }

    // 2) 특정 유저 차단 해제
    @DeleteMapping
    public ResponseEntity<?> unblock(
            @RequestParam("ownerEmail") String ownerEmail,
            @RequestParam("blockedUserId") Long blockedUserId
    ) {
        User owner = userService.findByEmail(ownerEmail);
        blackListService.unblockUser(blockedUserId, owner);
        return ResponseEntity.ok("블랙리스트 해제 완료!");
    }
}

