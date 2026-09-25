# AI Architecture — MVP

## Decision

Use a provider-neutral `AiClient` inside Spring Boot. For the MVP, the implementation is `DemoAiClient` with deterministic rules. Do not build a multi-provider framework.

## Flow

React -> `POST /api/assistant/chat` -> `AssistantService` -> `AiClient` -> `DemoAiClient` -> deterministic intent/recommendation rules.

The interpreted intent returns to Spring Boot. Spring Boot then retrieves authoritative product/customer data from MongoDB Atlas, validates inventory and business rules, and builds the recommendation or add-to-cart response.

## Source-of-truth rule

`DemoAiClient` may interpret event, serving count, preferences, natural-language intent, and a small set of Frosted Corner questions. It must not be authoritative for product existence, product IDs, prices, inventory, order totals, discounts, customer records, subscriptions, or business rules.

## Suggested package

`com.frostedcorner.ai`: `AiClient.java`, `DemoAiClient.java`

`com.frostedcorner.assistant`: controller, service, DTOs, `CustomerIntent.java`

`com.frostedcorner.recommendations`: `RecommendationService.java`

## Failure behavior

Because the MVP implementation is deterministic, unsupported or ambiguous requests should return a safe guided response based on application data instead of failing the full customer experience.

## MVP intent scope

Support a small set of known demo intents:
- birthday or event requests
- chocolate or other flavor preferences
- serving count
- product recommendations
- add-to-cart requests
- basic Frosted Corner questions

Personalized recommendations may deterministically use customer preferences, previous orders, current cart, selected event, and selected season.

Personalized offers use a fixed 10% discount. Spring Boot determines eligibility and computes the authoritative discount.

Voice input uses browser speech-to-text and the same `/api/assistant/chat` endpoint. Do not create a separate voice backend.
