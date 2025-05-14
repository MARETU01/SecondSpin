package com.secondspin.user.service.impl;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.JwtUtils;
import com.secondspin.common.utils.RedisConstants;
import com.secondspin.user.enums.AccountStatus;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.mapper.UsersMapper;
import com.secondspin.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondspin.user.utils.HashUtil;
import com.secondspin.user.utils.MailUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    public String login(Users user) {
        Users userToLogin = lambdaQuery().eq(Users::getEmail, user.getEmail()).one();
        if (userToLogin == null || !HashUtil.checkPassword(user.getPassword(), userToLogin.getPassword())) {
            throw new RuntimeException("email or password not correct");
        }
        if (userToLogin.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("user's status is not active");
        }
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUserId(userToLogin.getUserId());
        jwtUser.setUsername(userToLogin.getUsername());
        jwtUser.setEmail(userToLogin.getEmail());
        return JwtUtils.generateJwt(jwtUser);
    }

    @Override
    public void sendCode(Users user) {
        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.VERIFY_CODE_KEY + user.getEmail(),
                String.valueOf(verificationCode),
                RedisConstants.VERIFY_CODE_TTL,
                TimeUnit.MINUTES
        );
        mailUtil.sendVerificationCodeMail(user.getEmail(), String.valueOf(verificationCode));
    }

    @Override
    public void register(Users user, String verification) {
        String storedCodde = stringRedisTemplate.opsForValue().get(
                RedisConstants.VERIFY_CODE_KEY + user.getEmail()
        );
        if (!Objects.equals(storedCodde, verification)) {
            throw new RuntimeException("verification code not correct");
        }
        String encodedPassword = HashUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPassword);
        save(user);
        stringRedisTemplate.delete(RedisConstants.VERIFY_CODE_KEY + user.getEmail());
    }
}
