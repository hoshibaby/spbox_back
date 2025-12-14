package org.jyr.postbox.repository;

import org.jyr.postbox.domain.Box;
import org.jyr.postbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {

    // 로그인한 유저의 박스 찾기
    Optional<Box> findByOwner(User owner);

    // 익명 사용자가 url로 접근(예: /box/abc123)
    Optional<Box> findByUrlKey(String urlKey);

    //owner.userId 로 박스 찾기
    Optional<Box> findByOwner_UserId(String userId);;
}
