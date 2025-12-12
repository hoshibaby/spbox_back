package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.Box;
import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.box.BoxHeaderDTO;
import org.jyr.postbox.repository.BoxRepository;
import org.jyr.postbox.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoxServiceImpl implements BoxService {

    private final BoxRepository boxRepository;
    private final MessageRepository messageRepository;

    @Override
    public Box createBoxForUser(User user) {

        String urlKey = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        Box box = Box.builder()
                .owner(user)
                .urlKey(urlKey)
                .title(user.getNickname() + "님의 SecretBox")
                .build();

        return boxRepository.save(box);
    }

    @Override
    public BoxHeaderDTO getBoxHeaderByUrlKey(String urlKey) {

        // 1) 박스 찾기
        Box box = boxRepository.findByUrlKey(urlKey)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));

        // 2) 메시지 카운트 구하기
        long totalMessageCount  = messageRepository.countByBox(box);
        long unreadMessageCount = messageRepository.countByBoxAndHiddenFalse(box);
        long replyCount         = messageRepository.countByBoxAndReplyContentIsNotNull(box);

        // 3) 헤더 DTO 생성
        return BoxHeaderDTO.builder()
                .boxId(box.getId())
                .boxTitle(box.getTitle())
                .urlKey(box.getUrlKey())
                .ownerName(box.getOwner().getNickname())
                .profileImageUrl(box.getOwner().getProfileImageUrl())
                .totalMessageCount(totalMessageCount)
                .unreadMessageCount(unreadMessageCount)
                .replyCount(replyCount)
                .build();
    }

    @Override
    public BoxHeaderDTO getBoxHeaderByUserId(String userId) {
        Box box = boxRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));
        return buildHeaderDTO(box);
    }

    @Override
    public Box getBoxByOwnerUserId(String userId) {
        return boxRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));
    }

    // 공통 로직 분리: 헤더 DTO 만드는 부분
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
                .totalMessageCount(totalMessageCount)
                .unreadMessageCount(unreadMessageCount)
                .replyCount(replyCount)
                .build();
    }
}
