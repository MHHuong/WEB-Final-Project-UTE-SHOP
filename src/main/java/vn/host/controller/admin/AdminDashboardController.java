package vn.host.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    //DASHBOARD
    @GetMapping({"", "/"})
    public String root() {
        // /admin -> /admin/dashboard
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard/index"; // templates/admin/dashboard/index.html
    }


    //PRODUCT
    @GetMapping("/products")
    public String productsPage() {
        return "admin/products/products";
    }

    //CATEGORIES
    @GetMapping("/categories")
    public String categoriesPage() {
        return "admin/category/categories";
    }

    @GetMapping("/categories/add")
    public String addPage() {
        return "admin/category/add-category";
    }

    @GetMapping("/categories/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "admin/category/edit-category";
    }

    //USER
    @GetMapping("/customers")
    public String customersPage() {
        return "admin/customers/customers"; // templates/admin/customers/customers.html
    }

    @GetMapping("/customers/create-customers")
    public String createCustomerPage() {
        return "admin/customers/create-customers";
    }

    @GetMapping("/customers/customers-edits")
    public String editCustomerPage() {
        return "admin/customers/customers-edits";
    }

    //COUPON
    @GetMapping("/coupons")
    public String couponsPage() {
        return "admin/coupons/admin-coupons";
    }

    @GetMapping("/coupons/add")
    public String addCouponPage() {
        return "admin/coupons/add-coupons";
    }

    //PROMOTION
    @GetMapping("/promotions")
    public String promotionsPage() {
        return "admin/promotions/promotions";
    }

    @GetMapping("/promotions/add")
    public String addPromotionPage() {
        return "admin/promotions/add-promotions";
    }

    @GetMapping("/promotions/edit")
    public String editPromotionPage() {
        return "admin/promotions/edit-promotions";
    }

    //SHIPPING-PROVIDER
    @GetMapping("/shipping-providers")
    public String shippingProviderPage() {
        return "admin/shipping-provider/shipping-provider";
    }

    @GetMapping("/shipping-providers/add")
    public String addShippingProviderPage() {
        return "admin/shipping-provider/add-shipping-provider";
    }

    @GetMapping("/shipping-providers/edit")
    public String editShippingProviderPage() {
        return "admin/shipping-provider/edit-shipping-provider";
    }

    //SHIPPING-PROVIDER
    @GetMapping("/shippers")
    public String shipperPage() {
        return "admin/shippers/shippers";
    }

    @GetMapping("/shippers/add")
    public String addShipperPage() {
        return "admin/shippers/add-shipper";
    }

    @GetMapping("/shippers/edit")
    public String editShipperPage() {
        return "admin/shippers/edit-shipper";
    }

}