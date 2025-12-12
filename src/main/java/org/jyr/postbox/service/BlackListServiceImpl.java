package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.BlackList;
import org.jyr.postbox.domain.Box;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.user.BlackListDTO;
import org.jyr.postbox.repository.BlackListRepository;
import org.jyr.postbox.repository.BoxRepository;
import org.jyr.postbox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlackListServiceImpl implements BlackListService {

    private final BlackListRepository blackListRepository;
    private final BoxRepository boxRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BlackListDTO> getBlackListForOwner(User owner) {

        // 1) 내 박스 찾기
        Box box = boxRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("박스를 찾을 수 없습니다."));

        // 2) 블랙리스트 엔티티 조회
        List<BlackList> list = blackListRepository.findByBox(box);

        // 3) DTO 변환
        return list.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void unblockUser(Long blockedUserId, User owner) {

        // 1) 내 박스 찾기
        Box box = boxRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("박스를 찾을 수 없습니다."));

        // 2) 차단된 유저 조회
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단된 유저를 찾을 수 없습니다."));

        // 3) 실제 삭제 (레포 메서드 사용)
        blackListRepository.deleteByBoxAndBlockedUser(box, blockedUser);
    }

    // ================== private 메서드 ==================

    private BlackListDTO toDTO(BlackList entity) {
        User blocked = entity.getBlockedUser();

        return BlackListDTO.builder()
                .id(entity.getId())
                .blockedUserId(blocked.getId())
                .blockedNickname(blocked.getNickname())
                .blockedEmail(blocked.getEmail())
                .build();
    }
}
