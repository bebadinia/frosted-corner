# First Agent Tasks

Do these in sequence. Do not start all feature work before the shared baselines are merged.

## Task 0 — Repository collaboration check

Owner: all three developers, one tiny branch each.

Acceptance:
- branch created from main
- commit pushed
- PR opened
- another developer reviews
- PR merged/deleted

## Task 1 — Frontend baseline

Owner: Developer 1  
Branch: `feature/frontend-baseline`

Task:
Scaffold React with Vite under `frontend/`. Add a minimal app shell and API base configuration. Do not implement business features yet.

Acceptance:
- `npm install` succeeds
- `npm run dev` starts on the expected Vite port
- app renders a Frosted Corner heading
- `VITE_API_BASE_URL` is read from environment configuration
- no backend business rules are duplicated

## Task 2 — Backend baseline

Owner: Developer 2  
Branch: `feature/backend-baseline`

Task:
Scaffold one Spring Boot application under `backend/` with Spring Web, Spring Data MongoDB, Bean Validation, and tests. Use base package `com.frostedcorner`.

Acceptance:
- backend builds
- application starts on port 8080
- MongoDB URI comes from configuration/environment
- a simple health/test endpoint can be reached locally
- package structure is ready for internal modules
- no microservices or extra infrastructure

## Merge gate

Merge Tasks 1 and 2 to `main`. Everyone pulls the new `main`.

Only after this gate should capability implementation fan out.

## Task 3 — Product catalog vertical slice

Primary owner: Developer 2  
Support: Developer 1

Backend:
- Product model/repository/service/controller
- seed realistic active products
- `GET /api/products`
- return `id`, `name`, `description`, `price`, `category`, `imageFileName`, and `active`
- do not add event filtering to `GET /api/products`; event menus use `Event` and `EventMenu` in a later task

Frontend:
- fetch products through a small API module
- render menu cards using backend product data

Acceptance:
- React loads real products from Spring Boot
- Spring Boot loads active products from MongoDB
- no hard-coded product list in React
- no event tags or event relationships are stored directly on `Product`

## Task 4 — Users, customers, and franchise locations

Owner: Developer 2  
Support: Developer 1

Implement:
- `User`, `Customer`, and `FranchiseLocation` models, repositories, and services
- seed demo users for `CUSTOMER`, `EMPLOYEE`, `MANAGER`, and `OWNER` roles
- link customer users through `customerId`
- link employee and manager users through `storeId`
- `GET /api/customers/{customerId}`
- `PUT /api/customers/{customerId}`
- support the `X-User-Id` request header defined in `docs/api-contracts.md`
- enforce customer ownership and store-scope checks in the service layer

Acceptance:
- a valid `X-User-Id` resolves a persisted user
- a customer can view and update only their linked customer profile
- an employee or manager cannot access another store's inventory or supply requests
- an owner can access all-store analytics
- a request with an unknown user ID returns a clear unauthorized or not-found responses

## Task 5 — Order + inventory core

Primary owner: Developer 2  
Support: Developer 3 for integration tests

Implement:
- inventory records
- `POST /api/orders`
- backend price lookup
- quantity/inventory validation
- backend total calculation
- order persistence
- inventory decrement

Acceptance:
- valid order persists
- inventory decreases exactly once
- insufficient inventory rejects the order
- React never supplies authoritative totals

## Task 6 — Cart + checkout UI

Owner: Developer 1

Implement:
- add/remove/update cart
- checkout request
- order confirmation
- error display

Acceptance:
- customer can complete an order using the real backend
- backend returned total is displayed
- validation errors are visible

## Task 7 — Analytics

Owner: Developer 2 or Developer 3

Implement `GET /api/analytics/summary` from persisted orders.

Requirements:
- accept an optional `storeId` query parameter
- when `storeId` is supplied, calculate results only from orders for that store
- a `MANAGER` may access analytics only for their assigned `storeId`
- an `OWNER` may request analytics without `storeId` to receive all-store results
- enforce the acting-user and store-scope rules defined in `docs/api-contracts.md`

