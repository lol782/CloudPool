package com.cloudpool.repository;

import com.cloudpool.model.VectorCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VectorCollectionRepository extends JpaRepository<VectorCollection, UUID> {
    List<VectorCollection> findByUserId(UUID userId);
    Optional<VectorCollection> findByUserIdAndName(UUID userId, String name);
}
