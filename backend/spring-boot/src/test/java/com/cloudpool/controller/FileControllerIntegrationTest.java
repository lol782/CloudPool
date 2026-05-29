package com.cloudpool.controller;

import com.cloudpool.model.*;
import com.cloudpool.repository.*;
import com.cloudpool.service.AuditLogService;
import com.cloudpool.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private FileShareRepository fileShareRepository;

    @MockBean
    private FileMetadataRepository fileMetadataRepository;

    private User testUser;
    private FileMetadata mockMetadata;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .role("USER")
                .active(true)
                .build();

        mockMetadata = FileMetadata.builder()
                .id(UUID.randomUUID())
                .name("test.txt")
                .originalName("test.txt")
                .size(12L)
                .mimeType("text/plain")
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testUploadFileEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        Mockito.when(storageService.uploadFile(any(), anyString(), any(User.class)))
                .thenReturn(mockMetadata);

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("bucket", "test-bucket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.txt"));
    }

    @Test
    void testListFilesEndpoint() throws Exception {
        Mockito.when(storageService.listUserFiles(any(User.class)))
                .thenReturn(Collections.singletonList(mockMetadata));

        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test.txt"));
    }
}
