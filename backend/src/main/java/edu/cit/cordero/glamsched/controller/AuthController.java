package edu.cit.cordero.glamsched.controller;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.dto.AuthResponse;
import edu.cit.cordero.glamsched.dto.UserLoginRequest;
import edu.cit.cordero.glamsched.dto.UserRegistrationRequest;
import edu.cit.cordero.glamsched.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody UserRegistrationRequest request) {
        ApiResponse<AuthResponse> response = authService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(201).body(response); // 201 Created as per SDD
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody UserLoginRequest request) {
        ApiResponse<AuthResponse> response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response); // 401 Unauthorized as per SDD
        }
    }
}
