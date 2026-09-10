package com.nye.backend.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nye.backend.security.JwtService;
import com.nye.backend.security.SecurityUser;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void uploadPhoto_requiresAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/uploads/photo").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadPhoto_returnsAReachableUrl_forAnAuthenticatedUser() throws Exception {
        User user = userRepository.save(User.builder()
                .fullName("Uploader").phone("+22370099001")
                .passwordHash(passwordEncoder.encode("Password123"))
                .role(Role.USER).phoneVerified(true).build());
        String token = jwtService.generateToken(new SecurityUser(user));

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5});

        String response = mockMvc.perform(multipart("/api/uploads/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String url = objectMapper.readTree(response).get("url").asText();
        assertThat(url).startsWith("http://localhost:8080/uploads/").endsWith(".jpg");
    }

    @Test
    void uploadPhoto_rejectsNonImageContentType() throws Exception {
        User user = userRepository.save(User.builder()
                .fullName("Uploader2").phone("+22370099002")
                .passwordHash(passwordEncoder.encode("Password123"))
                .role(Role.USER).phoneVerified(true).build());
        String token = jwtService.generateToken(new SecurityUser(user));

        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/uploads/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnsupportedMediaType());
    }
}
