package org.jyr.postbox.repository;

import org.jyr.postbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //회원가입시 중복체크
    boolean existsByEmail(String email);
    //로그인 시 유저찾기
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId);
    boolean existsByAddressId(String addressId);
    Optional<User> findByAddressId(String addressId);
    boolean existsByUserId(String userId);



}