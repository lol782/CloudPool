# CloudPool Release Process

## Version Numbering
We follow Semantic Versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes.
- **MINOR**: New features (backward-compatible).
- **PATCH**: Bug fixes (backward-compatible).

---

## Release Steps

1. **Prepare Release**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Update Version**
   - Update version tag in `backend/spring-boot/pom.xml`.
   - Update version string in `backend/rust/Cargo.toml`.
   - Update root `VERSION` file.

3. **Update Changelog**
   - Document new changes inside `CHANGELOG.md`.

4. **Run Tests**
   Ensure all local compilations and JNI bindings are stable:
   ```bash
   mvn clean verify
   cargo test
   ```

5. **Create Tag**
   ```bash
   git tag -a v0.1.0 -m "Release version 0.1.0"
   git push origin v0.1.0
   ```

6. **Build & Push Docker Image**
   ```bash
   docker build -t cloudpool:0.1.0 .
   docker push cloudpool:0.1.0
   ```

7. **Create GitHub Release**
   - Navigate to **Releases** on the GitHub repository page.
   - Click **Draft a new release** and select the tag you just pushed.
   - Add detailed release notes summarizing new features, bug fixes, and breaking changes.
   - Attach any build artifacts (JAR files, Docker image digests).

8. **Announce Release**
   - Post on community forums and discussion channels.
   - Update the project documentation site with the new version.
   - Send a newsletter to subscribed users and contributors.

---

## Rollback Process
If critical issues are detected post-release, initiate rollback tags:

```bash
git revert <commit-hash>
git tag v0.1.1 -m "Rollback v0.1.0"
docker push cloudpool:0.1.1
```
