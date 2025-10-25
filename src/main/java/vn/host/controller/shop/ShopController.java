package vn.host.controller.shop;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.host.dto.shop.RegisterReq;
import vn.host.dto.shop.ShopRes;
import vn.host.dto.shop.UpdateReq;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.service.UserService;
import vn.host.service.ShopService;

import java.net.URI;
import java.util.Map;

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

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> uploadMyLogo(Authentication auth,
                                                           @RequestPart("file") MultipartFile file) throws Exception {
        User u = authedUser(auth);
        String url = shopSvc.updateMyLogo(u.getUserId(), file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/me/logo")
    public ResponseEntity<Void> deleteMyLogo(Authentication auth) throws Exception {
        User u = authedUser(auth);
        shopSvc.deleteMyLogo(u.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/me/logo", "/../shop/me/logo"})
    public ResponseEntity<Void> getMyLogo(Authentication auth) {
        User u = authedUser(auth);
        Shop s = shopSvc.getMyShopOrNull(u.getUserId());
        if (s == null || s.getLogo() == null || s.getLogo().isBlank()) {
            URI fb = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .build().toUri();
            return ResponseEntity.status(HttpStatus.FOUND).location(fb).build();
        }
        String logo = s.getLogo();
        String target = logo.startsWith("http")
                ? logo
                : ServletUriComponentsBuilder.fromCurrentContextPath().path(logo).build().toUriString();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}