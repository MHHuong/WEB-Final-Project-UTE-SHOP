package vn.host.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginView() {
        return "pages/auth/login";
    }
    @GetMapping("/register")
    public String registerView() {
        return "pages/auth/register";
    }
    @GetMapping("/forgot-password")
    public String forgotPasswordView() {
        return "pages/auth/forgot-password";
    }
}