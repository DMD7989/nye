package com.nye.backend.config;

import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByPhone("+22300000000")) {
            return;
        }
        User admin = User.builder()
                .fullName("Admin N'yé")
                .phone("+22300000000")
                .email("admin@nye.local")
                .passwordHash(passwordEncoder.encode("Admin1234!"))
                .role(Role.ADMIN)
                .phoneVerified(true)
                .build();
        userRepository.save(admin);

        User demoUser = User.builder()
                .fullName("Utilisateur Démo")
                .phone("+22311111111")
                .email("demo@nye.local")
                .passwordHash(passwordEncoder.encode("Demo1234!"))
                .role(Role.USER)
                .phoneVerified(true)
                .build();
        userRepository.save(demoUser);
    }
}
