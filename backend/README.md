# CodeArena — Backend

Spring Boot 3.3 / Java 21 API for the CodeArena coding assessment platform: auth, problems, the sandboxed judge, contests, and a live leaderboard.

## ⚠️ Important: this hasn't been compiled in the environment that wrote it

I wrote this backend in a sandbox whose network allowlist doesn't include
Maven Central, so I could not run `mvn compile` / `mvn test` here to verify
it builds. I've been careful with imports, method signatures, and Spring
wiring (and the annotation processors for Lombok/MapStruct are configured
in `pom.xml`), but **please run `mvn clean compile` yourself before relying
on this** — treat it as a thorough first draft, not a build-verified
artifact. If something doesn't compile, it's most likely a small
import/annotation mismatch, not a structural problem — check the file the
stack trace points to first.

## Stack

- **Spring Boot 3.3**, Java 21
- **Spring Security** — stateless JWT auth (access + persisted, revocable refresh tokens), method-level `@PreAuthorize` for role checks
- **Spring Data JPA** / Hibernate — MySQL in production, H2 in-memory for `dev` profile (no DB setup needed to try it locally)
- **Spring WebSocket (STOMP)** — live leaderboard broadcast
- **jjwt** for token signing/parsing, **Lombok** + **MapStruct** wired into the Maven compiler plugin
- **springdoc-openapi** — Swagger UI at `/swagger-ui.html`
- Docker (invoked via `ProcessBuilder`, not a Java Docker client library) for the sandboxed judge

## Getting started (no Docker judge, no MySQL — fastest path)

```bash
mvn spring-boot:run
```

This runs on the `dev` profile by default (see `application.yml`): H2
in-memory database, auto-created schema, `/h2-console` enabled. Auth,
problems, contests, and leaderboard endpoints all work. **Run/Submit will
fail** until you build the judge images (next section), because
`DockerJudgeService` genuinely shells out to `docker run`.

Swagger UI: http://localhost:8080/swagger-ui.html

## Setting up the judge

```bash
cd docker
./build-judge-images.sh
```

This builds `codearena/java`, `codearena/python`, `codearena/cpp`,
`codearena/c`, `codearena/javascript` — see `docs/docker-sandbox-guide.md`
(one level up, in the project root) for what each image contains and why
the `docker run` flags in `DockerJudgeService` are what they are.

## Running everything with Docker Compose (MySQL + backend)

```bash
cp .env.example .env   # then edit .env — at minimum change JWT_SECRET
docker compose up --build
```

This mounts the host's Docker socket into the backend container so it can
launch sibling judge containers — see the comment in `docker-compose.yml`
before using this in anything beyond local dev; that mount is powerful and
should be treated like root access to the host.

## Project structure

```
src/main/java/com/codearena/
├── config/          SecurityConfig, JwtProperties, JudgeProperties, CorsProperties,
│                     WebSocketConfig, OpenApiConfig, JpaAuditingConfig
├── security/        JWT filter, token provider, UserDetails, 401 handler
├── common/           ApiResponse envelope, BaseEntity, GlobalExceptionHandler + custom exceptions
├── user/              User entity/repo/service, Role enum
├── auth/               Register/login/refresh/logout, OTP email verification, password reset
├── problem/             Problem + TestCase entities, listing/detail/create, DTOs
├── submission/           Submission entity, run/submit orchestration
│   └── judge/              DockerJudgeService, LanguageRuntime (per-language image + commands),
│                            OutputComparator, JudgeResult
├── contest/              Contest + ContestParticipant, create/list/detail/register
└── leaderboard/           On-demand standings computation + WebSocket broadcast
```

**Convention going forward:** Assignments, Quizzes, Certificates and
Notifications aren't built yet (matching the frontend's "coming soon"
stubs) — give each its own top-level package following the same shape as
`contest/`: entity, repository, service, controller, `dto/` subpackage.

## How the judge actually works (short version)

