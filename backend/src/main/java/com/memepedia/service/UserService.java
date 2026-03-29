package com.memepedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.memepedia.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
    User register(String username, String password, String email);
    void updateRole(Long userId, Integer role);
    void updateStatus(Long userId, Integer status);
}
