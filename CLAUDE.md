# CLAUDE.md

This repository is a course ecommerce platform organized as a monorepo.

For the current handoff state, verification results, remaining work, and grading-risk notes, read `AGENTS.md` first.

## Layout

- `backend/legacy-web`: Spring Boot monolith backend. It contains the full business implementation and the course-required Servlet/JSP/Listener/Filter/JDBC evidence.
- `backend/gateway-service`, `auth-service`, `product-service`, `order-service`, `admin-service`, `common`: Spring Cloud microservice evidence path.
- `frontend/shop-web`: Vue 3 + Vite storefront.
- `frontend/admin-web`: Vue 3 + Vite admin console.
- `docker`: Docker Compose, Dockerfiles, Nginx, MySQL, and Redis configuration.
- `docs`: course materials, report materials, acceptance evidence, and development logs.
- `scripts`: build, acceptance, reset, and submission automation.

## Common Commands

```bash
# Build Java modules from the repository root
mvn -q -DskipTests package

# Build frontend
npm --prefix frontend/shop-web install
npm --prefix frontend/admin-web install
npm --prefix frontend/shop-web run build
npm --prefix frontend/admin-web run build

# Run complete monolith stack
docker compose -f docker/docker-compose.legacy.yml up -d --build
./scripts/acceptance_check.sh

# Run microservice stack
./scripts/build_microservices.sh
docker compose -f docker/docker-compose.yml up -d --build
./scripts/microservices_smoke_test.sh
```

## Main Application

- Frontend: `http://localhost:18081`
- Backend API: `http://localhost:18080/api`
- Traditional status page: `http://localhost:18080/legacy/status`

Default accounts:

- User: `alice / 123456`
- Super admin: `admin / admin123`

## Notes

- User-facing APIs should derive the current user from the Redis-backed token session, not from client-supplied `userId`.
- Admin APIs are role-protected by `ADMIN` / `SUPER_ADMIN`.
- Generated artifacts such as `target`, `dist`, `node_modules`, `submission`, browser profiles, uploads, and logs are not source.
- Current active course evidence lives under `docs/course/acceptance`; `docs/archive` is historical only.
