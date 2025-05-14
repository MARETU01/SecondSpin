package com.secondspin.user.controller;


import com.secondspin.common.utils.Result;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.service.IUsersService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final IUsersService usersService;

    public UsersController(IUsersService usersService) {
        this.usersService = usersService;
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
    public Result<Void> registerStepOne(@RequestBody Users user) {
        usersService.sendCode(user);
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> registerStepTwo(@RequestBody Users user, @RequestParam String verification) {
        try {
            usersService.register(user, verification);
            return Result.success();
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Users getUser(@PathVariable Long id) {
        return usersService.getById(id);
    }
}
