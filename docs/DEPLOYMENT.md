# CloudPool Deployment & DevOps Manual

This document provides complete details on configuring production Kubernetes cluster architectures, Ingress rules, Docker multi-stage builds, and CI/CD automation pipelines for CloudPool.

---

## ☸️ Kubernetes Cluster Architecture

The platform resources are packaged in container environments and orchestrated using Kubernetes. The manifest templates are placed under the `kubernetes/` folder in the root workspace.

### 1. Api Gateway / Spring Server Deployment (`kubernetes/deployment.yaml`)
Deployment contains 3 replica instances, utilizing rolling updates to guarantee zero-downtime, and includes health check actuator paths:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cloudpool-api
  namespace: cloudpool
  labels:
    app: cloudpool-api
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cloudpool-api
  template:
    metadata:
      labels:
        app: cloudpool-api
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: cloudpool-api
      
      initContainers:
      - name: migrate
        image: cloudpool:latest
        command: ["./mvnw", "flyway:migrate"]
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: cloudpool-secrets
              key: database-url
...
```

### 2. Service Definitions (`kubernetes/service.yaml`)
Enables cluster routing and exposes ports:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: cloudpool-api
  namespace: cloudpool
  labels:
    app: cloudpool-api
spec:
  type: ClusterIP
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  - name: metrics
    port: 9090
    targetPort: 9090
    protocol: TCP
  selector:
    app: cloudpool-api
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 3600
```

### 3. Ingress Controller Routing (`kubernetes/ingress.yaml`)
Nginx ingress annotations, path settings, rate-limiting, and Let's Encrypt TLS hooks:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: cloudpool-ingress
  namespace: cloudpool
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.cloudpool.example.com
    secretName: cloudpool-tls
  rules:
  - host: api.cloudpool.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: cloudpool-api
            port:
              name: http
```

---

## 📦 Containerization & Docker Builds

We implement multi-stage Docker builds to keep our target image footprint small and highly secure.

### Multi-Stage build steps
1. **Build stage**: Use JDK 17 with Maven caching to build the JAR.
2. **Native stage**: Build Cargo release artifacts.
3. **Execution stage**: Copy the compiled JNI bridge files and spring jar to a lightweight distroless or alpine container scope.

---

## 🛠️ GitHub Actions CI/CD Workflows

CI/CD configurations are located in `.github/workflows/`.

### 1. Build and Verification Suite (`.github/workflows/build.yml`)
Triggers on PRs and pushes to main/develop branches. Sets up PostgreSQL and Redis services, compiles Rust DLL, runs full testing suites, compiles maven package, and builds the container image.

### 2. CD Deployment Release (`.github/workflows/deploy.yml`)
Triggers on merge to main. Sets up kubectl configurations, tags and updates the deployment image, triggers rolling restarts, and runs a curl actuator health check ping verification.
