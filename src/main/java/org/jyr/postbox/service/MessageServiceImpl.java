package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.*;
import org.jyr.postbox.dto.box.BoxHeaderDTO;
import org.jyr.postbox.dto.box.MyBoxResponseDTO;
import org.jyr.postbox.dto.message.MessageCreateDTO;
import org.jyr.postbox.dto.message.MessageDetailDTO;
import org.jyr.postbox.dto.message.MessagePageDTO;
import org.jyr.postbox.dto.message.MessageSummaryDTO;
import org.jyr.postbox.exception.BlockedUserException;
import org.jyr.postbox.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final BoxRepository boxRepository;
    private final MessageRepository messageRepository;
    private final BlackListRepository blackListRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final org.jyr.postbox.ai.service.AiReplyService aiReplyService;
    private final UserRepository userRepository;

    // =============== 메시지 작성 ===============
    @Override
    @Transactional
    public Long createMessage(MessageCreateDTO dto, User loginUserOrNull) {

        // 0) DTO 방어
        if (dto == null || dto.getBoxUrlKey() == null || dto.getBoxUrlKey().isBlank()) {
            throw new IllegalArgumentException("boxUrlKey가 비어 있습니다.");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어 있습니다.");
        }

        // 1) 박스 찾기
        Box box = boxRepository.findByUrlKey(dto.getBoxUrlKey())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 박스입니다."));

        // 2) 로그인 필수 박스 → 익명 차단
        if (!box.isAllowAnonymous() && loginUserOrNull == null) {
            throw new IllegalStateException("이 박스는 로그인한 회원만 메시지를 남길 수 있습니다.");
        }

        // ✅ 2-1) 블랙리스트 검사: 로그인 유저가 차단 상태면 작성 불가
        if (loginUserOrNull != null) {
            boolean blocked = blackListRepository.existsByBoxAndBlockedUser(box, loginUserOrNull);
            if (blocked) {
                throw new BlockedUserException("차단된 사용자입니다. 메시지를 보낼 수 없습니다.");
            }
        }

        // 3) 작성자 결정
        // 정책:
        // - 박스 주인 → OWNER
        // - 그 외(로그인/비로그인) → ANONYMOUS
        AuthorType authorType = AuthorType.ANONYMOUS;
        User authorUser = null;

        if (loginUserOrNull != null) {
            authorUser = loginUserOrNull;

            if (loginUserOrNull.getId().equals(box.getOwner().getId())) {
                authorType = AuthorType.OWNER;
            }
        }

        // 4) 메시지 저장
        Message saved = messageRepository.save(
                Message.builder()
                        .box(box)
                        .content(dto.getContent().trim())
                        .authorUser(authorUser)
                        .authorType(authorType)
                        .privateMessage(dto.isPrivateMessage())
                        .build()
        );
        // ✅ 4-1) AI 자동답변 (AI 모드 ON + "박스 주인" + "askAi 토글 ON" 일 때만)
        if (box.isAiMode()
                && authorType == AuthorType.OWNER
                && dto.isAskAi()) {

            try {
                String aiText = aiReplyService.generateReply(saved);
                saved.writeReply(aiText, ReplyAuthorType.AI);
                messageRepository.save(saved); // ✅ replyContent 반영
            } catch (Exception e) {
                // MVP: AI 실패해도 메시지 작성은 성공해야 함
                // log.warn("AI reply failed. messageId={}", saved.getId(), e);
            }
        }


        // 5) 알림 생성 (박스 주인에게)
        String alertMessage =
                (authorType == AuthorType.OWNER)
                        ? "박스 주인이 메시지를 남겼어요."
                        : "새로운 메시지가 도착했어요!";

        notificationRepository.save(
                Notification.builder()
                        .targetUser(box.getOwner())
                        .type(NotificationType.COMMENT) // 프로젝트 enum에 맞게
                        .alertMessage(alertMessage)
                        .message(saved)
                        .linkUrl("/me/messages/" + saved.getId())
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return saved.getId();
    }




    // =============== MyBox 메시지 리스트(페이지) ===============
    @Override
    @Transactional(readOnly = true)
    public MessagePageDTO getMessagesForOwner(User owner, int page, int size) {

        Box box = boxRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("해당 유저의 박스가 없습니다."));

        // ✅ 1) 박스 헤더 DTO 생성 (핵심)
        BoxHeaderDTO boxHeaderDTO = BoxHeaderDTO.builder()
                .boxId(box.getId())
                .boxTitle(box.getTitle())
                .urlKey(box.getUrlKey())
                .ownerName(owner.getNickname())
                .profileImageUrl(owner.getProfileImageUrl())
                .headerImageUrl(owner.getHeaderImageUrl())  // ✅ BoxHeaderDTO에 필드 추가되어 있어야 함
                .allowAnonymous(box.isAllowAnonymous())
                .aiMode(box.isAiMode())
                .build();

        PageRequest pageable = PageRequest.of(page, size);
        Page<Message> result = messageRepository
                .findByBoxOrderByCreatedAtDesc(box, pageable);

        return MessagePageDTO.builder()
                .box(boxHeaderDTO) // ✅ 2) MessagePageDTO에 box 넣기 (핵심)
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .content(
                        result.getContent().stream()
                                .map(this::toSummaryDTO)
                                .collect(Collectors.toList())
                )
                .allowAnonymous(box.isAllowAnonymous())
                .build();
    }


    // =============== MyBox "답변 있는 메시지" 리스트(페이지) ===============
    @Override
    @Transactional(readOnly = true)
    public MessagePageDTO getAnsweredMessagesForOwner(User owner, int page, int size) {

        Box box = boxRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("해당 유저의 박스가 없습니다."));

        // 여기 추가: boxHeaderDTO 생성
        BoxHeaderDTO boxHeaderDTO = BoxHeaderDTO.builder()
                .boxId(box.getId())
                .boxTitle(box.getTitle())
                .urlKey(box.getUrlKey())
                .ownerName(owner.getNickname())
                .profileImageUrl(owner.getProfileImageUrl())
                .headerImageUrl(owner.getHeaderImageUrl())   // BoxHeaderDTO에 필드 있어야 함
                .allowAnonymous(box.isAllowAnonymous())
                .aiMode(box.isAiMode())
                .build();

        PageRequest pageable = PageRequest.of(page, size);

        Page<Message> result = messageRepository
                .findByBoxAndReplyContentIsNotNullOrderByCreatedAtDesc(box, pageable);

        return MessagePageDTO.builder()
                .box(boxHeaderDTO) // 이제 빨간줄 사라짐
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .content(
                        result.getContent().stream()
                                .map(this::toSummaryDTO)
                                .collect(Collectors.toList())
                )
                .allowAnonymous(box.isAllowAnonymous())
                .build();
    }




    // =============== 공개 메시지 리스트(페이지) ===============
    @Override
    @Transactional(readOnly = true)
    public MessagePageDTO getPublicMessages(String boxUrlKey, int page, int size) {

        Box box = boxRepository.findByUrlKey(boxUrlKey)
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다."));

        // ✅ 정렬을 Pageable에 명시(안정성 ↑)
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 숨김 제외 + 최신순
        Page<Message> result = messageRepository
                .findByBoxAndHiddenFalseAndSystemMessageFalseAndPrivateMessageFalseOrderByCreatedAtDesc(
                        box, pageable
                );


        return MessagePageDTO.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .allowAnonymous(box.isAllowAnonymous()) // ✅ 핵심: 공개 페이지에서 글쓰기 조건 판단용
                .content(
                        result.getContent().stream()
                                .map(this::toSummaryDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }



    // =============== 답장 / 숨김 / 블랙리스트 ===============
    @Override
    public void replyToMessage(Long messageId, String replyContent, User owner) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        if (!message.getBox().getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("이 메시지에 답변할 권한이 없습니다.");
        }

        // 1) 답장 쓰기
        message.writeReply(replyContent);

        // 2) 알림 대상 = 원래 메시지를 쓴 회원 (익명이면 null)
        User authorUser = message.getAuthorUser();

        // 🔥 작성자가 존재하고 + "오너와 다른 사람"일 때만 알림 생성
        if (authorUser != null && !authorUser.getId().equals(owner.getId())) {
            notificationService.createNotification(
                    authorUser.getId(),                     // targetUserId
                    NotificationType.OWNER_REPLY,
                    "답글 작성한 메세지에 답글이 달렸어요",
                    "/me/messages/" + message.getId(),      // 클릭 시 이동 링크
                    message.getId()
            );
        }
    }

    @Transactional
    public void clearReply(Long messageId, User owner) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));

        // 이 메시지가 진짜 이 사람(post box 주인)의 것인지 확인
        if (!message.getBox().getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("내 박스의 메시지가 아닙니다.");
        }

        // 답장 내용/시간 비우기
        message.setReplyContent(null);
        message.setReplyCreatedAt(null);

        // 🔥 지금은 OWNER 답변만 있으니까, 이걸 지우면 "답변 없음" 상태
        message.setHasAnyAnswer(false);

        messageRepository.save(message);
    }


    @Override
    public void hideMessage(Long messageId, User owner) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        if (!message.getBox().getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("이 메시지를 숨길 권한이 없습니다.");
        }

        message.hide();
    }

    @Transactional
    @Override
    public void blacklistUserByMessage(Long messageId, User owner) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        Box box = message.getBox();

        // 박스 주인 검증
        if (!box.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("이 박스의 주인이 아닙니다.");
        }

        User blockedUser = message.getAuthorUser();

        // 1) 익명인 경우 - 유저 차단 불가 → 메시지만 숨기기
        if (blockedUser == null) {
            message.hide();
            messageRepository.save(message);
            return;
        }

        // 2) 블랙리스트 저장 (중복 방지)
        if (!blackListRepository.existsByBoxAndBlockedUser(box, blockedUser)) {
            blackListRepository.save(
                    BlackList.builder()
                            .box(box)
                            .blockedUser(blockedUser)
                            .build()
            );
        }

        // 3) 원본 메시지 숨김
        message.hide();
        messageRepository.save(message);

    }


    // =========================
    // 내부 변환 메서드들
    // =========================

    private String calculateAuthorLabel(Message m) {
        switch (m.getAuthorType()) {

            case ANONYMOUS:
                return "익명";

            case OWNER:
            default:
                return m.getAuthorUser() != null
                        ? m.getAuthorUser().getNickname()
                        : "계정주";
        }
    }



    // =============== 내부 변환 메서드 ===============
    private MessageSummaryDTO toSummaryDTO(Message m) {

        boolean fromOwner = (m.getAuthorType() == AuthorType.OWNER);

        return MessageSummaryDTO.builder()
                .id(m.getId())
                .shortContent(shorten(m.getContent(), 20))
                .fromOwner(fromOwner)
                .hasReply(m.getReplyContent() != null)
                .hidden(m.isHidden())

                .createdAt(m.getCreatedAt())
                .authorType(m.getAuthorType().name())
                .authorLabel(calculateAuthorLabel(m))

                .build();
    }

    private String shorten(String content, int max) {
        if (content == null) return "";
        if (content.length() <= max) return content;
        return content.substring(0, max) + "...";
    }

    @Override
    @Transactional(readOnly = true)
    public MessageDetailDTO getMessageDetailForOwner(Long messageId, User loginUser) {

        // 1) 메시지 조회
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        Long loginId = loginUser.getId();
        Long ownerId = m.getBox().getOwner().getId();
        Long authorId = (m.getAuthorUser() != null) ? m.getAuthorUser().getId() : null;

        // 🔥 권한 체크: 박스 주인 OR 작성자만 허용
        if (!loginId.equals(ownerId) &&
                (authorId == null || !loginId.equals(authorId))) {
            throw new IllegalStateException("이 메시지에 접근할 권한이 없습니다.");
        }

        // 3) 작성자가 박스 주인인지 여부
        boolean fromOwner = (m.getAuthorType() == AuthorType.OWNER);

        // 4) DTO 로 변환해서 리턴
        return MessageDetailDTO.builder()
                .id(m.getId())
                .content(m.getContent())
                .fromOwner(fromOwner)
                .hidden(m.isHidden())
                .createdAt(m.getCreatedAt())
                .replyContent(m.getReplyContent())
                .replyCreatedAt(m.getReplyCreatedAt())
                .authorUserId(m.getAuthorUser() != null ? m.getAuthorUser().getId() : null)
                .authorType(m.getAuthorType().name())
                .boxOwnerId(m.getBox().getOwner().getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MyBoxResponseDTO getMyBox(User owner) {

        // 1) 박스 찾기
        Box box = boxRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("박스가 없습니다."));

        // 2) 박스 헤더 정보용 카운트 값 계산
        long totalMessageCount  = messageRepository.countByBox(box);
        long unreadMessageCount = messageRepository.countByBoxAndHiddenFalse(box);
        long replyCount         = messageRepository.countByBoxAndReplyContentIsNotNull(box);

        // 3) 박스 헤더 DTO 생성
        BoxHeaderDTO boxHeaderDTO = BoxHeaderDTO.builder()
                .boxId(box.getId())
                .boxTitle(box.getTitle())
                .urlKey(box.getUrlKey())
                .ownerName(owner.getNickname())          // 🔥 최신 nickname 반영
                .profileImageUrl(owner.getProfileImageUrl())
                .totalMessageCount(totalMessageCount)
                .unreadMessageCount(unreadMessageCount)
                .replyCount(replyCount)
                .allowAnonymous(box.isAllowAnonymous())
                .build();

        // 4) 메시지 요약 리스트
        List<MessageSummaryDTO> summaryList = messageRepository
                .findByBoxOrderByCreatedAtDesc(box)
                .stream()
                .map(this::toSummaryDTO)
                .toList();

        // 5) ⭐ MyBoxResponseDTO 전부 채워서 리턴
        return MyBoxResponseDTO.builder()
                .nickname(owner.getNickname())
                .profileImageUrl(owner.getProfileImageUrl())
                .headerImageUrl(owner.getHeaderImageUrl())
                .box(boxHeaderDTO)
                .messages(summaryList)
                .allowAnonymous(box.isAllowAnonymous())
                .aiMode(box.isAiMode())
                .build();
    }


    @Override
    public void updateMessage(Long messageId, String newContent, User loginUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // 1) 작성자가 로그인 유저여야 하고
        // 2) 그 박스의 주인도 로그인 유저여야 한다 = 내 박스에 내가 쓴 글만 수정 가능
        if (message.getAuthorUser() == null ||
                !message.getAuthorUser().getId().equals(loginUser.getId()) ||
                !message.getBox().getOwner().getId().equals(loginUser.getId())) {
            throw new IllegalStateException("내 박스에 내가 쓴 메시지만 수정할 수 있습니다.");
        }

        message.setContent(newContent);
    }

//    @Override
//    public void deleteMessage(Long messageId, User loginUser) {
//        Message message = messageRepository.findById(messageId)
//                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
//
//        if (message.getAuthorUser() == null ||
//                !message.getAuthorUser().getId().equals(loginUser.getId()) ||
//                !message.getBox().getOwner().getId().equals(loginUser.getId())) {
//            throw new IllegalStateException("내 박스에 내가 쓴 메시지만 삭제할 수 있습니다.");
//        }
//
//        messageRepository.delete(message);
//    }

    @Override
    public void deleteMessage(Long messageId, User loginUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // ✅ 박스 주인만 삭제 가능 (받은 메시지 관리)
        if (!message.getBox().getOwner().getId().equals(loginUser.getId())) {
            throw new IllegalStateException("이 메시지를 삭제할 권한이 없습니다.");
        }

        messageRepository.delete(message);
    }


    //Ai
    @Transactional
    @Override
    public void generateAiReply(Long messageId, String loginUserId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        User owner = userRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 박스 주인 검증
        if (!message.getBox().getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("AI 답변 생성 권한이 없습니다.");
        }

        // AI 기능 ON 확인(유료/권한)
        if (!message.getBox().isAiMode()) {
            throw new IllegalStateException("AI 기능이 활성화되지 않았습니다.");
        }

        // 이미 답변 있으면 막기(원하면 덮어쓰기 정책으로 변경 가능)
        if (message.getReplyContent() != null && !message.getReplyContent().isBlank()) {
            throw new IllegalStateException("이미 답변이 존재합니다.");
        }

        String aiText = aiReplyService.generateReply(message);
        message.writeReply(aiText, ReplyAuthorType.AI);
        messageRepository.save(message);
    }


}