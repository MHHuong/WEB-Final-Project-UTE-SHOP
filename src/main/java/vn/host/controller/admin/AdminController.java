package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.service.AdminService;
import vn.host.util.sharedenum.UserRole;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 1. Lấy danh sách user
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId"));
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    // 2. Tìm user theo email
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId"));
        return ResponseEntity.ok(adminService.searchUsersByEmail(email, pageable));
    }

    // 3. Lọc user theo vai trò
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    // 4. Cập nhật trạng thái (active = 1, inactive = 0)
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            adminService.updateUserStatus(id, status);
            return ResponseEntity.ok("User status updated successfully");
        }catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 5. Cập nhật vai trò user
    @PutMapping("/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        try {
            adminService.updateUserRole(id, role);
            return ResponseEntity.ok("User role updated successfully");
        }catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 6. Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        }catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 7. Tạo mới user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            user.setUserId(null);
            user.setStatus(1);
            return ResponseEntity.ok(adminService.saveUser(user));
        }catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 8. Cập nhật thông tin user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser
    ) {
        try{
            User user = adminService.updateUserInfo(id, updatedUser);
            return ResponseEntity.ok(user);
        }catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }

    }

    // 9. Lấy thông tin 1 user
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try{
            User user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy user!");
        }
    }


}
