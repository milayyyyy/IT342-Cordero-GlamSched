package edu.cit.cordero.glamsched.features.auth;

import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AuthService authService;

    private HttpEntity<Object> jsonRequest(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void register_Success_Returns201() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("CLIENT");

        AuthResponse.UserData userData = new AuthResponse.UserData(1L, "test@example.com", "Test User", "CLIENT");
        AuthResponse authResponse = new AuthResponse(userData, "mock-jwt-token", "mock-refresh-token");
        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authResponse);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(apiResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity("/auth/register", jsonRequest(request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("success", true);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        Map<?, ?> user = (Map<?, ?>) data.get("user");
        assertThat(user.get("email")).isEqualTo("test@example.com");
    }

    @Test
    void register_DuplicateEmail_Returns400() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("CLIENT");

        ApiResponse<AuthResponse> apiResponse = ApiResponse.error("AUTH-001", "Email already exists!");

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(apiResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity("/auth/register", jsonRequest(request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH-001");
    }

    @Test
    void login_Success_Returns200() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse.UserData userData = new AuthResponse.UserData(1L, "test@example.com", "Test User", "CLIENT");
        AuthResponse authResponse = new AuthResponse(userData, "mock-jwt-token", "mock-refresh-token");
        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authResponse);

        when(authService.login(any(UserLoginRequest.class))).thenReturn(apiResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity("/auth/login", jsonRequest(request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("accessToken")).isEqualTo("mock-jwt-token");
    }

    @Test
    void login_InvalidCredentials_Returns401() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        ApiResponse<AuthResponse> apiResponse = ApiResponse.error("AUTH-001", "Invalid credentials!");

        when(authService.login(any(UserLoginRequest.class))).thenReturn(apiResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity("/auth/login", jsonRequest(request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("success", false);
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH-001");
    }
}
