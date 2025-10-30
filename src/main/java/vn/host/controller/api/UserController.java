package vn.host.controller.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Shipper;
import vn.host.entity.User;
import vn.host.model.request.PasswordRequest;
import vn.host.model.request.UserRequest;
import vn.host.model.response.ApiResponse;
import vn.host.service.OrderService;
import vn.host.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    private User authed(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        return
                userService.getUserByEmail(auth.getName());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        try {
            User me = authed(auth);
            if (me == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(
                    new ApiResponse(
                            "Success",
                            "Get user info successfully",
                            me
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserInfo(Authentication auth,@PathVariable Long id, @RequestBody UserRequest user) {
        try {
            User me = authed(auth);
            if (me == null) return ResponseEntity.notFound().build();

            if (!Objects.equals(me.getUserId(), id)) {
                return ResponseEntity.status(403).body(
                        new ApiResponse(
                                "Error",
                                "You are not allowed to update this user",
                                null
                        )
                );
            }

            User update_user = userService.saveInfoUser(user, id);

            return ResponseEntity.ok(
                    new ApiResponse(
                            "Success",
                            "User info updated successfully",
                            update_user
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    @PutMapping("{id}/password")
    public ResponseEntity<?> updatePassword(Authentication auth,@PathVariable Long id, @RequestBody PasswordRequest password) {
        try {
            User me = authed(auth);
            if (me == null) return ResponseEntity.notFound().build();
            if (!Objects.equals(me.getUserId(), id)) {
                return ResponseEntity.status(403).body(
                        new ApiResponse(
                                "Error",
                                "You are not allowed to update this user",
                                null
                        )
                );
            }

            userService.updatePassword(id, password);

            return ResponseEntity.ok(
                    new ApiResponse(
                            "Success",
                            "Password updated successfully",
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    )
            );
        }
    }
}
