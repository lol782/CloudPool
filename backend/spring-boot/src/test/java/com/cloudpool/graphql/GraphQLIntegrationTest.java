package com.cloudpool.graphql;

import com.cloudpool.model.*;
import com.cloudpool.repository.BucketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
public class GraphQLIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private BucketRepository bucketRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .role("USER")
                .active(true)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testHealthCheckQuery() {
        String document = "query { healthCheck }";

        graphQlTester.document(document)
                .execute()
                .path("healthCheck")
                .entity(String.class)
                .isEqualTo("OK");
    }

    @Test
    void testMeQuery() {
        String document = "query { me { email name } }";

        graphQlTester.document(document)
                .execute()
                .path("me.email")
                .entity(String.class)
                .isEqualTo("test@example.com");
    }

    @Test
    void testCreateBucketMutation() {
        String document = "mutation { createBucket(name: \"test-bucket\", description: \"desc\") { name description } }";

        Bucket bucket = Bucket.builder()
                .name("test-bucket")
                .description("desc")
                .build();

        Mockito.when(bucketRepository.save(any(Bucket.class))).thenReturn(bucket);

        graphQlTester.document(document)
                .execute()
                .path("createBucket.name")
                .entity(String.class)
                .isEqualTo("test-bucket");
    }
}
