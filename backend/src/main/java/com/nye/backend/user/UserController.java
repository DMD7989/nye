package com.nye.backend.user;

import com.nye.backend.security.SecurityUser;
import com.nye.backend.user.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Profil utilisateur")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return UserResponse.from(principal.getUser());
    }

    @PatchMapping
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        User user = principal.getUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setLanguage(request.language());
        return UserResponse.from(userRepository.save(user));
    }

    @PutMapping("/fcm-token")
    public UserResponse updateFcmToken(
            @Valid @RequestBody UpdateFcmTokenRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        User user = principal.getUser();
        user.setFcmToken(request.token());
        return UserResponse.from(userRepository.save(user));
    }

    @PutMapping("/location")
    public UserResponse updateLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        User user = principal.getUser();
        user.setLastKnownLatitude(request.latitude());
        user.setLastKnownLongitude(request.longitude());
        user.setLastLocationUpdatedAt(Instant.now());
        return UserResponse.from(userRepository.save(user));
    }

    @PutMapping("/notification-preferences")
    public UserResponse updateNotificationPreferences(
            @Valid @RequestBody UpdateNotificationPreferencesRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        User user = principal.getUser();
        user.setNotificationsEnabled(request.enabled());
        if (request.radiusKm() != null) {
            user.setNotificationRadiusKm(request.radiusKm());
        }
        return UserResponse.from(userRepository.save(user));
    }
}
