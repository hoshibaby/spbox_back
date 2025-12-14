package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.domain.UserRole;
import org.jyr.postbox.domain.UserStatus;
import org.jyr.postbox.dto.admin.AdminUserListItemDTO;
import org.jyr.postbox.repository.UserRepository;
import org.jyr.postbox.service.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    public List<AdminUserListItemDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> AdminUserListItemDTO.builder()
                        .id(u.getId())
                        .userId(u.getUserId())
                        .email(u.getEmail())
                        .nickname(u.getNickname())
                        .addressId(u.getAddressId())
                        .role(u.getRole().name()) // 필드명 맞춰서
                        .status(u.getStatus())
                        .createdAt(u.getCreatedAt()) // 없으면 제거
                        .build())
                .toList();
    }


    @Override
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        user.setStatus(UserStatus.BANNED);
    }

    @Override
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        user.setStatus(UserStatus.ACTIVE);
    }

    //admin 부여
    @Override
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        user.setRole(UserRole.ADMIN);
    }

    @Override
    public void demoteToUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        user.setRole(UserRole.USER);
    }


}
