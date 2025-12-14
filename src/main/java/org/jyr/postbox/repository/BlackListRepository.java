package org.jyr.postbox.repository;

import org.jyr.postbox.domain.BlackList;
import org.jyr.postbox.domain.Box;
import org.jyr.postbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {

    // 이 박스에서 이 유저가 차단됐는지 확인
    boolean existsByBoxAndBlockedUser(Box box, User blockedUser);

    // 필요하면 해제할 때 사용
    long deleteByBoxAndBlockedUser(Box box, User blockedUser);

    // 내 박스에서 차단된 유저 목록
    List<BlackList> findByBox(Box box);

    // 🔹 이 박스에서 차단한 모든 유저 기록 삭제 (owner 입장)
    @Modifying
    long deleteAllByBox(Box box);

    // 🔹 이 유저가 '차단당한 쪽(blockedUser)'으로 올라간 모든 레코드 삭제
    @Modifying
    long deleteAllByBlockedUser(User blockedUser);
}
