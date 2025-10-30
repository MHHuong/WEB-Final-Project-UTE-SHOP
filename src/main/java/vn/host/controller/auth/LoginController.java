package vn.host.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginView() {
        return "/auth/login";
    }
    @GetMapping("/register")
    public String registerView() {
        return "/auth/register";
    }
    @GetMapping("/forgot-password")
    public String forgotPasswordView() {
        return "/auth/forgot-password";
    }
}