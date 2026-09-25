# Frosted Corner Copilot Instructions

You are an implementation assistant for a 4-day MVP. Humans define architecture, API contracts, data models, acceptance criteria, and business rules. Do not redesign the system unless explicitly asked.

## Goal

Build a working local end-to-end application demonstrating all 8 capabilities:

1. Simplified customer ordering
2. Conversational ordering
3. Event-based menus
4. AI-recommended offers
5. Franchise inventory and supply management
6. Order and sales insights
7. Personalized customer experience
8. Subscription dessert plans

Reliability of the demo is more important than production-grade sophistication.

## Required architecture

- React owns presentation and user interaction.
- Spring Boot owns business logic, validation, pricing, orders, inventory, recommendations, subscriptions, analytics, and AI orchestration.
- MongoDB Atlas owns persistent application data.
- For the MVP, AI behavior is deterministic behind the provider-neutral `AiClient` abstraction.
- Spring Boot must call `AiClient`; React must never call an AI provider or hard-code recommendation logic.
- Use one Spring Boot modular monolith, not microservices.

Expected packages:

`com.frostedcorner.catalog`
`com.frostedcorner.customers`
`com.frostedcorner.orders`
`com.frostedcorner.inventory`
`com.frostedcorner.supplies`
`com.frostedcorner.events`
`com.frostedcorner.subscriptions`
`com.frostedcorner.analytics`
`com.frostedcorner.recommendations`
`com.frostedcorner.ai`
`com.frostedcorner.assistant`

Use normal layering where appropriate:

Controller -> Service -> Repository -> MongoDB

Controllers must not contain significant business logic.

## Source-of-truth rules

`DemoAiClient` is never the source of truth for:

- prices
- inventory
- order totals
- customer records
- subscription state
- product existence
- business rules
- discounts

The backend must retrieve authoritative application data before returning recommendations or creating orders.

## MVP constraints

Do not add unless explicitly required:

- microservices
- Kubernetes
- Docker orchestration
- Kafka
- message queues
- API gateway
- cloud deployment
- multiple databases
- real payment processing
- recurring billing infrastructure
- external supplier integration
- a data warehouse
- a machine-learning recommendation model
- complex authentication

Prefer the simplest implementation that demonstrates the capability end-to-end.

## Critical dependency chain

Prioritize work in this order:

Products -> Cart -> Order -> Inventory -> Analytics

AI, events, personalization, supplies, and subscriptions should integrate around that stable core.

## Order rules

When an order is submitted:

1. Accept product IDs and quantities from the client.
2. Load products from MongoDB.
3. Use backend product prices.
4. Validate quantities.
5. Validate available inventory.
6. Calculate totals in Spring Boot.
7. Persist the order.
8. Decrease inventory for ordered products.
9. Return the authoritative order result.

Never trust totals sent by React.

## AI rules

For the MVP, the `AiClient` implementation is `DemoAiClient` with deterministic intent and recommendation rules. Business logic must depend on `AiClient`, not on provider-specific classes.

For conversational ordering:

1. Receive natural-language text in Spring Boot.
2. Use `DemoAiClient` only to interpret intent/preferences and produce deterministic assistant wording for known demo scenarios.
3. Support a small set of known intents such as birthday or event requests, chocolate or other flavor preferences, serving count, product recommendations, add-to-cart requests, and a small set of basic Frosted Corner questions.
4. Retrieve real products from MongoDB Atlas.
5. Build recommendations from real products.
6. Reuse existing cart and inventory validation for add-to-cart behavior.
7. Personalized recommendations may deterministically use customer preferences, previous orders, current cart, selected event, and selected season.
8. Personalized offers use a fixed 10% discount. Spring Boot determines eligibility and calculates the authoritative discount.
9. Do not create a general coupon or promotion engine for the MVP.
10. No external AI credentials are required for the MVP, but keep `AiClient` so a real provider can be added after the MVP.
11. Return product IDs and authoritative product data to React.

Voice ordering is browser speech-to-text feeding the same `/api/assistant/chat` endpoint. Do not build a separate voice architecture.

## Coding-agent behavior

For every implementation task:

- Work only within the requested scope.
- Read relevant files before editing.
- Do not modify unrelated modules.
- Do not introduce new dependencies without a concrete need.
- Reuse existing patterns.
- Preserve existing API contracts unless the task explicitly changes them.
- Add or update tests for changed business behavior.
- State files changed and validation performed.
- If requirements conflict, stop and identify the conflict instead of inventing a new architecture.

## Definition of done for a task

A task is not complete merely because code compiles.

It should satisfy its acceptance criteria, preserve the architecture above, and include appropriate tests or a documented manual verification step.

## Demo-first principle

Keep `main` runnable.

The final demo should support:

personalized products
-> event menu
-> conversational request
-> recommendation/offer
-> cart and checkout
-> persisted order
-> inventory decrease
-> updated sales analytics
-> subscription creation and active state
