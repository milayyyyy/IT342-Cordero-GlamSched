package edu.cit.cordero.glamsched.features.auth;

import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFullName("Test User");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setRole("CLIENT");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("CLIENT");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ApiResponse<AuthResponse> response = authService.register(registrationRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void register_DuplicateEmail_ReturnsError() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        ApiResponse<AuthResponse> response = authService.register(registrationRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo("AUTH-001");
    }

    @Test
    void register_MissingFields_ReturnsError() {
        registrationRequest.setEmail(null);

        ApiResponse<AuthResponse> response = authService.register(registrationRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo("VALID-001");
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void login_InvalidPassword_ReturnsError() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo("AUTH-001");
    }

    @Test
    void login_UserNotFound_ReturnsError() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo("AUTH-001");
    }
}
