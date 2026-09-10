package com.nye.backend.user;

import com.nye.backend.common.ResourceNotFoundException;
import com.nye.backend.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Administration - Utilisateurs")
public class UserAdminController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @RequestParam Role role) {
        User user = findOrThrow(id);
        user.setRole(role);
        return UserResponse.from(userRepository.save(user));
    }

    @PatchMapping("/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id) {
        User user = findOrThrow(id);
        user.setEnabled(false);
        return UserResponse.from(userRepository.save(user));
    }

    @PatchMapping("/{id}/reactivate")
    public UserResponse reactivate(@PathVariable Long id) {
        User user = findOrThrow(id);
        user.setEnabled(true);
        return UserResponse.from(userRepository.save(user));
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }
}
