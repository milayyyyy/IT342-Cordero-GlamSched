package edu.cit.cordero.glamsched.service;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.dto.AuthResponse;
import edu.cit.cordero.glamsched.dto.UserLoginRequest;
import edu.cit.cordero.glamsched.dto.UserRegistrationRequest;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse<AuthResponse> register(UserRegistrationRequest request) {
        if (request.getFullName() == null || request.getEmail() == null || request.getPassword() == null || request.getRole() == null) {
            return ApiResponse.error("VALID-001", "All fields are required!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("AUTH-001", "Email already exists!");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        user = userRepository.save(user);

        AuthResponse.UserData userData = new AuthResponse.UserData(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );

        AuthResponse authResponse = new AuthResponse(
                userData,
                "mock-jwt-token",
                "mock-refresh-token"
        );

        return ApiResponse.success(authResponse);
    }

    public ApiResponse<AuthResponse> login(UserLoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // Set Security Context for the current session (Simplified for now)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                AuthResponse.UserData userData = new AuthResponse.UserData(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

                AuthResponse authResponse = new AuthResponse(
                        userData,
                        "mock-jwt-token",
                        "mock-refresh-token"
                );
                return ApiResponse.success(authResponse);
            }
        }

        return ApiResponse.error("AUTH-001", "Invalid credentials!");
    }
}
