# 3-Developer Collaboration Workflow

## Rule 1: `main` is always demo-able

Do not use `main` as a personal work branch.

## Rule 2: no long-lived `develop` branch

With only three developers and four implementation days, a second integration branch adds more merge overhead than value.

## Rule 3: one bounded task per branch

Start each task from the newest `main`.

Examples:

- `feature/frontend-menu`
- `feature/backend-order-create`
- `feature/backend-inventory-decrement`
- `feature/ai-intent-parser`
- `fix/analytics-order-count`

## Daily branch workflow in GitHub Desktop

1. Switch to `main`.
2. **Fetch origin**.
3. Pull if GitHub Desktop indicates remote changes.
4. Create a new task branch from `main`.
5. Make a small, bounded change.
6. Commit with a meaningful message.
7. Push/publish the branch.
8. Open a pull request.
9. Ask one teammate to review.
10. Fix review items on the same branch.
11. Merge into `main`.
12. Delete the merged branch.
13. Everyone fetches/pulls `main` before starting the next task.

## Commit style

Keep it simple:

- `feat: add product catalog endpoint`
- `feat: implement checkout UI`
- `fix: prevent order when inventory is insufficient`
- `test: cover inventory decrement`
- `docs: define order API contract`
- `chore: configure local environment`

## Avoid merge conflicts

Developers should avoid editing the same central files simultaneously.

Before starting a task, announce it in team chat.

High-conflict files include:

- root `README.md`
- shared backend configuration
- shared route configuration
- central frontend app/router files
- API contract documents
- seed-data files

Coordinate before editing those.

## Pull request rule

The reviewer checks:
- does it satisfy the issue/task?
- does it preserve API contracts?
- did business logic accidentally move to React?
- does AI invent authoritative data?
- could it break order -> inventory -> analytics?
- are secrets absent?
- does the relevant test/manual verification pass?

For a four-day MVP, keep reviews fast and focused.
