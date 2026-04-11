package edu.cit.cordero.glamsched.controller;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        ApiResponse<User> response = userService.getUserById(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        String newRole = request.get("role");
        
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALID-001", "Role is required!"));
        }

        ApiResponse<User> response = userService.updateUserRole(userId, newRole);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{userId}/photo")
    public ResponseEntity<ApiResponse<User>> updateUserPhoto(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        String profileImage = request.get("profileImage");
        
        if (profileImage == null || profileImage.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALID-001", "Image data is required!"));
        }

        ApiResponse<User> response = userService.updateUserPhoto(userId, profileImage);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