Acceptance:
- total orders
- revenue
- average order value
- top-selling products
- a newly placed order changes the result
- a manager-scoped request excludes orders from other stores
- an owner-wide request includes orders from all stores

## Task 8 — Conversational recommendation baseline

Owner: Developer 3

Implement:
- provider-neutral `AiClient` plus deterministic `DemoAiClient`
- deterministic intent extraction for known demo scenarios
- real MongoDB Atlas product retrieval
- recommendation response
- guided deterministic response for unsupported or ambiguous requests

Acceptance:
- React never calls an AI provider directly
- `DemoAiClient` cannot invent authoritative prices, discounts, or products
- recommended product IDs exist in MongoDB Atlas
- a sample party request returns usable recommendations
- add-to-cart suggestions reuse existing cart and inventory validation
## Task 9 — Event menus

Owner: Developer 2  
Support: Developer 1

Implement:
- `Event` and `EventMenu` models, repositories, services, and controllers
- seed active events and their product assignments
- `GET /api/events`
- `GET /api/events/{eventId}/products`
- query event products through `EventMenu` using `eventId` and `productId`
- return only active products for an event menu

Acceptance:
- active events are returned from MongoDB
- an event returns only its assigned active products
- an unknown event returns a clear not-found response
- products are related to events through `EventMenu`, not fields stored on `Product`

## Task 10 — Supply requests

Owner: Developer 2

Implement:
- `SupplyRequest` model, repository, service, and controller
- `POST /api/supply-requests`
- validate that the requested `productId` exists
- enforce that a `MANAGER` can create a request only for their assigned `storeId`
- persist `status` as `ORDERED` and set `createdAt` in Spring Boot

Acceptance:
- a manager can create a supply request for their store
- a manager cannot create a request for another store
- the request persists with `ORDERED` status and a server-generated timestamp
- no external supplier integration is added

## Task 11 — Subscription dessert plans

Owner: Developer 2  
Support: Developer 1

Implement:
- fixed server-side MVP subscription plan definitions; do not create a `SubscriptionPlan` MongoDB collection
- `Subscription` model, repository, service, and controller
- `GET /api/subscription-plans`
- `POST /api/subscriptions`
- `GET /api/customers/{customerId}/subscription`
- enforce that a customer manages only their own subscription
- set `status` to `ACTIVE`, `startedAt` in Spring Boot, and calculate `nextDeliveryDate`

Acceptance:
- available plans are returned by the backend
- a customer can create an active subscription for an available plan
- the active subscription persists in MongoDB
- a customer cannot view or manage another customer's subscription
- subscription state is always controlled by Spring Boot

## Task 12 — Demo payment records

Owner: Developer 2

Implement:
- `Payment` model, repository, service, and controller
- `POST /api/payments`
- load the persisted order using `orderId`
- set `Payment.amount` from the authoritative `Order.total`
- persist `paymentDate` in Spring Boot
- support only demo payment records; do not integrate a payment provider

Acceptance:
- a payment record can be created for an existing order
- the persisted payment amount exactly matches the persisted order total
- a frontend-supplied payment amount is not accepted
- an unknown order produces a clear not-found response

## Task 13 — Customer personalization

Owner: Developer 3  
Support: Developer 1

Implement:
- `GET /api/customers/{customerId}/recommendations`
- `GET /api/customers/{customerId}/orders`
- derive recommendations from persisted customer preferences, favorite categories, and prior orders
- return only real active products from MongoDB
- enforce that a customer can access only their own personalization and order-history data

Acceptance:
- a customer receives recommendations referencing real active products
- recommendations use persisted customer or order data when available
- a customer can retrieve their prior orders for an “Order Again” experience
- a customer cannot retrieve another customer's personalization or order history
- no separate recommendation platform or machine-learning model is added

Continue with events, personalization, supplies, and subscriptions only after the critical ordering path is stable.