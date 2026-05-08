package edu.cit.cordero.glamsched.features.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import edu.cit.cordero.glamsched.features.booking.AppointmentRepository;
import edu.cit.cordero.glamsched.features.review.ReviewRepository;
import edu.cit.cordero.glamsched.features.service.ServiceRepository;
import edu.cit.cordero.glamsched.features.service.ServiceReactionRepository;
import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceReactionRepository serviceReactionRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            FollowRepository followRepository, AppointmentRepository appointmentRepository,
            ReviewRepository reviewRepository, ServiceRepository serviceRepository,
            ServiceReactionRepository serviceReactionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.serviceRepository = serviceRepository;
        this.serviceReactionRepository = serviceReactionRepository;
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error("USER_NOT_FOUND", "User not found"));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty())
            return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User user = opt.get();
        if (req.getName() != null && !req.getName().isBlank())
            user.setName(req.getName());
        if (req.getEmail() != null && !req.getEmail().isBlank())
            user.setEmail(req.getEmail());
        if (req.getPhone() != null)
            user.setPhone(req.getPhone());
        if (req.getAddress() != null)
            user.setAddress(req.getAddress());
        if (req.getBio() != null)
            user.setBio(req.getBio());
        userRepository.save(user);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}/password")
    public ApiResponse<String> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty())
            return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User user = opt.get();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return ApiResponse.error("AUTH_ERROR", "Current password is incorrect");
        }
        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            return ApiResponse.error("VALID_ERROR", "New password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password updated successfully");
    }

    @PutMapping("/{id}/photo")
    public ApiResponse<User> updatePhoto(@PathVariable Long id, @RequestBody User photoData) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setProfileImage(photoData.getProfileImage());
            userRepository.save(user);
            return ApiResponse.success(user);
        }
        return ApiResponse.error("USER_NOT_FOUND", "User not found");
    }

    @PutMapping("/{id}/cover")
    public ApiResponse<User> updateCover(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User user = opt.get();
        user.setCoverImage(body.get("coverImage"));
        userRepository.save(user);
        return ApiResponse.success(user);
    }

    /** Full artist profile: user data + followerCount + followedByMe. */
    @GetMapping("/{id}/profile")
    public ApiResponse<Map<String, Object>> getArtistProfile(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User artist = opt.get();
        long followerCount = followRepository.countByArtistId(id);
        boolean followedByMe = clientId != null && followRepository.existsByClientIdAndArtistId(clientId, id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", artist.getId());
        result.put("name", artist.getName());
        result.put("email", artist.getEmail());
        result.put("role", artist.getRole());
        result.put("profileImage", artist.getProfileImage());
        result.put("coverImage", artist.getCoverImage());
        result.put("phone", artist.getPhone());
        result.put("address", artist.getAddress());
        result.put("bio", artist.getBio());
        result.put("followerCount", followerCount);
        result.put("followedByMe", followedByMe);
        return ApiResponse.success(result);
    }

    /** Toggle follow/unfollow an artist. Returns followedByMe + followerCount. */
    @Transactional
    @PostMapping("/{artistId}/follow")
    public ApiResponse<Map<String, Object>> toggleFollow(
            @PathVariable Long artistId,
            @RequestParam Long clientId) {
        boolean alreadyFollowing = followRepository.existsByClientIdAndArtistId(clientId, artistId);
        if (alreadyFollowing) {
            followRepository.deleteByClientIdAndArtistId(clientId, artistId);
        } else {
            followRepository.save(new Follow(clientId, artistId));
        }
        long count = followRepository.countByArtistId(artistId);
        Map<String, Object> result = new HashMap<>();
        result.put("followedByMe", !alreadyFollowing);
        result.put("followerCount", count);
        return ApiResponse.success(result);
    }

    /** Get list of artist IDs that a client follows. */
    @GetMapping("/following")
    public ApiResponse<List<Long>> getFollowing(@RequestParam Long clientId) {
        List<Long> artistIds = followRepository.findByClientId(clientId)
                .stream().map(Follow::getArtistId).toList();
        return ApiResponse.success(artistIds);
    }

    /** Delete account and all associated data. */
    @Transactional
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAccount(@PathVariable Long id, @RequestParam String password) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User user = opt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResponse.error("AUTH_ERROR", "Incorrect password");
        }
        // Remove reactions left by this client
        serviceReactionRepository.deleteByClientId(id);
        // Remove follows
        followRepository.findByClientId(id).forEach(followRepository::delete);
        // Remove appointments
        appointmentRepository.findByClientId(id).forEach(appointmentRepository::delete);
        appointmentRepository.findByArtistId(id).forEach(appointmentRepository::delete);
        // Remove services and their reactions (artist account)
        serviceRepository.findByArtistId(id).forEach(s -> {
            serviceReactionRepository.deleteByServiceId(s.getId());
            serviceRepository.delete(s);
        });
        // Remove reviews
        reviewRepository.deleteByArtistId(id);
        reviewRepository.deleteByClientId(id);
        // Remove user
        userRepository.delete(user);
        return ApiResponse.success("Account deleted");
    }

    /** Return follower + following counts for a user's own profile. */
    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> getUserStats(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("USER_NOT_FOUND", "User not found");
        User u = opt.get();
        long followers = followRepository.countByArtistId(id);
        long following = followRepository.countByClientId(id);
        Map<String, Object> stats = new HashMap<>();
        stats.put("followers", followers);
        stats.put("following", following);
        stats.put("role", u.getRole());
        return ApiResponse.success(stats);
    }
}
