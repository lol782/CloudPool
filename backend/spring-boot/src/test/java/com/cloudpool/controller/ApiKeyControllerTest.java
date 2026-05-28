package com.cloudpool.controller;

import com.cloudpool.dto.ApiKeyUsageLogDto;
import com.cloudpool.model.ApiKey;
import com.cloudpool.model.User;
import com.cloudpool.repository.ApiKeyRepository;
import com.cloudpool.service.ApiKeyUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private ApiKeyUsageService apiKeyUsageService;

    private User testUser;
    private ApiKey mockKey;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("dev@example.com")
                .name("Developer User")
                .role("USER")
                .active(true)
                .build();

        mockKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("test-key")
                .description("Console generated key")
                .keyHash("hashed_value")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testListKeys() throws Exception {
        Mockito.when(apiKeyRepository.findByUser(any(User.class)))
                .thenReturn(Collections.singletonList(mockKey));

        mockMvc.perform(get("/api/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test-key"));
    }

    @Test
    void testGenerateKey() throws Exception {
        Mockito.when(apiKeyRepository.save(any(ApiKey.class)))
                .thenReturn(mockKey);

        Map<String, Object> req = new HashMap<>();
        req.put("name", "new-key");
        req.put("daysToLive", 30);

        mockMvc.perform(post("/api/keys/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-key"))
                .andExpect(jsonPath("$.apiKey").exists());
    }

    @Test
    void testDeleteKeySuccess() throws Exception {
        Mockito.when(apiKeyRepository.findById(mockKey.getId()))
                .thenReturn(Optional.of(mockKey));

        mockMvc.perform(delete("/api/keys/" + mockKey.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("API key deleted successfully"));

        Mockito.verify(apiKeyRepository, Mockito.times(1)).delete(mockKey);
    }

    @Test
    void testDeleteKeyAccessDenied() throws Exception {
        User otherUser = User.builder().id(UUID.randomUUID()).build();
        mockKey.setUser(otherUser);

        Mockito.when(apiKeyRepository.findById(mockKey.getId()))
                .thenReturn(Optional.of(mockKey));

        mockMvc.perform(delete("/api/keys/" + mockKey.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));

        Mockito.verify(apiKeyRepository, Mockito.never()).delete(any());
    }

    @Test
    void testGetAnalyticsLogs() throws Exception {
        ApiKeyUsageLogDto logDto = ApiKeyUsageLogDto.builder()
                .id(UUID.randomUUID())
                .apiKeyName("test-key")
                .endpoint("/api/files")
                .method("GET")
                .statusCode(200)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(apiKeyUsageService.getLogsForUser(any(User.class)))
                .thenReturn(Collections.singletonList(logDto));

        mockMvc.perform(get("/api/keys/analytics/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endpoint").value("/api/files"))
                .andExpect(jsonPath("$[0].statusCode").value(200));
    }
}
