# Contributing to CloudPool

Thank you for your interest in contributing to CloudPool!

## Getting Started

1. Fork the repository.
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/cloudpool.git
   ```
3. Create a branch:
   ```bash
   git checkout -b feature/your-feature
   ```
4. Make your changes.
5. Format and check your code.
6. Commit:
   ```bash
   git commit -am 'feat: add file compression'
   ```
7. Push:
   ```bash
   git push origin feature/your-feature
   ```
8. Open a Pull Request.

---

## Code Style & Formatting

- **Java**: Follow Google Java Style Guide.
- **Rust**: Use `rustfmt` via `cargo fmt`.
- **Spotless Formatting**: Execute Spotless checking inside the Maven folder:
  ```bash
  mvn spotless:apply
  ```

---

## Testing Requirements

- Unit tests must be provided for all new code.
- Integration tests must be updated for API changes.
- Project coverage should not drop below 80%.

To run all tests locally:
```bash
# In backend/spring-boot
mvn clean test
```

---

## Commit Messages

Follow Conventional Commits:

- `feat:`: New features.
- `fix:`: Bug fixes.
- `docs:`: Documentation updates.
- `test:`: Adding or updating tests.
- `refactor:`: Code refactor without interface changes.
- `chore:`: Dependencies updates or build script modifications.
