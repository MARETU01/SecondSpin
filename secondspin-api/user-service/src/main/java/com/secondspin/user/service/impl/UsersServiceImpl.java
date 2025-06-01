package com.secondspin.user.service.impl;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.ImagesUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public Boolean sendCode(Users user) {
        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.VERIFY_CODE_KEY + user.getEmail(),
                String.valueOf(verificationCode),
                RedisConstants.VERIFY_CODE_TTL,
                TimeUnit.MINUTES
        );
        mailUtil.sendVerificationCodeMail(user.getEmail(), String.valueOf(verificationCode));
        return true;
    }

    @Override
    public Integer register(Users user, String verification) {
        String storedCode = stringRedisTemplate.opsForValue().get(
                RedisConstants.VERIFY_CODE_KEY + user.getEmail()
        );
        if (!Objects.equals(storedCode, verification)) {
            throw new RuntimeException("verification code not correct");
        }
        if (lambdaQuery().eq(Users::getEmail, user.getEmail()).one() != null) {
            throw new RuntimeException("email already registered");
        } else if (lambdaQuery().eq(Users::getUsername, user.getUsername()).one() != null) {
            throw new RuntimeException("username already registered");
        }
        String encodedPassword = HashUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPassword);
        save(user);
        stringRedisTemplate.delete(RedisConstants.VERIFY_CODE_KEY + user.getEmail());
        return user.getUserId();
    }

    @Override
    public Boolean sendForgetPasswordCode(Users user) {
        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.RESET_PASSWORD_KEY + user.getEmail(),
                String.valueOf(verificationCode),
                RedisConstants.VERIFY_CODE_TTL,
                TimeUnit.MINUTES
        );
        mailUtil.sendVerificationCodeMail(user.getEmail(), String.valueOf(verificationCode));
        return true;
    }

    @Override
    public Boolean sendResetPasswordCode(Users user) {
        Users realUser = getById(user.getUserId());
        if (HashUtil.checkPassword(user.getPassword(), realUser.getPassword())) {
            int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.RESET_PASSWORD_KEY + user.getEmail(),
                    String.valueOf(verificationCode),
                    RedisConstants.VERIFY_CODE_TTL,
                    TimeUnit.MINUTES
            );
            mailUtil.sendVerificationCodeMail(user.getEmail(), String.valueOf(verificationCode));
            return true;
        }
        throw new RuntimeException("password not correct");
    }

    @Override
    public Boolean resetPassword(Users user, String verification) {
        String storedCode = stringRedisTemplate.opsForValue().get(
                RedisConstants.RESET_PASSWORD_KEY + user.getEmail()
        );
        if (!Objects.equals(storedCode, verification)) {
            throw new RuntimeException("verification code not correct");
        }
        String encodedPassword = HashUtil.encodePassword(user.getPassword());
        lambdaUpdate()
                .eq(Users::getEmail, user.getEmail())
                .set(Users::getPassword, encodedPassword)
                .update();
        stringRedisTemplate.delete(RedisConstants.RESET_PASSWORD_KEY + user.getEmail());
        return true;
    }

    @Override
    public Users getUserInfo(Users user, Integer id) {
        Users userInfo = getById(id);
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (user != null && user.getUserId().equals(id)) {
            return userInfo.setPassword(null);
        }
        return userInfo
                .setPassword(null)
                .setRealName(null)
                .setPhone(null);
    }

    @Override
    public Boolean updateUserInfo(Integer userId, Users userInfo) {
        if (userInfo.getUsername() != null && lambdaQuery().eq(Users::getUsername, userInfo.getUsername()).ne(Users::getUserId, userId).one() != null) {
            throw new RuntimeException("username already exists");
        } else if (userInfo.getEmail() != null && lambdaQuery().eq(Users::getEmail, userInfo.getEmail()).ne(Users::getUserId, userId).one() != null) {
            throw new RuntimeException("email already exists");
        }
        return lambdaUpdate()
                .eq(Users::getUserId, userId)
                .set(userInfo.getUsername() != null, Users::getUsername, userInfo.getUsername())
                .set(userInfo.getRealName() != null, Users::getRealName, userInfo.getRealName())
                .set(userInfo.getEmail() != null, Users::getEmail, userInfo.getEmail())
                .set(userInfo.getPhone() != null, Users::getPhone, userInfo.getPhone())
                .update();
    }

    @Override
    public Boolean updateAvatar(Integer userId, MultipartFile file) {
        try {
            String avatarName = ImagesUtils.saveAvatar(file);
            return lambdaUpdate()
                    .eq(Users::getUserId, userId)
                    .set(Users::getAvatarUrl, avatarName)
                    .update();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar image: " + e.getMessage());
        }
    }
}
