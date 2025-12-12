package org.jyr.postbox.controller;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.dto.user.MyProfileResponseDTO;
import org.jyr.postbox.dto.user.MyProfileUpdateRequestDTO;
import org.jyr.postbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<MyProfileResponseDTO> getMyProfile(
            @RequestParam("userId") String userId  // 🔥 String으로 변경
    ) {
        MyProfileResponseDTO dto = userService.getMyProfile(userId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/profile")
    public ResponseEntity<MyProfileResponseDTO> updateMyProfile(
            @RequestParam("userId") String userId,          // 🔥 String
            @RequestBody MyProfileUpdateRequestDTO dto
    ) {
        MyProfileResponseDTO updated = userService.updateMyProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

//    @DeleteMapping
//    public ResponseEntity<Void> deleteMyAccount(
//            @RequestParam("userId") String userId
//    ) {
//        userService.deleteUserByUserId(userId);
//        return ResponseEntity.ok().build();
//    }
}
