package com.cloudpool.service;

import com.cloudpool.model.AuditLog;
import com.cloudpool.model.User;
import com.cloudpool.repository.AuditLogRepository;
import com.cloudpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatSyncService {

    private final UserRepository userRepository;
    private final GoogleDriveService googleDriveService;
    private final AuditLogRepository auditLogRepository;

    /**
     * scheduled task running every hour to check validity of refresh tokens.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void validateContributorTokens() {
        log.info("Starting scheduled Google Drive refresh token validity check...");
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.getGoogleRefreshToken() != null && user.isActive()) {
                boolean isValid = googleDriveService.validateRefreshToken(user);
                if (!isValid) {
                    log.warn("Suspending user {} due to dead Google OAuth refresh token", user.getEmail());
                    user.setActive(false);
                    userRepository.save(user);

                    AuditLog audit = AuditLog.builder()
                            .user(user)
                            .action("SUSPEND_CONTRIBUTOR_DRIVE")
                            .resourceType("USER")
                            .resourceId(user.getId().toString())
                            .details(String.format("User %s account suspended automatically because linked Google Drive refresh token is invalid/revoked.", user.getEmail()))
                            .build();
                    auditLogRepository.save(audit);
                }
            }
        }
        log.info("Google Drive refresh token validity check completed.");
    }
}
