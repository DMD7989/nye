package com.nye.backend.auth;

import com.nye.backend.common.ResourceNotFoundException;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private UserRepository userRepository;

    private OtpService otpService;
    private User user;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(userRepository);
        user = User.builder()
                .id(1L)
                .fullName("Test User")
                .phone("+22370000000")
                .passwordHash("hash")
                .role(Role.USER)
                .phoneVerified(false)
                .build();
    }

    @Test
    void requestOtp_setsCodeAndExpiry_andSavesUser() {
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        otpService.requestOtp(user.getPhone());

        assertThat(user.getOtpCode()).matches("\\d{6}");
        assertThat(user.getOtpExpiresAt()).isAfter(Instant.now());
        verify(userRepository).save(user);
    }

    @Test
    void requestOtp_throws_whenUserNotFound() {
        when(userRepository.findByPhone("+22300000099")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.requestOtp("+22300000099"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void verifyOtp_returnsTrue_andMarksVerified_forCorrectUnexpiredCode() {
        user.setOtpCode("123456");
        user.setOtpExpiresAt(Instant.now().plus(4, ChronoUnit.MINUTES));
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = otpService.verifyOtp(user.getPhone(), "123456");

        assertThat(result).isTrue();
        assertThat(user.isPhoneVerified()).isTrue();
        assertThat(user.getOtpCode()).isNull();
        assertThat(user.getOtpExpiresAt()).isNull();
    }

    @Test
    void verifyOtp_returnsFalse_forWrongCode() {
        user.setOtpCode("123456");
        user.setOtpExpiresAt(Instant.now().plus(4, ChronoUnit.MINUTES));
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));

        boolean result = otpService.verifyOtp(user.getPhone(), "000000");

        assertThat(result).isFalse();
        assertThat(user.isPhoneVerified()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtp_returnsFalse_forExpiredCode() {
        user.setOtpCode("123456");
        user.setOtpExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));

        boolean result = otpService.verifyOtp(user.getPhone(), "123456");

        assertThat(result).isFalse();
        assertThat(user.isPhoneVerified()).isFalse();
    }

    @Test
    void verifyOtp_returnsFalse_whenNoCodeWasEverRequested() {
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));

        boolean result = otpService.verifyOtp(user.getPhone(), "123456");

        assertThat(result).isFalse();
    }
}
