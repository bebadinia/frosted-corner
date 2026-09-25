# Frosted Corner MVP

4-day hackathon MVP for a dessert/franchise ordering platform.

## Architecture

- Frontend: React + Vite
- Backend: Java + Spring Boot
- Database: MongoDB Atlas
- AI: deterministic `DemoAiClient` behind a provider-neutral `AiClient`, called only from Spring Boot
- Architecture style: modular monolith
- Local ports:
  - React: http://localhost:5173
  - Spring Boot: http://localhost:8080

Backend MongoDB configuration is supplied through environment variables:

- `MONGODB_URI`: Atlas connection string, for example `mongodb+srv://<user>:<password>@<cluster>/<options>`
- `MONGODB_DATABASE`: target Atlas database name

No external AI credentials are required for the MVP.

## Team Ownership

- Developer 1: React/customer UI/franchise UI/dashboard/API integration
- Developer 2: Spring Boot/core backend/MongoDB/products/orders/inventory/events/supplies/subscriptions
- Developer 3: deterministic AI/conversational ordering/recommendations/personalization/integration testing

Ownership is not exclusive. Help unblock each other when necessary.

## Most Important Dependency Chain

Products -> Cart -> Order -> Inventory -> Analytics

Get this end-to-end flow stable before polishing or adding complexity.

## First-time setup

Read these files in order:

1. `docs/SETUP.md`
2. `docs/collaboration-workflow.md`
3. `docs/architecture.md`
4. `docs/api-contracts.md`
5. `.github/copilot-instructions.md`

## Branching

Do not create a long-lived `develop` branch.

Create short-lived task branches from `main`:

- `feature/frontend-menu`
- `feature/backend-orders`
- `feature/ai-recommendations`
- `fix/order-inventory-validation`

Open a pull request back to `main`. Keep `main` runnable.

## AI implementation agents

Repository-wide Copilot rules:
- `.github/copilot-instructions.md`

Path-specific implementation rules:
- `.github/instructions/`

Suggested custom agent definitions:
- `.github/agents/`

Reusable task format:
- `docs/agent-task-template.md`

If your installed Copilot version does not expose repository custom agents from `.github/agents`, use the same files as prompt templates in Copilot Agent mode. The global and path-specific instruction files still remain the source of truth for the project.
