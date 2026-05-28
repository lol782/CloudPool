use thiserror::Error;

#[derive(Error, Debug)]
pub enum CloudpoolError {
    #[error("File not found: {0}")]
    FileNotFound(String),

    #[error("IO error: {0}")]
    IoError(#[from] std::io::Error),

    #[error("Serialization error: {0}")]
    SerializationError(#[from] serde_json::Error),

    #[error("Invalid input: {0}")]
    InvalidInput(String),

    #[error("Database error: {0}")]
    DatabaseError(String),

    #[error("Cache error: {0}")]
    CacheError(String),

    #[error("Unknown error")]
    Unknown,
}

pub type Result<T> = std::result::Result<T, CloudpoolError>;
