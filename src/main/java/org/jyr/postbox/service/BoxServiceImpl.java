// src/main/java/org/jyr/postbox/service/BoxServiceImpl.java
package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.Box;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.box.BoxHeaderDTO;
import org.jyr.postbox.repository.BoxRepository;
import org.jyr.postbox.repository.MessageRepository;
import org.jyr.postbox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BoxServiceImpl implements BoxService {

    private final BoxRepository boxRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // =========================
    // 1) 회원가입 시 박스 생성
    // =========================
    @Override
    @Transactional
    public Box createBoxForUser(User user) {

        String urlKey = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        Box box = Box.builder()
                .owner(user)
                .urlKey(urlKey)
                .title(user.getNickname() + "님의 SecretBox")
                .allowAnonymous(true)
                .aiMode(false)
                .build();

        return boxRepository.save(box);
    }

    // =========================
    // 2) urlKey로 헤더 DTO 조회 (공개 페이지 등)
    // =========================
    @Override
    @Transactional(readOnly = true)
    public BoxHeaderDTO getBoxHeaderByUrlKey(String urlKey) {
        Box box = boxRepository.findByUrlKey(urlKey)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));
        return buildHeaderDTO(box);
    }


    // =========================
    // 3) userId로 헤더 DTO 조회 (MyBox / 글쓰기 페이지 등)
    // =========================
    @Override
    @Transactional(readOnly = true)
    public BoxHeaderDTO getBoxHeaderByUserId(String userId) {
        Box box = boxRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));
        return buildHeaderDTO(box);
    }

    // =========================
    // 4) userId로 Box 엔티티 조회
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Box getBoxByOwnerUserId(String userId) {
        return boxRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));
    }

    // =========================
    // 5) 비회원 허용 토글 저장
    // =========================
    @Override
    @Transactional
    public void updateAllowAnonymous(String userId, boolean allowAnonymous) {
        Box box = boxRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다. userId=" + userId));

        box.setAllowAnonymous(allowAnonymous);
        // @Transactional 이면 더티체킹으로 반영됨. 그래도 확실히 하려면 save 해도 OK.
        boxRepository.save(box);
    }

    // =========================
    // 6) AI 모드 토글 저장 (DB 반영 핵심)
    // =========================
    @Override
    @Transactional
    public void updateAiMode(String userId, boolean enabled) {

        // userId는 "문자열 아이디(ororong1)" 기준이라고 가정 (너 Settings에서 userId=ororong1로 보내는 중)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음. userId=" + userId));

        Box box = boxRepository.findByOwner(user)
                .orElseThrow(() -> new IllegalStateException("박스 없음. userId=" + userId));

        box.setAiMode(enabled);
        boxRepository.save(box);
    }


    // =========================
    // 공통: 헤더 DTO 생성
    // =========================
    private BoxHeaderDTO buildHeaderDTO(Box box) {

        long totalMessageCount  = messageRepository.countByBox(box);
        long unreadMessageCount = messageRepository.countByBoxAndHiddenFalse(box);
        long replyCount         = messageRepository.countByBoxAndReplyContentIsNotNull(box);

        return BoxHeaderDTO.builder()
                .boxId(box.getId())
                .boxTitle(box.getTitle())
                .urlKey(box.getUrlKey())
                .ownerName(box.getOwner().getNickname())
                .profileImageUrl(box.getOwner().getProfileImageUrl())
                .headerImageUrl(box.getOwner().getHeaderImageUrl())
                .totalMessageCount(totalMessageCount)
                .unreadMessageCount(unreadMessageCount)
                .replyCount(replyCount)
                .allowAnonymous(box.isAllowAnonymous())
                .aiMode(box.isAiMode())
                .build();
    }
}
