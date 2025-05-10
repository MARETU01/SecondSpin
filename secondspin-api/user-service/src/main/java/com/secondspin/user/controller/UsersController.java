package com.secondspin.user.controller;


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
    public String login(@RequestBody Users user) {
        return usersService.login(user);
    }

    @PostMapping("/register/code")
    public String registerStepOne(@RequestBody Users user) {
        usersService.sendCode(user);
        return "success";
    }

    @PostMapping("/register")
    public String registerStepTwo(@RequestBody Users user, @RequestParam String verification) {
        return usersService.register(user, verification);
    }
}
