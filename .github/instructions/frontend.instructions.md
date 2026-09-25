---
applyTo: "frontend/**/*.{js,jsx,ts,tsx,css,html}"
---

# Frontend implementation rules

- React owns presentation and user interaction only.
- Do not place authoritative pricing, inventory, analytics, subscription-state, or order business rules in React.
- Call Spring Boot through a small API layer instead of scattering fetch calls through components.
- Keep state simple for the MVP. Do not introduce Redux or another state library unless the existing implementation clearly requires it.
- Prefer small reusable components.
- Display backend validation errors to the user.
- Never call an AI provider directly from React. Use the backend assistant endpoints only.
- Voice input must use browser speech-to-text and send the resulting text to the same backend conversational endpoint; do not create a separate voice backend flow.
- Use `VITE_API_BASE_URL` for the backend base URL.
- Optimize for working flows, not visual polish.
