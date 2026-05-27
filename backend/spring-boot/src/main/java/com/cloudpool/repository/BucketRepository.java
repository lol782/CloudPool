package com.cloudpool.repository;

import com.cloudpool.model.Bucket;
import com.cloudpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, UUID> {
    List<Bucket> findByUser(User user);
    Optional<Bucket> findByUserAndName(User user, String name);
}
