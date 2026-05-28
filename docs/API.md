# CloudPool API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All REST API endpoints require authentication using a Bearer token in the `Authorization` header, or a custom API key in the `X-API-KEY` header.

```http
Authorization: Bearer <JWT_TOKEN>
X-API-KEY: <API_KEY>
```

---

## 📂 REST API Endpoints

### 1. Files & Storage

#### Upload File
* **Endpoint**: `POST /files/upload`
* **Content-Type**: `multipart/form-data`
* **Parameters**:
  * `file` (Multipart file, required): The binary file to upload.
  * `bucket` (String, optional, default: `default-pool`): The name of the bucket/pool to upload the file to.
* **Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "test.txt",
  "originalName": "test.txt",
  "size": 1024,
  "mimeType": "text/plain",
  "extension": "txt",
  "driveLocation": "Google Drive / local_path",
  "driveFileId": "drive_12345",
  "public": false,
  "encrypted": false
}
```

#### List Files
* **Endpoint**: `GET /files`
* **Response (200 OK)**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "test.txt",
    "originalName": "test.txt",
    "size": 1024,
    "mimeType": "text/plain"
  }
]
```

#### Download File
* **Endpoint**: `GET /files/download/{id}`
* **Response (200 OK)**:
* Binary stream of the file content.

#### Share File
* **Endpoint**: `POST /files/{fileId}/share`
* **Request Body**:
```json
{
  "sharedWithEmail": "colleague@example.com",
  "expiryHours": 24
}
```
* **Response (200 OK)**:
```json
{
  "id": "8c0a3794-ff3d-4c8d-8fb2-3788a1e2fba3",
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "sharedWithEmail": "colleague@example.com",
  "token": "4693e5008cf448b1aa1b67f1b63795b5",
  "expiresAt": "2026-05-29T20:20:11"
}
```

#### Download Shared File (Anonymous)
* **Endpoint**: `GET /files/shared/{token}`
* **Response (200 OK)**:
* Binary stream of the shared file content.

---

### 2. Relational Database Orchestration

#### Create Table
* **Endpoint**: `POST /db/tables`
* **Request Body**:
```json
{
  "name": "dev_cust_leads",
  "displayName": "Customer Leads",
  "description": "User records for advertising campaigns",
  "schema": {
    "id": "UUID",
    "email": "STRING",
    "age": "INTEGER",
    "is_active": "BOOLEAN"
  }
}
```

#### Insert Record
* **Endpoint**: `POST /db/tables/{tableId}/records`
* **Request Body**:
```json
{
  "email": "john@example.com",
  "age": 30,
  "is_active": true
}
```

#### Query Records
* **Endpoint**: `GET /db/tables/{tableId}/records?page=1&size=20`

---

### 3. Vector Search Engine

#### Create Collection
* **Endpoint**: `POST /vector/collections`
* **Request Body**:
```json
{
  "name": "documents",
  "description": "Document embeddings",
  "dimension": 1536,
  "distanceMetric": "cosine"
}
```

#### Index Document
* **Endpoint**: `POST /vector/collections/{collectionId}/documents`
* **Request Body**:
```json
{
  "docId": "doc_001",
  "content": "The quick brown fox jumps over the lazy dog.",
  "metadata": {
    "source": "document.pdf"
  }
}
```

#### Search Collection
* **Endpoint**: `POST /vector/collections/{collectionId}/search`
* **Request Body**:
```json
{
  "query": "Jumping animals",
  "limit": 10
}
```
* **Response (200 OK)**:
```json
[
  {
    "docId": "doc_001",
    "content": "The quick brown fox jumps over the lazy dog.",
    "score": 0.8542
  }
]
```

---

## 🕸️ GraphQL API Endpoints

### Endpoint
```
POST http://localhost:8080/graphql
```

### Playground (GraphiQL)
```
http://localhost:8080/graphiql
```

### Sample Query (Fetch User Profile & Files)
```graphql
query {
  me {
    email
    name
    role
  }
  files {
    id
    name
    size
  }
}
```

### Sample Mutation (Create Storage Bucket)
```graphql
mutation {
  createBucket(name: "production-backup", description: "Storage for staging DB backups") {
    id
    name
    description
  }
}
```

---

## ⚠️ Error Handling

All errors follow a consistent JSON format:

```json
{
  "success": false,
  "error": {
    "code": "FILE_NOT_FOUND",
    "message": "File not found",
    "details": "File with ID 123 not found"
  }
}
```

### Error Codes

| HTTP Code | Meaning |
|-----------|---------|
| `400` | Bad Request — Invalid parameters or malformed request body |
| `401` | Unauthorized — Missing or invalid authentication token |
| `403` | Forbidden — Insufficient permissions for the requested resource |
| `404` | Not Found — Resource does not exist |
| `409` | Conflict — Resource already exists or state conflict |
| `413` | Payload Too Large — File exceeds the maximum upload size |
| `429` | Too Many Requests — Rate limit exceeded |
| `500` | Internal Server Error — Unexpected server-side failure |

---

## 🚦 Rate Limiting

Rate limits are applied per authenticated user:

| Limit | Value |
|-------|-------|
| API calls per minute | 100 |
| Concurrent file uploads | 5 |

Rate limit status is returned in response headers:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1705318200
```

When the rate limit is exceeded, the API returns a `429 Too Many Requests` response with a `Retry-After` header indicating how many seconds to wait before retrying.
