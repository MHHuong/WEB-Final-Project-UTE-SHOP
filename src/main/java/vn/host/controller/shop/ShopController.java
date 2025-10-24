package vn.host.controller.shop;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.shop.RegisterReq;
import vn.host.dto.shop.ShopRes;
import vn.host.dto.shop.UpdateReq;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.service.UserService;
import vn.host.service.ShopService;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopSvc;
    private final UserRepository userRepo;
    private final UserService userService;

    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        String email = auth.getName();
        return userService.getUserByEmail(email);
    }

    @GetMapping("/me")
    public ResponseEntity<ShopRes> myShop(Authentication auth) {
        User u = authedUser(auth);
        Shop s = shopSvc.getMyShopOrNull(u.getUserId());
        if (s == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ShopRes.of(s));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(Authentication auth,
                                      @RequestBody @Valid RegisterReq req) {
        User owner = authedUser(auth);
        Shop incoming = new Shop();
        incoming.setShopName(req.getShopName());
        incoming.setAddress(req.getAddress());
        incoming.setDescription(req.getDescription());

        Shop saved = shopSvc.registerOneShopForOwner(owner, incoming);
        return ResponseEntity.status(HttpStatus.CREATED).body(ShopRes.of(saved));
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShopRes> update(Authentication auth,
                                          @RequestBody @Valid UpdateReq req) {
        User u = authedUser(auth);
        Shop updated = shopSvc.updateMyShop(u.getUserId(), s -> {
            if (req.getShopName() != null) s.setShopName(req.getShopName());
            if (req.getAddress() != null) s.setAddress(req.getAddress());
            if (req.getDescription() != null) s.setDescription(req.getDescription());
        });
        if (req.getPhone() != null) {
            u.setPhone(req.getPhone().trim());
            userRepo.save(u);
        }
        return ResponseEntity.ok(ShopRes.of(updated));
    }
}