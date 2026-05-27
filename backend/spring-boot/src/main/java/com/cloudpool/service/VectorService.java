package com.cloudpool.service;

import com.cloudpool.model.FileMetadata;
import com.cloudpool.model.User;
import com.cloudpool.repository.FileMetadataRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VectorService {

    private final FileMetadataRepository fileMetadataRepository;

    public List<VectorSearchResult> search(String query, User user) {
        List<FileMetadata> files = fileMetadataRepository.findByUserId(user.getId());
        List<VectorSearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String[] queryTerms = query.toLowerCase().split("\\s+");

        for (FileMetadata file : files) {
            double score = computeRelevanceScore(file, queryTerms);
            if (score > 0) {
                results.add(new VectorSearchResult(file, score));
            }
        }

        results.sort(Comparator.comparingDouble(VectorSearchResult::getScore).reversed());
        return results;
    }

    private double computeRelevanceScore(FileMetadata file, String[] queryTerms) {
        double score = 0.0;
        String name = file.getOriginalName().toLowerCase();
        String ext = file.getExtension() != null ? file.getExtension().toLowerCase() : "";
        String bucket = file.getBucket().getName().toLowerCase();

        // Load content if it's text
        String content = "";
        if ("txt".equalsIgnoreCase(ext) && file.getDriveLocation() != null) {
            try {
                Path path = Paths.get(file.getDriveLocation());
                if (Files.exists(path)) {
                    content = new String(Files.readAllBytes(path)).toLowerCase();
                }
            } catch (IOException e) {
                // Ignore content reading errors
            }
        }

        for (String term : queryTerms) {
            if (name.contains(term)) {
                score += 10.0; // High match for filename
            }
            if (bucket.contains(term)) {
                score += 5.0; // Match in pool name
            }
            if (ext.equals(term)) {
                score += 3.0; // Match in extension
            }
            if (!content.isEmpty() && content.contains(term)) {
                // Count occurrences
                int occurrences = countOccurrences(content, term);
                score += Math.min(occurrences * 0.5, 8.0); // Caps content score contribution
            }
        }

        return score;
    }

    private int countOccurrences(String text, String find) {
        int count = 0;
        int lastIdx = 0;
        while ((lastIdx = text.indexOf(find, lastIdx)) != -1) {
            count++;
            lastIdx += find.length();
        }
        return count;
    }

    @Data
    public static class VectorSearchResult {
        private UUID id;
        private String name;
        private String pool;
        private long size;
        private String type;
        private double score;

        public VectorSearchResult(FileMetadata file, double score) {
            this.id = file.getId();
            this.name = file.getOriginalName();
            this.pool = file.getBucket().getName();
            this.size = file.getSize();
            this.type = file.getMimeType();
            this.score = Math.round(score * 100.0) / 100.0; // Round to 2 decimal places
        }
    }
}
