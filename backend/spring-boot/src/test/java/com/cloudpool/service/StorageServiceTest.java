package com.cloudpool.service;

import com.cloudpool.model.*;
import com.cloudpool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import com.cloudpool.util.FileUploadValidator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private GoogleDriveService googleDriveService;

    @Mock
    private FileShareRepository fileShareRepository;

    @Mock
    private QuotaService quotaService;
 
    @Mock
    private FileUploadValidator fileUploadValidator;

    @InjectMocks
    private StorageService storageService;

    private User testUser;
    private Bucket testBucket;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(storageService, "localDir", "./storage");

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .name("Test User")
                .role("USER")
                .active(true)
                .build();

        testBucket = Bucket.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("test-bucket")
                .build();

        testFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );
    }

    @Test
    void testUploadFileLocalSuccess() throws IOException {
        when(quotaService.reserveQuota(any(UUID.class), anyLong())).thenReturn(true);
        when(bucketRepository.findByUserAndName(any(User.class), anyString()))
                .thenReturn(Optional.of(testBucket));
        when(fileMetadataRepository.save(any(FileMetadata.class)))
                .thenAnswer(invocation -> {
                    FileMetadata f = invocation.getArgument(0);
                    f.setId(UUID.randomUUID());
                    return f;
                });

        FileMetadata result = storageService.uploadFile(testFile, "test-bucket", testUser);

        assertNotNull(result);
        assertEquals("test.txt", result.getOriginalName());
        verify(fileMetadataRepository).save(any(FileMetadata.class));
        verify(auditLogService).log(any(), any(), any(), any(), any());
    }

    @Test
    void testDownloadFileSuccess() throws IOException {
        UUID fileId = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .id(fileId)
                .bucket(testBucket)
                .name("test_file.txt")
                .originalName("test.txt")
                .driveFileId("drive_123")
                .isPublic(false)
                .build();

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        byte[] expectedContent = "drive content".getBytes();
        when(googleDriveService.downloadFile(eq("drive_123"), any(User.class)))
                .thenReturn(expectedContent);

        byte[] result = storageService.downloadFile(fileId, testUser);

        assertArrayEquals(expectedContent, result);
        verify(googleDriveService).downloadFile(eq("drive_123"), any(User.class));
    }
}
