package vn.host.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.shop.MeRes;
import vn.host.dto.shop.ShopRes;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.service.ShopService;
import vn.host.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthMeController {

    private final UserService userService;
    private final ShopService shopService;

    @GetMapping("/me")
    public ResponseEntity<MeRes> me(Authentication auth) {
        String email = (auth != null) ? auth.getName() : null;
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