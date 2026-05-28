use crate::{CloudpoolError, Result};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::{SystemTime, UNIX_EPOCH};

/// In-memory cache with TTL support
pub struct Cache {
    data: Arc<Mutex<HashMap<String, CacheEntry>>>,
}

struct CacheEntry {
    value: Vec<u8>,
    expires_at: u64,
}

impl Cache {
    /// Create new cache
    pub fn new() -> Self {
        Cache {
            data: Arc::new(Mutex::new(HashMap::new())),
        }
    }

    /// Set value with TTL (in seconds)
    pub fn set(&self, key: String, value: Vec<u8>, ttl: u64) -> Result<()> {
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .map_err(|_| CloudpoolError::Unknown)?
            .as_secs();

        let entry = CacheEntry {
            value,
            expires_at: now + ttl,
        };

        let mut data = self.data.lock()
            .map_err(|_| CloudpoolError::CacheError("Lock failed".to_string()))?;
        
        data.insert(key, entry);
        Ok(())
    }

    /// Get value
    pub fn get(&self, key: &str) -> Result<Option<Vec<u8>>> {
        let mut data = self.data.lock()
            .map_err(|_| CloudpoolError::CacheError("Lock failed".to_string()))?;

        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .map_err(|_| CloudpoolError::Unknown)?
            .as_secs();

        if let Some(entry) = data.get(key) {
            if entry.expires_at > now {
                return Ok(Some(entry.value.clone()));
            } else {
                data.remove(key);
            }
        }

        Ok(None)
    }

    /// Delete value
    pub fn delete(&self, key: &str) -> Result<()> {
        let mut data = self.data.lock()
            .map_err(|_| CloudpoolError::CacheError("Lock failed".to_string()))?;
        
        data.remove(key);
        Ok(())
    }

    /// Clear cache
    pub fn clear(&self) -> Result<()> {
        let mut data = self.data.lock()
            .map_err(|_| CloudpoolError::CacheError("Lock failed".to_string()))?;
        
        data.clear();
        Ok(())
    }

    /// Get cache size
    pub fn size(&self) -> Result<usize> {
        let data = self.data.lock()
            .map_err(|_| CloudpoolError::CacheError("Lock failed".to_string()))?;
        
        Ok(data.len())
    }
}
