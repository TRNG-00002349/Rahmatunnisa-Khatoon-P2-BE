package com.revature.controller;

import com.revature.dto.ApiResponse;
import com.revature.entity.User;
import com.revature.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success("User banned successfully", null));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        adminService.deleteAnyPost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        User user = adminService.changeUserRole(id, request.get("role"));
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }
}
