package org.jyr.postbox.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.domain.*;
import org.jyr.postbox.dto.user.*;
import org.jyr.postbox.repository.*;
import org.jyr.postbox.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // =========================
    // 의존성
    // =========================
    private final BoxService boxService;
    private final UserRepository userRepository;
    private final BoxRepository boxRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final BlackListRepository blackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // =========================
    // 회원가입 / 로그인
    // =========================

    @Override
    @Transactional
    public Long signup(UserSignupDTO dto) {

        // 0) 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 1-1) userId(로그인 ID) 중복 체크
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2) 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(dto.getPassword());

        // 3) User 엔티티 생성
        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(encodedPw)
                .nickname(dto.getNickname())
                .role(UserRole.USER)
                .build();

        // 4) addressId 결정
        String addressId = dto.getAddressId();

        if (addressId == null || addressId.isBlank()) {
            // 입력 안 한 경우 → 닉네임 기반 자동 생성
            String base = makeSlugFromNickname(dto.getNickname());
            addressId = generateUniqueAddressId(base);
        } else {
            // 직접 입력한 경우 → 정규화 + 검증 + 중복 체크
            addressId = normalizeAddressId(addressId);   // @, 특수문자 정리
            validateAddressId(addressId);                // 길이/형식 확인

            if (userRepository.existsByAddressId(addressId)) {
                throw new IllegalArgumentException("이미 사용 중인 ID입니다.");
            }
        }

        user.setAddressId(addressId);

        // 5) 저장
        User saved = userRepository.save(user);

        // 6) 가입과 동시에 Box 자동 생성
        boxService.createBoxForUser(saved);

        return saved.getId();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {

        // 1) userId로 사용자 찾기
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        //1-1) 유저검증
        if (user.getStatus() == UserStatus.BANNED) {
            throw new IllegalStateException("운영 정책 위반으로 해당 계정의 이용이 제한되었습니다.");
        }

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) JWT 토큰 생성 (userId + role)
        String token = jwtTokenProvider.createToken(
                user.getUserId(),
                user.getRole().name()   // 또는 user.getUserRole().name()
        );


        // 4) 유저의 박스 조회
        Box box = boxRepository.findByOwner(user)
                .orElseThrow(() -> new IllegalStateException("해당 유저의 박스가 존재하지 않습니다."));

        // 5) LoginResponseDTO 구성
        LoginResponseDTO response = new LoginResponseDTO(user, token);
        response.setBoxUrlKey(box.getUrlKey());
        response.setAddressId(user.getAddressId());

        return response;
    }

    // =========================
    // 아이디 / 비밀번호 찾기
    // =========================

    @Override
    public String findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));
        return user.getUserId();
    }

    @Override
    @Transactional
    public String resetPassword(String userId, String email) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디와 이메일 정보가 일치하지 않습니다.");
        }

        // 1) 임시 비밀번호 생성
        String tempPassword = generateTempPassword(10);

        // 2) 비밀번호 변경 (암호화)
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // 3) (이메일 발송은 나중에 추가)
        return tempPassword;
    }

    @Override
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }




    // 임시 비밀번호 생성 유틸
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

    // =========================
    // 유저 조회
    // =========================

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다."));
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다."));
    }

    // =========================
    // 프로필 / 설정
    // =========================

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponseDTO getMyProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return MyProfileResponseDTO.from(user);
    }

    @Override
    @Transactional
    public MyProfileResponseDTO updateMyProfile(String userId, MyProfileUpdateRequestDTO dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(dto.getProfileImageUrl());
        }
        if (dto.getHeaderImageUrl() != null) {
            user.setHeaderImageUrl(dto.getHeaderImageUrl());
        }

        // ✅ 추가: 오늘 한마디 업데이트
        if (dto.getTodayMessage() != null) {
            String msg = dto.getTodayMessage().trim();
            // 선택: 너무 긴 입력 방어
            if (msg.length() > 120) msg = msg.substring(0, 120);
            user.setTodayMessage(msg);
        }

        if (dto.getAiConsultingEnabled() != null) {
            user.setAiConsultingEnabled(dto.getAiConsultingEnabled());
        }

        return MyProfileResponseDTO.from(user); // 더티체킹으로 update
    }


    @Override
    @Transactional
    public void updateAiConsultingByUserId(String userId, boolean enabled) {
        User user = findByUserId(userId);
        user.setAiConsultingEnabled(enabled);
    }

    // =========================
    // 계정 삭제 (박스/메시지/알림/블랙리스트 포함)
    // =========================

    @Override
    @Transactional
    public void deleteUserByUserId(String userId) {

        // 1) 유저 찾기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다."));

        // 2) 이 유저의 박스 (1:1 관계)
        Optional<Box> optionalBox = boxRepository.findByOwner(user);

        // 3) 메시지 ID들을 한 곳에 모아두기 (중복 방지용)
        Set<Long> messageIdSet = new HashSet<>();

        // 3-1) 내 박스에 달린 모든 메시지들
        optionalBox.ifPresent(box -> {
            List<Message> boxMessages = messageRepository.findByBox(box);
            for (Message m : boxMessages) {
                messageIdSet.add(m.getId());
            }
        });

        // 3-2) 내가 작성한 모든 메시지들 (다른 사람 박스도 포함)
        List<Message> authoredMessages = messageRepository.findByAuthorUser(user);
        for (Message m : authoredMessages) {
            messageIdSet.add(m.getId());
        }

        // 4) 메시지들에 연결된 알림 먼저 삭제
        if (!messageIdSet.isEmpty()) {
            List<Long> allMessageIds = new ArrayList<>(messageIdSet);

            // 이 메시지들에 대한 알림 제거
            notificationRepository.deleteAllByMessage_IdIn(allMessageIds);

            // 메시지 삭제
            messageRepository.deleteAllById(allMessageIds);
        }

        // 5-1) 내 박스에서 내가 차단해둔 유저들 기록 삭제 (owner 입장)
        optionalBox.ifPresent(box -> {
            blackListRepository.deleteAllByBox(box);
        });

        // 5-2) 내가 '차단당한 쪽(blockedUser)'으로 들어간 블랙리스트 전부 삭제
        blackListRepository.deleteAllByBlockedUser(user);

        // 6) 박스 삭제
        optionalBox.ifPresent(boxRepository::delete);

        // 7) 마지막으로 사용자 삭제
        userRepository.delete(user);
    }

    // =========================
    // addressId 관련 헬퍼 메서드
    // =========================

    /**
     * 닉네임을 기반으로 자동 생성용 slug 만들기
     * 예: "열시" -> "user" (한글만 있을 때)
     * 예: "YulSi!!" -> "yulsi"
     */
    private String makeSlugFromNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "user";
        }

        // 1) 소문자로
        String temp = nickname.toLowerCase();

        // 2) 영문/숫자만 남기고 나머지는 제거
        temp = temp.replaceAll("[^a-z0-9]", "");

        // 3) 다 지워졌다면 기본값
        if (temp.isBlank()) {
            temp = "user";
        }

        return temp;
    }

    /**
     * 같은 addressId 가 있으면 뒤에 숫자 붙여서 유니크하게 생성
     * 예: yulsi, yulsi1, yulsi2...
     */
    private String generateUniqueAddressId(String base) {
        String candidate = base;
        int suffix = 1;

        while (userRepository.existsByAddressId(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    /**
     * 사용자가 입력한 addressId를 정리
     * - 앞뒤 공백 제거
     * - 소문자로
     * - 맨 앞 @ 제거
     * - 허용하지 않는 문자 제거
     */
    private String normalizeAddressId(String raw) {
        String temp = raw.trim().toLowerCase();

        // 맨 앞 @ 제거
        if (temp.startsWith("@")) {
            temp = temp.substring(1);
        }

        // 허용 문자: 영문 소문자, 숫자, _, .
        temp = temp.replaceAll("[^a-z0-9_.]", "");

        return temp;
    }

    /**
     * addressId 형식 검증
     */
    private void validateAddressId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID를 입력해 주세요.");
        }
        if (id.length() < 4 || id.length() > 20) {
            throw new IllegalArgumentException("ID는 4~20자여야 합니다.");
        }
        // 예약어 예시
        if (id.equals("admin") || id.equals("me") || id.equals("q")) {
            throw new IllegalArgumentException("사용할 수 없는 ID입니다.");
        }
    }
}
