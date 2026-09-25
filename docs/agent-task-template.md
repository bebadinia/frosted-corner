# Copilot Agent Task Template

Copy this into a GitHub issue or Copilot Agent chat.

## Context

Frosted Corner is a 4-day MVP using React, one Spring Boot modular monolith, MongoDB Atlas, and a deterministic `DemoAiClient` behind a provider-neutral `AiClient` called from Spring Boot.

Read:
- `.github/copilot-instructions.md`
- relevant file under `.github/instructions/`
- `docs/architecture.md`
- `docs/api-contracts.md`

## Exact task

[One bounded implementation task.]

## Scope

Allowed areas:
- `[paths the agent may change]`

Do not modify:
- `[unrelated areas]`

## Constraints

- Keep the modular-monolith architecture.
- Do not add infrastructure or dependencies unless required.
- Preserve existing API contracts unless this task explicitly changes them.
- Backend remains authoritative for business data.
- Do not commit secrets.

## Implementation clarifications

Follow these rules to avoid ambiguity and scope expansion:

- Authentication, `X-User-Id` authorization, and customer ownership validation are deferred because the user/customer authorization module does not yet exist. Do not create a new authentication or user module as part of this task.
- Validate that `customerId` and `storeId` are present and nonblank.
- Do not add customer-profile or store-management endpoints.
- Products must exist and must have `active = true` to be ordered.
- The order request DTO must contain only `customerId`, `storeId`, and item `productId`/`quantity` fields. Do not accept authoritative prices, line totals, or order totals.
- If the client includes price or total fields, they must never affect backend calculations.
- Reject an empty item list.
- Reject null, zero, or negative quantities.
- Reject duplicate product IDs in the same request with HTTP 400. This prevents duplicate lines from bypassing inventory validation.
- Return HTTP 404 for an unknown or inactive product.
- Return HTTP 404 when inventory does not exist for the requested store and product.
- Return HTTP 409 when inventory exists but the available quantity is insufficient.
- Use `BigDecimal` for unit prices, line totals, and the final total. Do not use `double` or `float` for monetary calculations.
- Persist successful orders with status `CONFIRMED` and an `Instant` UTC creation timestamp.
- Preserve product name and unit price in each persisted order item so historical order totals remain stable when catalog data changes.
- Validate every product, quantity, and inventory record before performing any persistence or inventory decrement.
- A validation failure must not save an order or modify any inventory.
- Add the inventory decrement behavior to the existing inventory service rather than placing inventory business logic in the controller.
- The order service must coordinate catalog lookup, inventory validation, backend pricing, order persistence, and inventory decrement.
- Do not add reservation, locking, messaging, or distributed transaction infrastructure.
- Do not add an undocumented order retrieval endpoint solely for manual verification.
- Verify persistence through the returned order ID, MongoDB inspection, and repository/service tests.
- Use the configured MongoDB Atlas instance. Do not commit `.env` files, connection strings, credentials, or other secrets.
- Run the complete backend test suite, not only the new order tests.

## Acceptance criteria

- [ ] [observable behavior 1]
- [ ] [observable behavior 2]
- [ ] [validation/error behavior]
- [ ] relevant tests added/updated
- [ ] existing relevant tests still pass

## Required validation

Run:
- `[build/test command]`

Manual check:
1. [step]
2. [step]
3. [expected result]

## Completion response

When finished, report:
1. files changed
2. behavior implemented
3. tests/commands run and result
4. any remaining blocker or assumption
