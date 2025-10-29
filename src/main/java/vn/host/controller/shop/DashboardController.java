package vn.host.controller.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.dashboard.DashboardRes;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.service.DashboardService;
import vn.host.service.ShopService;
import vn.host.service.UserService;

import java.time.Instant;

@RestController
@RequestMapping("/api/shop/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class DashboardController {

    private final UserService users;
    private final ShopService shops;
    private final DashboardService dashboard;

    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        return users.getUserByEmail(auth.getName());
    }

    private Shop myShopOr403(User u) {
        Shop s = shops.getMyShopOrNull(u.getUserId());
        if (s == null) throw new SecurityException("Shop not registered");
        return s;
    }

    @GetMapping
    public ResponseEntity<DashboardRes> getDashboard(Authentication auth,
                                                     @RequestParam(required = false) Long from,
                                                     @RequestParam(required = false) Long to) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Instant fromI = from != null ? Instant.ofEpochMilli(from) : null;
        Instant toI = to != null ? Instant.ofEpochMilli(to) : null;

        DashboardRes res = dashboard.buildForShop(s.getShopId(), fromI, toI);
        return ResponseEntity.ok(res);
    }
}
