package org.jyr.postbox.repository;

import org.jyr.postbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 회원가입 시 중복 체크
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByAddressId(String addressId);

    // 로그인 / 조회
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    Optional<User> findByAddressId(String addressId);
}