# CodeArena — Backend

Spring Boot 3.3 / Java 21 API for the CodeArena coding assessment platform: auth, problems, the sandboxed judge, contests, and a live leaderboard backed by MongoDB Atlas.

## Stack

- **Spring Boot 3.3**, Java 21
- **Spring Security** — stateless JWT auth (access + persisted, revocable refresh tokens), method-level `@PreAuthorize` for role checks
- **Spring Data MongoDB** — MongoDB Atlas for cloud document persistence (or local MongoDB for `dev` profile)
- **Spring WebSocket (STOMP)** — live leaderboard broadcast
- **jjwt** for token signing/parsing, **Lombok** + **MapStruct** wired into the Maven compiler plugin
- **springdoc-openapi** — Swagger UI at `/swagger-ui.html`
- Docker (invoked via `ProcessBuilder`, not a Java Docker client library) for the sandboxed judge

## Getting started

```bash
mvn spring-boot:run
```

This runs on the `dev` profile by default (see `application.yml`). Auth, problems, contests, and leaderboard endpoints all work. **Run/Submit need the judge images built** (next section), because `DockerJudgeService` genuinely shells out to `docker run`.

Swagger UI: http://localhost:8080/swagger-ui.html

## Setting up the judge

```bash
cd docker
./build-judge-images.sh
```

This builds `codearena/java`, `codearena/python`, `codearena/cpp`, `codearena/c`, `codearena/javascript` — see `docs/docker-sandbox-guide.md` (one level up, in the project root) for what each image contains and why the `docker run` flags in `DockerJudgeService` are what they are.

## Running everything with Docker Compose & MongoDB Atlas

```bash
cp .env.example .env   # then edit .env — set MONGODB_URI and JWT_SECRET
docker compose up --build
```

This mounts the host's Docker socket into the backend container so it can launch sibling judge containers.

## Project structure

```
src/main/java/com/codearena/
├── config/          SecurityConfig, JwtProperties, JudgeProperties, CorsProperties,
│                     WebSocketConfig, OpenApiConfig, MongoAuditingConfig
├── security/        JWT filter, token provider, UserDetails, 401 handler
├── common/           ApiResponse envelope, BaseEntity, GlobalExceptionHandler + custom exceptions
├── user/              User document/repo/service, Role enum
├── auth/               Register/login/refresh/logout, OTP email verification, password reset
├── problem/             Problem + TestCase documents, listing/detail/create, DTOs
├── submission/           Submission document, run/submit orchestration
│   └── judge/              DockerJudgeService, LanguageRuntime (per-language image + commands),
│                            OutputComparator, JudgeResult
├── contest/              Contest + ContestParticipant, create/list/detail/register
└── leaderboard/           On-demand standings computation + WebSocket broadcast
```

## Environment variables (production profile)

| Variable | Purpose | Default |
|---|---|---|
| `MONGODB_URI` | MongoDB Atlas Connection String (`mongodb+srv://...`) | see `application-prod.yml` |
| `JWT_SECRET` | HMAC signing key | dev-only placeholder |
| `JWT_ACCESS_EXPIRY_MS`, `JWT_REFRESH_EXPIRY_MS` | Token lifetimes | 15 min / 7 days |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list, must include your deployed frontend URL | `http://localhost:5173` |
| `JUDGE_WORKDIR`, `JUDGE_TIMEOUT_SECONDS`, `JUDGE_MEMORY_MB`, `JUDGE_CPU_LIMIT`, `JUDGE_PIDS_LIMIT` | Judge sandbox limits | see `application.yml` |
