package org.jyr.postbox.service;

import org.jyr.postbox.domain.User;
import org.jyr.postbox.dto.user.BlackListDTO;

import java.util.List;

public interface BlackListService {

    // 내 박스 기준 블랙리스트 전체 조회
    List<BlackListDTO> getBlackListForOwner(User owner);

    // 특정 유저 차단 해제
    void unblockUser(Long blockedUserId, User owner);
}

