package org.jyr.postbox.service;

import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.user.*;

public interface UserService {

    // 회원가입 후 생성된 유저 id 반환
    Long signup(UserSignupDTO dto);


    // 이메일로 유저 검색 (로그인/인증에서 사용)
    User findByEmail(String email);

    User findById(Long id);

    User findByUserId(String userId);

    void updateAiConsultingByUserId(String userId, boolean enabled);

    void deleteUserByUserId(String userId);

    LoginResponseDTO login(LoginRequestDTO dto);

    MyProfileResponseDTO getMyProfile(String userId);  // PK 기준

    MyProfileResponseDTO updateMyProfile(String userId, MyProfileUpdateRequestDTO dto);

    // ⭐ 이메일로 userId 찾기
    String findUserIdByEmail(String email);

    // 아이디+이메일로 검증 후 임시 비밀번호 발급
    String resetPassword(String userId, String email);

    // ⭐ 비밀번호 변경 (로그인 상태)
    void changePassword(String userId, String currentPassword, String newPassword);


}