1. `POST /api/problems/{slug}/run` or `/submit` hits `SubmissionService`.
2. It calls `DockerJudgeService.execute(language, code, stdin, timeLimitMs, memoryLimitMb)`.
3. That writes the code + input + a generated `run.sh` to a fresh temp
   directory, then shells out to `docker run` with the resource-limiting
   flags from `docs/docker-sandbox-guide.md` (`--memory`, `--cpus`,
   `--pids-limit`, `--network none`, `--read-only`, dropped capabilities),
   redirecting the container's stdout/stderr to files on the host.
4. Exit code 42 (a sentinel `run.sh` uses) means the compile step failed →
   `COMPILATION_ERROR`. Exit code 124 (`timeout`'s own code) →
   `TIME_LIMIT_EXCEEDED`. Any other non-zero → `RUNTIME_ERROR`. Zero →
   the code ran fine, and the verdict comes back as `PENDING` so the
   caller can diff stdout against the expected output.
4. `/submit` loops this over every test case, persists a `Submission`, and
   broadcasts the recomputed leaderboard to `/topic/leaderboard/global` (or
   `/topic/leaderboard/{contestId}`) over WebSocket.

## What's simplified (documented on purpose, not hidden)

- **Memory-limit-exceeded detection**: the judge currently reports
  `RUNTIME_ERROR` for any non-zero, non-timeout exit rather than
  specifically detecting an OOM kill. Real OOM detection needs to inspect
  the container's exit reason (`docker inspect` for an OOMKilled flag, or
  cgroup memory events) — worth adding once you're past the MVP.
  Distinguishing this from other runtime failures reliably takes deeper
  cgroup/OOM inspection.
- **Leaderboard**: computed on-demand from `Submission` rows rather than
  an incrementally-maintained table/Redis sorted set. Fine at portfolio
  scale; the `docs/docker-sandbox-guide.md`-style note in
  `LeaderboardService`'s Javadoc explains the upgrade path.
  Penalty-per-wrong-submission (mentioned in the original spec) isn't
  factored into scoring yet — only distinct-problems-solved and total
  runtime as a tiebreaker.
- **Email**: `EmailService` logs instead of sending. Wire in
  `spring-boot-starter-mail` or a provider SDK (SES, SendGrid) when ready.
- **Assignments / Quizzes / Certificates / Notifications**: not built —
  see "Convention going forward" above.
- **Redis**: not wired in. The spec calls it optional, and nothing here
  depends on it yet — it's the natural next addition for leaderboard
  caching and session storage under real load.

## Testing

```bash
mvn test
```

- `CodeArenaApplicationTests` — full Spring context load (catches most
  wiring mistakes: bad property bindings, missing beans, security config
  errors) using the H2-backed `dev` profile.
- `OutputComparatorTest` — unit tests for the judge's output-comparison
  logic (exact match / presentation error / wrong answer).

This is a thin starting slice, not full coverage — the next tests worth
adding are `AuthServiceTest` (mocking the repositories) and a
`ProblemControllerIT` with `@SpringBootTest` + `MockMvc` against the H2
profile.

## Environment variables (production profile)

| Variable | Purpose | Default |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection | see `application-prod.yml` |
| `JWT_SECRET` | HMAC signing key — **change this**, it ships with an obviously-fake dev default | dev-only placeholder |
| `JWT_ACCESS_EXPIRY_MS`, `JWT_REFRESH_EXPIRY_MS` | Token lifetimes | 15 min / 7 days |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list, must include your deployed frontend URL | `http://localhost:5173` |
| `JUDGE_WORKDIR`, `JUDGE_TIMEOUT_SECONDS`, `JUDGE_MEMORY_MB`, `JUDGE_CPU_LIMIT`, `JUDGE_PIDS_LIMIT` | Judge sandbox limits | see `application.yml` |

## Next steps

1. `mvn clean compile` and fix whatever the compiler flags (see the caveat at the top).
2. Build the judge images and confirm a real submission runs end-to-end.
3. Point the React frontend's `.env` at this backend and replace its mock problem/leaderboard data with real API calls.
4. Add the Assignments/Quizzes/Certificates/Notifications modules.
5. Add Redis for leaderboard caching once contest sizes justify it.
