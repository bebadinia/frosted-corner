---
applyTo: "backend/src/main/java/com/frostedcorner/{assistant,recommendations}/**/*.java"
---

# AI and recommendation implementation rules

- The MVP implementation behind `AiClient` is deterministic `DemoAiClient`, not an external provider.
- `DemoAiClient` may identify known demo intents such as birthday or event requests, chocolate or other flavor preferences, serving count, product recommendations, add-to-cart requests, and a small set of basic Frosted Corner questions.
- The backend must retrieve actual products and other authoritative data from MongoDB Atlas before producing a recommendation or cart action.
- Never allow `DemoAiClient` to invent authoritative prices, inventory quantities, product IDs, order totals, discounts, customer data, or subscription state.
- Business services should depend on `AiClient`, not directly on `DemoAiClient`.
- Recommendation flow is: user message -> `DemoAiClient` intent/preferences -> Spring Boot product query/validation -> authoritative response to React.
- Add-to-cart behavior must reuse existing cart and inventory validation rather than bypassing core ordering logic.
- Personalized recommendations may deterministically use customer preferences, previous orders, current cart, selected event, and selected season.
- Personalized offers use a fixed 10% discount, and Spring Boot remains authoritative for eligibility and discount calculation.
- No external AI credentials are required for the MVP.
- Do not add vector databases, embeddings infrastructure, RAG platforms, ML recommendation models, or separate voice backends for the MVP.
