package com.nye.backend.auth;

import com.nye.backend.auth.dto.AuthResponse;
import com.nye.backend.auth.dto.LoginRequest;
import com.nye.backend.auth.dto.RegisterRequest;
import com.nye.backend.common.DuplicateResourceException;
import com.nye.backend.security.JwtService;
import com.nye.backend.security.SecurityUser;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Un compte existe déjà avec ce numéro de téléphone");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .phone(request.phone())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .phoneVerified(false)
                .build();
        user = userRepository.save(user);

        // Le numéro doit être vérifié par OTP avant de pouvoir publier une alerte (cahier des charges §12.2).
        otpService.requestOtp(user.getPhone());

        String token = jwtService.generateToken(new SecurityUser(user));
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole(), user.isPhoneVerified());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.phone(), request.password()));

        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable après authentification"));

        String token = jwtService.generateToken(new SecurityUser(user));
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole(), user.isPhoneVerified());
    }
}
