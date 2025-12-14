package org.jyr.postbox.service;

import java.util.List;

public interface AdminUserService {
    List<?> getAllUsers();
    void banUser(Long userId);
    void unbanUser(Long userId);

    //admin 부여
    void promoteToAdmin(Long userId);
    void demoteToUser(Long userId);
}
