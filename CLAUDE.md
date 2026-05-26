# CLAUDE.md

This repository is a course ecommerce platform organized as a monorepo.

## Layout

- `apps/api`: Spring Boot monolith backend. It contains the full business implementation and the course-required legacy Servlet/JSP/Listener/Filter/JDBC module.
- `apps/web`: Vue 3 + Vite frontend for storefront and admin console.
- `services/*`: course microservice projects for gateway, auth, catalog, order, and admin.
- `libs/common`: shared DTO/session model examples.
- `docs`: course materials, report materials, acceptance evidence, and development logs.
- `scripts`: build, acceptance, and submission automation.

## Common Commands

```bash
# Build Java modules from the repository root
mvn -q -DskipTests package

# Build frontend
npm --prefix apps/web install
npm --prefix apps/web run build

# Run complete monolith stack
docker compose up -d --build
./scripts/acceptance_check.sh

# Run microservice stack
./scripts/build_microservices.sh
docker compose -f docker-compose.microservices.yml up -d --build
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
