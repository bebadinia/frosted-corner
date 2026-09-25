---
applyTo: "**/*{Test,Tests,test,spec}*"
---

# Test rules

Prioritize tests around the critical demo path:

- product retrieval
- order total calculation
- invalid quantities
- insufficient inventory
- order persistence
- inventory decrement
- analytics reflecting orders
- subscription persistence
- AI-response validation/fallback

Do not create a large test framework. Use the project's existing Spring/JUnit and frontend test tooling.
