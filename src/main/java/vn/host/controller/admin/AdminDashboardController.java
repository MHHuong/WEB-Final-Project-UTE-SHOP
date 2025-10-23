package vn.host.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping({"", "/"})
    public String root() {
        // /admin -> /admin/dashboard
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard/index"; // templates/admin/dashboard/index.html
    }

    @GetMapping("/products")
    public String productsPage() {
        return "admin/products/products";
    }

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
    @GetMapping("/customers")
    public String customersPage() {
        return "admin/customers/customers"; // templates/admin/customers/customers.html
    }

    // thêm user
    @GetMapping("/customers/create-customers")
    public String createCustomerPage() {
        return "admin/customers/create-customers";
    }

    // sửa user
    @GetMapping("/customers/customers-edits")
    public String editCustomerPage() {
        return "admin/customers/customers-edits";
    }
}
