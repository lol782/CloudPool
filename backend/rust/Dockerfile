# Build Rust FFI native shared library
FROM rust:1.75 AS builder
WORKDIR /usr/src/cloudpool-rust
COPY Cargo.toml Cargo.lock ./
COPY src ./src
RUN cargo build --release

FROM debian:bookworm-slim
COPY --from=builder /usr/src/cloudpool-rust/target/release/libcloudpool_rust.so /usr/local/lib/libcloudpool_rust.so
CMD ["ls", "-la", "/usr/local/lib"]
