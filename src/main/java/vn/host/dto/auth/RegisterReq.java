package vn.host.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterReq {
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Phone number is required")
    private String phone;
    private String role;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    @NotBlank(message = "OTP is required")
    private String otp;
}