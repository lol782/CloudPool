package com.cloudpool.listener;

import com.cloudpool.config.RabbitMQConfig;
import com.cloudpool.model.BackgroundJob;
import com.cloudpool.model.FileMetadata;
import com.cloudpool.repository.BackgroundJobRepository;
import com.cloudpool.repository.FileMetadataRepository;
import com.cloudpool.service.StorageService;
import com.cloudpool.util.RustBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessingListener {

    private final StorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final BackgroundJobRepository jobRepository;

    /**
     * Listen for file processing tasks
     */
    @RabbitListener(queues = RabbitMQConfig.FILE_PROCESSING_QUEUE)
    public void processFile(String jobIdStr) {
        log.info("Received background job ID: {}", jobIdStr);
        UUID jobId = UUID.fromString(jobIdStr);
        Optional<BackgroundJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.error("Job not found: {}", jobId);
            return;
        }

        BackgroundJob job = jobOpt.get();
        job.setStatus("RUNNING");
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            String payload = job.getPayload();
            String fileIdPrefix = "\"fileId\":\"";
            int startIdx = payload.indexOf(fileIdPrefix);
            if (startIdx != -1) {
                startIdx += fileIdPrefix.length();
                int endIdx = payload.indexOf("\"", startIdx);
                if (endIdx != -1) {
                    String fileIdStr = payload.substring(startIdx, endIdx);
                    UUID fileId = UUID.fromString(fileIdStr);
                    log.info("Processing file metadata in background: {}", fileId);
                    
                    Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findById(fileId);
                    if (fileMetadataOpt.isPresent()) {
                        FileMetadata metadata = fileMetadataOpt.get();
                        log.info("File original name: {}, size: {}", metadata.getOriginalName(), metadata.getSize());
                        
                        // Perform native checksum computation
                        byte[] fileData = storageService.downloadFileDirectly(metadata);
                        if (RustBridge.isLibraryLoaded() && fileData != null && fileData.length > 0) {
                            String checksum = RustBridge.calculateChecksum(fileData);
                            log.info("Computed native checksum: {}", checksum);
                        }
                    }
                }
            }

            job.setStatus("COMPLETED");
            log.info("Background job {} completed successfully", jobId);
        } catch (Exception e) {
            log.error("Error executing background job", e);
            job.setStatus("FAILED");
        } finally {
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    /**
     * Listen for embedding tasks
     */
    @RabbitListener(queues = RabbitMQConfig.EMBEDDING_QUEUE)
    public void processEmbedding(String jobIdStr) {
        log.info("Received embedding job ID: {}", jobIdStr);
        UUID jobId = UUID.fromString(jobIdStr);
        Optional<BackgroundJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.error("Job not found: {}", jobId);
            return;
        }

        BackgroundJob job = jobOpt.get();
        job.setStatus("RUNNING");
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            job.setStatus("COMPLETED");
            log.info("Embedding job {} completed successfully", jobId);
        } catch (Exception e) {
            log.error("Error executing embedding job", e);
            job.setStatus("FAILED");
        } finally {
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
}
