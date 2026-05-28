use crate::{CloudpoolError, Result};

/// Vector service for handling vector operations
pub struct VectorService;

impl VectorService {
    /// Calculate cosine similarity between two vectors
    pub fn cosine_similarity(vec1: &[f32], vec2: &[f32]) -> Result<f32> {
        if vec1.len() != vec2.len() {
            return Err(CloudpoolError::InvalidInput(
                "Vectors must have the same dimension".to_string()
            ));
        }

        let dot_product: f32 = vec1
            .iter()
            .zip(vec2.iter())
            .map(|(a, b)| a * b)
            .sum();

        let magnitude1 = (vec1.iter().map(|x| x * x).sum::<f32>()).sqrt();
        let magnitude2 = (vec2.iter().map(|x| x * x).sum::<f32>()).sqrt();

        if magnitude1 == 0.0 || magnitude2 == 0.0 {
            return Ok(0.0);
        }

        Ok(dot_product / (magnitude1 * magnitude2))
    }

    /// Calculate Euclidean distance between two vectors
    pub fn euclidean_distance(vec1: &[f32], vec2: &[f32]) -> Result<f32> {
        if vec1.len() != vec2.len() {
            return Err(CloudpoolError::InvalidInput(
                "Vectors must have the same dimension".to_string()
            ));
        }

        let sum: f32 = vec1
            .iter()
            .zip(vec2.iter())
            .map(|(a, b)| (a - b).powi(2))
            .sum();

        Ok(sum.sqrt())
    }

    /// Normalize vector
    pub fn normalize(vector: &[f32]) -> Result<Vec<f32>> {
        let magnitude = (vector.iter().map(|x| x * x).sum::<f32>()).sqrt();

        if magnitude == 0.0 {
            return Err(CloudpoolError::InvalidInput(
                "Cannot normalize zero vector".to_string()
            ));
        }

        Ok(vector.iter().map(|x| x / magnitude).collect())
    }

    /// Find k nearest neighbors
    pub fn knn(
        query_vector: &[f32],
        vectors: &[Vec<f32>],
        k: usize,
    ) -> Result<Vec<(usize, f32)>> {
        let mut distances: Vec<(usize, f32)> = vectors
            .iter()
            .enumerate()
            .map(|(idx, vec)| {
                let dist = Self::cosine_similarity(query_vector, vec)
                    .unwrap_or(0.0);
                (idx, dist)
            })
            .collect();

        distances.sort_by(|a, b| {
            b.1.partial_cmp(&a.1)
                .unwrap_or(std::cmp::Ordering::Equal)
        });

        Ok(distances.into_iter().take(k).collect())
    }
}
