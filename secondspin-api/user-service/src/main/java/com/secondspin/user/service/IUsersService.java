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

    Boolean sendCode(Users user);

    Integer register(Users user, String verification);

    Boolean sendForgetPasswordCode(Users user);

    Boolean sendResetPasswordCode(Users user);

    Boolean resetPassword(Users user, String verification);

    Users getUserInfo(Users user, Long id);
}
