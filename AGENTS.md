# Agent Handoff

This repository is a Web Development course ecommerce project. The current goal is not to add more features, but to reduce grading risk by keeping implementation, documentation, evidence, and submission packaging aligned with `docs/course/A0801051040-Web开发技术-实验指导书-2026.md`.

## Current Verified State

- Current Vue frontends plus Gateway/microservices are the primary page-acceptance path.
- Legacy monolith is the traditional Web evidence and fallback path, not the only demo entry.
- Email verification is a real SMTP flow through Docker MailHog; there is no fixed or returned demo code.
- `docs/course/acceptance/checklist.md` is the current point-by-point coverage matrix for the guidebook body and scoring table.
- Submission packaging was test-run successfully and produced a source-only zip around `4.4M`, well below the `50M` limit.
- Do not reuse any `20236918...` filename or archive as the user's final submission package; those names came from borrowed reference material and are not this user's provided identity.
- The package script excludes `node_modules`, `target`, `dist`, `uploads`, `logs`, `.git`, `submission`, and `docs/archive`.

Recent verified commands:

```bash
mvn test
npm run build
./scripts/acceptance_check.sh
python3 scripts/acceptance_api_smoke.py
scripts/microservices_smoke_test.sh
ACCEPTANCE_BASE_URL=http://127.0.0.1:18090/api ACCEPTANCE_MAILHOG_URL=http://127.0.0.1:18199 python3 scripts/acceptance_api_smoke.py
./scripts/create_submission_zip.sh
```

Observed passing results:

- `mvn test`: 71 tests, 0 failures, 0 errors.
- Frontend build passes, with only Vite chunk-size warnings.
- Monolith quick smoke passes.
- Monolith full API smoke passes.
- Microservice smoke passes, including Nacos registration, Gateway auth, Feign evidence, and stock deduction.
- Microservice gateway full API smoke passes.

## Do Not Spend Time On

- Do not prioritize final `.docx` export unless the user explicitly asks. They said this is not necessary for now.
- Do not prioritize final submission filename/personal-info handling unless the user explicitly asks. The user has not provided personal information and said it is not necessary for now.
- Do not add new major technologies, split databases, add message queues, implement real payment, or do a large refactor before submission.

## Remaining Worthwhile Work

1. Browser/manual visual巡检 for the current Vue frontend path first:
   - `http://localhost:5173`
   - `http://localhost:5174`
   - or Docker/Nginx microservice frontend entry when using full-stack containers
   - Home, product list/detail, cart, checkout, pay, orders.
   - Admin login, dashboard, products, orders, users.
   - Mobile widths around 390px and 768px, especially the admin drawer menu.

2. Legacy evidence spot check:
   - `http://localhost:18081`
   - `/legacy/status`
   - Confirm the single-stack path still works as traditional Java Web evidence and fallback demo.

3. Screenshot freshness check:
   - Evidence lives in `docs/course/acceptance/evidence`.
   - Confirm the key images still match the current UI and current documentation.
   - Highest-value screenshots: home, product detail, cart, checkout/pay/orders, admin dashboard/products/orders/users, responsive, Docker, Nacos, smoke pass, Feign logs.

4. Git/document migration cleanup:
   - `git status` shows many old doc paths deleted and new doc paths untracked.
   - Treat this as a docs reorganization, not accidental loss.
   - Active docs are under `docs/course`, root `docs/*.md`, and `README.md`.
   - `docs/archive` is historical and intentionally excluded from the submission zip.

5. Demo database reset before final live demonstration:
   - Smoke tests create QA users/orders/products/reviews/uploads.
   - For screenshots or classroom demo, reset to clean seed data first.
   - Do not reset if the user wants to inspect test-created data.

## Important Files

- Guidebook: `docs/course/A0801051040-Web开发技术-实验指导书-2026.md`
- Main report source: `docs/course/实验报告.md`
- Point-by-point acceptance matrix: `docs/course/acceptance/checklist.md`
- Screenshot/evidence directory: `docs/course/acceptance/evidence`
- API reference: `docs/api-reference.md`
- Architecture notes: `docs/architecture.md`
- Development commands: `docs/development.md`
- Deployment notes: `docs/deployment.md`
- Submission script: `scripts/create_submission_zip.sh`
- Full API smoke: `scripts/acceptance_api_smoke.py`
- Microservice smoke: `scripts/microservices_smoke_test.sh`

## Cautions

- The working tree may contain unrelated local files such as `apps/api/uploads`, an external large source directory, and IDE/settings files. Do not delete or revert user-created material unless explicitly asked.
- `20236918...` files/directories at the repository root are borrowed reference material from another student's microservice project. They are not authoritative for this project and should not be used as final submission identity.
- Archive docs can contain old claims like demo SMTP or earlier microservice notes. They are not active evidence.
- If running Docker, local service access often needs permission/escalation in sandboxed environments.
