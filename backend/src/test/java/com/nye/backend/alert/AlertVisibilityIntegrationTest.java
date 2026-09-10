package com.nye.backend.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nye.backend.security.JwtService;
import com.nye.backend.security.SecurityUser;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie de bout en bout la règle centrale du cahier des charges §13.1/§13.2 :
 * une alerte en attente n'est visible en clair que par son auteur ou un admin ;
 * une fois validée, elle devient publique pour tout le monde.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertVisibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenFor(User user) {
        return jwtService.generateToken(new SecurityUser(user));
    }

    private User persistUser(String phone, Role role, boolean phoneVerified) {
        User user = User.builder()
                .fullName("Test " + phone)
                .phone(phone)
                .passwordHash(passwordEncoder.encode("Password123"))
                .role(role)
                .phoneVerified(phoneVerified)
                .build();
        return userRepository.save(user);
    }

    @Test
    void unverifiedUser_cannotPublishAlert() throws Exception {
        User unverified = persistUser("+22370090001", Role.USER, false);

        String body = """
                {"missingPersonName":"X","missingPersonAge":10,"missingPersonIsMinor":false,
                "description":"d","photoUrl":"https://example.com/p.jpg","contactPhone":"+22370090001",
                "latitude":12.64,"longitude":-8.00}
                """;

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + tokenFor(unverified))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void pendingAlert_isRestrictedForAnonymous_fullForAuthorAndAdmin_thenPublicOnceValidated() throws Exception {
        User author = persistUser("+22370090002", Role.USER, true);
        User admin = persistUser("+22300090099", Role.ADMIN, true);

        String createBody = """
                {"missingPersonName":"Ibrahim Konate","missingPersonAge":9,"missingPersonIsMinor":true,
                "description":"Vu pres du marche","photoUrl":"https://example.com/enfant.jpg",
                "contactPhone":"+22370090002","latitude":12.639217,"longitude":-8.002913,"address":"Bamako"}
                """;

        String createResponse = mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + tokenFor(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long alertId = objectMapper.readTree(createResponse).get("id").asLong();

        // Anonymous: restricted view — masked name, blurred position, no photo.
        String anonymousResponse = mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode anonymousJson = objectMapper.readTree(anonymousResponse);
        assertThat(anonymousJson.get("restricted").asBoolean()).isTrue();
        assertThat(anonymousJson.get("missingPersonName").asText()).isEqualTo("Ibrahim K.");
        assertThat(anonymousJson.get("photoUrl").isNull()).isTrue();
        assertThat(anonymousJson.get("latitude").asDouble()).isEqualTo(12.64);

        // Author: full view even though the alert is still pending.
        String authorResponse = mockMvc.perform(get("/api/alerts/" + alertId)
                        .header("Authorization", "Bearer " + tokenFor(author)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode authorJson = objectMapper.readTree(authorResponse);
        assertThat(authorJson.get("restricted").asBoolean()).isFalse();
        assertThat(authorJson.get("missingPersonName").asText()).isEqualTo("Ibrahim Konate");

        // A random other authenticated user still gets the restricted view.
        User stranger = persistUser("+22370090003", Role.USER, true);
        String strangerResponse = mockMvc.perform(get("/api/alerts/" + alertId)
                        .header("Authorization", "Bearer " + tokenFor(stranger)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(strangerResponse).get("restricted").asBoolean()).isTrue();

        // Non-admin cannot validate the alert.
        mockMvc.perform(patch("/api/admin/alerts/" + alertId + "/validate")
                        .header("Authorization", "Bearer " + tokenFor(stranger)))
                .andExpect(status().isForbidden());

        // Admin validates the alert.
        mockMvc.perform(patch("/api/admin/alerts/" + alertId + "/validate")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk());

        // Anonymous now sees the full, unrestricted details.
        String publicResponse = mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode publicJson = objectMapper.readTree(publicResponse);
        assertThat(publicJson.get("restricted").asBoolean()).isFalse();
        assertThat(publicJson.get("missingPersonName").asText()).isEqualTo("Ibrahim Konate");
        assertThat(publicJson.get("photoUrl").asText()).isEqualTo("https://example.com/enfant.jpg");
        assertThat(publicJson.get("latitude").asDouble()).isEqualTo(12.639217);
        assertThat(publicJson.get("status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    void suspiciousAlert_isFlaggedButStillCreated_andAdminCanRejectIt() throws Exception {
        User author = persistUser("+22370090005", Role.USER, true);
        User admin = persistUser("+22300090098", Role.ADMIN, true);
        User stranger = persistUser("+22370090006", Role.USER, true);

        String suspiciousBody = """
                {"missingPersonName":"X","missingPersonAge":30,"missingPersonIsMinor":false,
                "description":"Merci d'envoyer de l'argent pour financer les recherches.",
                "photoUrl":"https://example.com/p.jpg","contactPhone":"+22370090005",
                "latitude":12.64,"longitude":-8.00}
                """;

        String createResponse = mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + tokenFor(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode createdJson = objectMapper.readTree(createResponse);
        long alertId = createdJson.get("id").asLong();

        // Flagged automatically, but still created (not silently blocked) for admin review.
        assertThat(createdJson.get("moderationFlagged").asBoolean()).isTrue();
        assertThat(createdJson.get("status").asText()).isEqualTo("PENDING");

        // A non-admin cannot reject an alert.
        mockMvc.perform(patch("/api/admin/alerts/" + alertId + "/reject")
                        .header("Authorization", "Bearer " + tokenFor(stranger))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Contenu frauduleux\"}"))
                .andExpect(status().isForbidden());

        // Admin rejects it.
        mockMvc.perform(patch("/api/admin/alerts/" + alertId + "/reject")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Demande d'argent déguisée en avis de recherche\"}"))
                .andExpect(status().isOk());

        // A rejected alert stays hidden from the public...
        String anonymousResponse = mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(anonymousResponse).get("restricted").asBoolean()).isTrue();

        // ...but the author can still see the full details, including why it was rejected.
        String authorResponse = mockMvc.perform(get("/api/alerts/" + alertId)
                        .header("Authorization", "Bearer " + tokenFor(author)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode authorJson = objectMapper.readTree(authorResponse);
        assertThat(authorJson.get("restricted").asBoolean()).isFalse();
        assertThat(authorJson.get("status").asText()).isEqualTo("REJECTED");
        assertThat(authorJson.get("rejectionReason").asText()).isEqualTo("Demande d'argent déguisée en avis de recherche");

        // It never shows up in the public listing either.
        String listResponse = mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode list = objectMapper.readTree(listResponse);
        for (JsonNode item : list) {
            assertThat(item.get("id").asLong()).isNotEqualTo(alertId);
        }
    }

    @Test
    void nonAdmin_isBlockedFromAdminEndpoints() throws Exception {
        User user = persistUser("+22370090004", Role.USER, true);

        mockMvc.perform(get("/api/admin/alerts")
                        .header("Authorization", "Bearer " + tokenFor(user)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .header("Authorization", "Bearer " + tokenFor(user)))
                .andExpect(status().isForbidden());
    }
}
