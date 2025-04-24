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

    @PostMapping("/send-code")
    public String registerStepOne(@RequestBody Users user) {
        usersService.sendCode(user);
        return "ok";
    }

    @PostMapping("/register")
    public String registerStepTwo(@RequestBody Users user) {
        usersService.save(user);
        return "ok";
    }

    @GetMapping("/test")
    public String test() {
        return "ok";
    }
}
