package com.nye.backend.auth;

import com.nye.backend.common.ResourceNotFoundException;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * Vérification du téléphone par code à usage unique (Nyé cahier des charges §13.2).
 * L'envoi réel par SMS n'est pas branché ici : en attendant l'intégration d'une passerelle SMS,
 * le code est journalisé côté serveur (comportement dev uniquement).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;

    public void requestOtp(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte avec ce numéro : " + phone));

        String code = String.format("%06d", random.nextInt(1_000_000));
        user.setOtpCode(code);
        user.setOtpExpiresAt(Instant.now().plus(OTP_VALIDITY));
        userRepository.save(user);

        // TODO: brancher une passerelle SMS (Orange, Twilio, etc.) avant la mise en production.
        log.info("[OTP] Code de vérification pour {} : {} (valide 5 minutes)", phone, code);
    }

    public boolean verifyOtp(String phone, String code) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte avec ce numéro : " + phone));

        boolean valid = user.getOtpCode() != null
                && user.getOtpCode().equals(code)
                && user.getOtpExpiresAt() != null
                && user.getOtpExpiresAt().isAfter(Instant.now());

        if (valid) {
            user.setPhoneVerified(true);
            user.setOtpCode(null);
            user.setOtpExpiresAt(null);
            userRepository.save(user);
        }

        return valid;
    }
}
