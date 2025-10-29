package vn.host.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication; // <-- KHÔNG CẦN NỮA
import org.springframework.web.bind.annotation.*;
import vn.host.dto.shop.MeRes;
import vn.host.dto.shop.ShopRes;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.security.JwtService; // <-- SỬ DỤNG SERVICE NÀY
import vn.host.service.ShopService;
import vn.host.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthMeController {

    private final UserService userService;
    private final ShopService shopService;
    private final JwtService authService;
    @GetMapping("/me")
    public ResponseEntity<MeRes> me(@RequestHeader("Authorization") String authHeader) {

        String email = null;
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }

            String token = authHeader.substring(7);
            email = authService.getSubject(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
        if (email == null) return ResponseEntity.status(401).build();
        User u = userService.findByEmail(email);
        if (u == null) return ResponseEntity.status(401).build();
        MeRes res = new MeRes();
        res.setUserId(u.getUserId());
        res.setEmail(u.getEmail());
        res.setFullName(u.getFullName());
        res.setRole(u.getRole() != null ? u.getRole().name() : null);
        res.setPhone(u.getPhone());
        Shop s = shopService.findShopByOwner_UserId(u.getUserId());
        res.setShop(s != null ? ShopRes.of(s) : null);
        return ResponseEntity.ok(res);
    }
}