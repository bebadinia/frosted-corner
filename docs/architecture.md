# Architecture

## Local topology

React `localhost:5173`
-> REST/HTTP
-> Spring Boot `localhost:8080`
-> MongoDB Atlas

Spring Boot handles assistant and recommendation behavior through a provider-neutral `AiClient`. For the MVP, the concrete implementation is deterministic `DemoAiClient` and does not call an external AI provider.

MongoDB connectivity is configured with `MONGODB_URI` and `MONGODB_DATABASE`. The database is not expected to run on each developer machine.

## Ownership

### React

Owns:
- presentation
- user interaction
- cart UI state
- form state
- displaying backend responses

Does not own authoritative business rules.

### Spring Boot

Owns:
- validation
- pricing
- orders
- inventory
- recommendations
- subscriptions
- analytics
- AI orchestration

### MongoDB

Owns persistent application data.

Expected collections:
- users
- customers
- franchiseLocations
- products
- events
- eventMenus
- orders
- inventory
- supplyRequests
- payments
- subscriptions

Only add collections for a concrete MVP need. `SubscriptionPlan` is not a collection: subscription plans are fixed server-side MVP definitions.

### AI orchestration

Owns:
- deterministic interpretation of supported natural-language requests
- deterministic recommendation/explanation wording for known demo scenarios

Does not own application facts or state.

MVP notes:
- Keep the provider-neutral `AiClient` abstraction so a real provider can be added after the MVP.
- Support known demo intents such as events, flavor preferences, serving count, product recommendations, add-to-cart requests, and basic Frosted Corner questions.
- Voice input uses browser speech-to-text and the same `/api/assistant/chat` backend endpoint.
- Personalized recommendations may deterministically use customer preferences, previous orders, current cart, selected event, and selected season.
- Personalized offers use a fixed 10% discount calculated authoritatively in Spring Boot.

## Backend package structure

Suggested:

com.frostedcorner
- catalog/
- customers/
- orders/
- inventory/
- supplies/
- events/
- subscriptions/
- analytics/
- recommendations/
- ai/
- assistant/

These are internal modules/packages inside one application, not services.

## Critical integration

Order placement must connect these capabilities:

checkout
-> backend validates products and inventory
-> backend calculates total
-> order persists
-> inventory decreases
-> analytics reads the new order

This is the most important technical path in the MVP.
