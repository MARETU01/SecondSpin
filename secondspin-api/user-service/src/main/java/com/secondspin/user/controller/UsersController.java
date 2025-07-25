package com.secondspin.user.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.utils.Result;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.service.IUsersService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final IUsersService usersService;
    private final ObjectMapper jacksonObjectMapper;

    public UsersController(IUsersService usersService, ObjectMapper jacksonObjectMapper) {
        this.usersService = usersService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody Users user) {
        try {
            return Result.success(usersService.login(user));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/register/code")
    public Result<Boolean> registerStepOne(@RequestBody Users user) {
        try {
            return Result.success(usersService.sendCode(user));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Integer> registerStepTwo(@RequestBody Users user, @RequestParam String verification) {
        try {
            return Result.success(usersService.register(user, verification));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/forget-password/code")
    public Result<Boolean> sendForgetPasswordCode(@RequestBody Users user) {
        try {
            return Result.success(usersService.sendForgetPasswordCode(user));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/reset-password/code")
    public Result<Boolean> sendResetPasswordCode(@RequestHeader("user-info") String userJson,
                                                 @RequestBody Users userInfo) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        user.setPassword(userInfo.getPassword());
        try {
            return Result.success(usersService.sendResetPasswordCode(user));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@RequestHeader(value = "user-info", required = false) String userJson,
                                         @RequestBody(required = false) Users user,
                                         @RequestParam String verification) throws JsonProcessingException {
        if (userJson != null && !userJson.isEmpty()) {
            user.setEmail(jacksonObjectMapper.readValue(userJson, Users.class).getEmail());
        }
        try {
            return Result.success(usersService.resetPassword(user, verification));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Users> getUserInfo(@RequestHeader(value = "user-info", required = false) String userJson,
                                     @PathVariable Integer id) throws JsonProcessingException {
        Users user = null;
        if (userJson != null && !userJson.isEmpty()) {
            user = jacksonObjectMapper.readValue(userJson, Users.class);
        }
        try {
            return Result.success(usersService.getUserInfo(user, id));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/info")
    public Result<Boolean> updateUserInfo(@RequestHeader(value = "user-info") String userJson,
                                          @RequestBody Users userInfo) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(usersService.updateUserInfo(user.getUserId(), userInfo));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/avatar")
    public Result<Boolean> updateAvatar(@RequestHeader(value = "user-info") String userJson,
                                        @RequestParam("file") MultipartFile file) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(usersService.updateAvatar(user.getUserId(), file));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/validate")
    public Result<Users> getAllUsers(@RequestHeader(value = "user-info", required = false) String userJson) throws JsonProcessingException {
        if (userJson == null || userJson.isEmpty()) {
            return Result.failure("Not login in");
        }
        try {
            Users user = jacksonObjectMapper.readValue(userJson, Users.class);
            return Result.success(usersService.getById(user.getUserId()).setPassword(null));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Users getUser(@PathVariable Long id) {
        return usersService.getById(id);
    }

    @GetMapping
    public List<Users> getUsersInfo(@RequestParam List<Integer> ids) {
        return usersService.listByIds(ids);
    }

    @GetMapping("/search")
    public Result<List<Users>> searchUsers(@RequestParam String search) {
        try {
            List<Users> allUsers = usersService.list();
            List<Users> results = new ArrayList<>();

            String lowerSearch = search.toLowerCase();
            for (Users user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerSearch) ||
                        user.getEmail().toLowerCase().contains(lowerSearch)) {
                    Users safeUser = new Users();
                    safeUser.setUserId(user.getUserId());
                    safeUser.setUsername(user.getUsername());
                    safeUser.setEmail(user.getEmail());
                    safeUser.setAvatarUrl(user.getAvatarUrl());
                    results.add(safeUser);
                }
            }

            return Result.success(results);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
