package com.secondspin.user.service.impl;

import com.secondspin.user.pojo.Users;
import com.secondspin.user.mapper.UsersMapper;
import com.secondspin.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondspin.user.utils.MailUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2025-04-22
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final MailUtil mailUtil;

    private final StringRedisTemplate stringRedisTemplate;

    public UsersServiceImpl(MailUtil mailUtil, StringRedisTemplate stringRedisTemplate) {
        this.mailUtil = mailUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void sendCode(Users user) {
        Random random = new Random();
        int verificationCode = random.nextInt(900000) + 100000;
        stringRedisTemplate.opsForValue().set(user.getEmail(), String.valueOf(verificationCode));
        mailUtil.sendVerificationCodeMail(user.getEmail(), String.valueOf(verificationCode));
    }
}
