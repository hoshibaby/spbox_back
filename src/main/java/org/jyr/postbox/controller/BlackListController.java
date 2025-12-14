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
@CrossOrigin(origins = "http://localhost:5173")
public class BlackListController {

    private final UserService userService;
    private final BlackListService blackListService;

    // 1) 내 블랙리스트 목록 조회
    // GET /api/settings/blacklist?userId=ororong1
    @GetMapping
    public ResponseEntity<List<BlackListDTO>> getMyBlackList(@RequestParam("userId") String userId) {
        User owner = userService.findByUserId(userId);
        return ResponseEntity.ok(blackListService.getBlackListForOwner(owner));
    }

    @DeleteMapping
    public ResponseEntity<?> unblock(@RequestParam("userId") String userId,
                                     @RequestParam("blockedUserId") Long blockedUserId) {
        User owner = userService.findByUserId(userId);
        blackListService.unblockUser(blockedUserId, owner);
        return ResponseEntity.ok("블랙리스트 해제 완료!");
    }
}
