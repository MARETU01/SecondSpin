package com.secondspin.user.service;

import com.secondspin.user.pojo.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-22
 */
public interface IUsersService extends IService<Users> {

    String login(Users user);

    void sendCode(Users user);

    String register(Users user, String verification);
}
