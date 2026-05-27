package com.cloudpool.repository;

import com.cloudpool.model.DevTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DevTableRepository extends JpaRepository<DevTable, UUID> {
    Page<DevTable> findByUserId(UUID userId, Pageable pageable);
    Page<DevTable> findByProjectId(UUID projectId, Pageable pageable);
    Optional<DevTable> findByUserIdAndName(UUID userId, String name);
    Optional<DevTable> findByProjectIdAndName(UUID projectId, String name);
    boolean existsByName(String name);
}
