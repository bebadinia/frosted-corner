---
applyTo: "backend/**/*.java"
---

# Backend implementation rules

- Use one Spring Boot application.
- Keep capabilities separated by package inside `com.frostedcorner`.
- Controllers should be thin.
- Business logic belongs in services.
- MongoDB access belongs in repositories.
- MongoDB Atlas is the project database. Connection strings and credentials must come from environment/configuration and must never be committed.
- Validate all authoritative business data in Spring Boot.
- Do not trust frontend totals, prices, inventory counts, or subscription state.
- Keep AI behavior behind the provider-neutral `AiClient` abstraction. For the MVP, prefer deterministic `DemoAiClient` rules over external provider calls.
- Do not let AI code invent authoritative product IDs, prices, inventory, order totals, discounts, or customer data.
- Prefer simple request/response DTOs over complex mapping frameworks for this MVP.
- Avoid new infrastructure or architectural layers unless required by an acceptance criterion.
- Add unit/service tests for business rules and integration tests only where they materially protect the demo path.
