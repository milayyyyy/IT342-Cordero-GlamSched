package edu.cit.cordero.glamsched.service;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiResponse<User> updateUserRole(Long userId, String newRole) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return ApiResponse.error("USER-001", "User not found!");
        }

        User user = userOptional.get();
        
        // Validate role
        if (!newRole.equals("CLIENT") && !newRole.equals("ARTIST") && !newRole.equals("ADMIN")) {
            return ApiResponse.error("VALID-001", "Invalid role! Must be CLIENT, ARTIST, or ADMIN.");
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        
        return ApiResponse.success(updatedUser);
    }

    public ApiResponse<User> getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return ApiResponse.error("USER-001", "User not found!");
        }

        return ApiResponse.success(userOptional.get());
    }

    public ApiResponse<User> updateUserPhoto(Long userId, String profileImage) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return ApiResponse.error("USER-001", "User not found!");
        }

        User user = userOptional.get();
        user.setProfileImage(profileImage);
        User updatedUser = userRepository.save(user);
        
        return ApiResponse.success(updatedUser);
    }
}
