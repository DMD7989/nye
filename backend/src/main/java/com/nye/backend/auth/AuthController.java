package com.nye.backend.auth;

import com.nye.backend.auth.dto.AuthResponse;
import com.nye.backend.auth.dto.LoginRequest;
import com.nye.backend.auth.dto.OtpRequest;
import com.nye.backend.auth.dto.OtpVerifyRequest;
import com.nye.backend.auth.dto.RegisterRequest;
import com.nye.backend.common.InvalidOtpException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequest request) {
        otpService.requestOtp(request.phone());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request.phone(), request.code());
        if (!verified) {
            throw new InvalidOtpException("Code de vérification invalide ou expiré");
        }
        return ResponseEntity.ok().build();
    }
}
