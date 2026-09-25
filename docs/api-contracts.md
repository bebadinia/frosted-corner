# API Contracts — MVP Baseline

This file is the team's contract. Update it intentionally when the team agrees to an API change.

The exact fields may evolve during implementation, but backend ownership rules must remain unchanged.

## Request Access and Store Scope

`GET /api/products` is public for the MVP and does not require the `X-User-Id` header.

For the MVP, protected endpoints use the Spring Security HTTP session created by a successful login.
Spring Boot loads the `User` from MongoDB and enforces access rules in the service layer. React must not send a role or be trusted to choose permissions.

Rules:
- A `CUSTOMER` may create orders, update only their linked customer profile, and manage only their own subscription.
- An `EMPLOYEE` may view and update inventory only for their assigned `storeId`.
- A `MANAGER` may create supply requests and view analytics only for their assigned `storeId`.
- An `OWNER` may view analytics for all stores.
- For employee and manager actions, Spring Boot must reject a request when the submitted `storeId` does not match the acting user's assigned `storeId`.
- Spring Boot must reject a request when a customer ID in the URL or request body does not match the acting user's linked `customerId`, unless the acting user has an authorized staff role.

## Authentication

### POST `/api/auth/register`

Creates a `CUSTOMER` user and its linked customer profile. The password is stored only as a BCrypt hash.

Request:

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "password": "password-at-least-8-characters"
}
```

### POST `/api/auth/login`

Creates an authenticated HTTP session for an existing user.

### POST `/api/auth/logout`

Ends the current HTTP session.

### GET `/api/auth/me`

Returns the currently authenticated user, including the linked `customerId` for a customer account.


## Products
`GET /api/products` must return only products where `active` is `true`.
### GET `/api/products`

Returns active products from MongoDB.

Example response:

```json
[
  {
    "id": "p1",
    "name": "Chocolate Cupcake",
    "description": "Chocolate cupcake with frosting",
    "price": 4.5,
    "category": "Cupcakes",
    "imageFileName": "chocolate-cupcake.jpg",
    "active": true
  }
]
```

## Events

### GET `/api/events`

Returns active events.

Example response:

```json
[
  {
    "id": "e1",
    "name": "Birthday Party Special",
    "startDate": "2026-09-22",
    "endDate": "2026-09-30",
    "active": true
  }
]
```

### GET `/api/events/{eventId}/products`

Returns products assigned to the event through the `EventMenu` relationship.

Example response:

```json
[
  {
    "id": "p1",
    "name": "Chocolate Cupcake",
    "description": "Chocolate cupcake with frosting",
    "price": 4.5,
    "category": "Cupcakes",
    "imageFileName": "chocolate-cupcake.jpg",
    "active": true
  }
]
```

## Customers

### GET `/api/customers/{customerId}`

Returns a customer profile.

Example response:

```json
{
  "id": "c1",
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "phone": "555-0123",
  "street": "123 Main St",
  "city": "Portland",
  "state": "OR",
  "zipCode": "97201",
  "preferences": ["chocolate", "vegan"],
  "favoriteCategories": ["Cupcakes", "Brownies"]
}
```

### GET `/api/customers/{customerId}/orders`

Returns the authenticated customer's persisted orders plus the three most-ordered products calculated from that order history.

Example response:

```json
{
  "orders": [
    {
      "id": "demo-order-003",
      "customerId": "c1",
      "storeId": "store1",
      "status": "CONFIRMED",
      "total": 22.45,
      "items": [
        {
          "productId": "P005",
          "productName": "Chocolate Fudge Cupcake",
          "quantity": 3,
          "lineTotal": 13.47
        }
      ]
    }
  ],
  "favoriteItems": [
    {
      "productId": "P005",
      "productName": "Chocolate Fudge Cupcake",
      "quantityOrdered": 9
    }
  ]
}
```

Favorite-item totals are calculated in Spring Boot from persisted orders. React only displays the result.

### PUT `/api/customers/{customerId}`

Updates a customer's profile and preferences.

Request:

```json
{
  "name": "Alice Johnson",
  "phone": "555-0123",
  "street": "123 Main St",
  "city": "Portland",
  "state": "OR",
  "zipCode": "97201",
  "preferences": ["chocolate", "vegan"],
  "favoriteCategories": ["Cupcakes", "Brownies"]
}
```

## Orders

### GET `/api/locations`

Returns the seeded franchise locations for selectable takeout pickup.

Example response:

```json
[
  {
    "id": "store1",
    "storeName": "Frosted Corner - New York",
    "street": "101 Broadway",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "managerName": "Morgan Lee",
    "latitude": 40.7128,
    "longitude": -74.006
  }
]
```

### POST `/api/orders`

Creates an order for a customer with delivery or takeout fulfillment.

Request:

```json
{
  "customerId": "c1",
  "fulfillmentOption": "DELIVERY",
  "customer": {
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "phone": "555-0123",
    "street": "2490 Burnside Street",
    "city": "Portland",
    "state": "OR",
    "zipCode": "97205"
  },
  "items": [
    {
      "productId": "p1",
      "quantity": 2
    }
  ]
}
```

Do not send an authoritative total from React.

Backend:
- loads products
- validates quantities
- determines the authoritative store based on fulfillment rules
- validates store inventory
- uses backend prices
- calculates the fulfillment fee and final total
- persists the order
- decreases inventory

Rules:
- `fulfillmentOption=DELIVERY` requires `name`, `email`, `phone`, `street`, `city`, `state`, and `zipCode`
- `fulfillmentOption=TAKEOUT` requires `name`, `email`, `phone`, and a selected `storeId`
- Spring Boot resolves delivery to `LOCAL_DELIVERY` when the nearest store is within 25 miles, otherwise `SHIPPING`
- Spring Boot assigns the authoritative fee: `LOCAL_DELIVERY=2.99`, `SHIPPING=4.99`, `TAKEOUT=0.00`
- For `LOCAL_DELIVERY`, Spring Boot simulates a provider as `DoorDash` or `Uber Eats`

Example response:

```json
{
  "id": "o100",
  "customerId": "c1",
  "storeId": "store24",
  "fulfillmentType": "LOCAL_DELIVERY",
  "fulfillmentFee": 2.99,
  "fulfillmentProvider": "DoorDash",
  "customer": {
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "phone": "555-0123"
  },
  "deliveryAddress": {
    "street": "2490 Burnside Street",
    "city": "Portland",
    "state": "OR",
    "zipCode": "97205"
  },
  "status": "CONFIRMED",
  "items": [
    {
      "productId": "p1",
      "productName": "Chocolate Cupcake",
      "unitPrice": 4.5,
      "quantity": 2,
      "lineTotal": 9.0
    }
  ],
  "total": 11.99,
  "createdAt": "2026-09-22T10:30:00Z"
}
```

## Inventory

### GET `/api/inventory`

Returns inventory for a franchise store.

Query parameters:
- `storeId` (required)

Example response:

```json
[
  {
    "id": "inv1",
    "storeId": "store1",
    "productId": "p1",
    "quantity": 50,
    "lowStockThreshold": 10
  }
]
```

### PUT `/api/inventory/{productId}`

Adjusts the quantity of a product's inventory for a store.

Query parameters:
- `storeId` (required)

Request:

```json
{
  "quantity": 25
}
```

Example response:

```json
{
  "id": "inv1",
  "storeId": "store1",
  "productId": "p1",
  "quantity": 25,
  "lowStockThreshold": 10
}
```

## Supply Requests

### POST `/api/supply-requests`

Creates a supply request for a store. No external supplier integration is required.

Request:

```json
{
  "storeId": "store1",
  "productId": "p1",
  "quantityRequested": 100
}
```

Example response:

```json
{
  "id": "sr1",
  "storeId": "store1",
  "productId": "p1",
  "quantityRequested": 100,
  "status": "ORDERED",
  "createdAt": "2026-09-22T10:30:00Z"
}
```

## Payments

### POST `/api/payments`

Creates a demo payment record for an existing order. Do not integrate a real payment provider.

Request:

```json
{
  "orderId": "o100",
  "paymentMethod": "CREDIT_CARD"
}
```

Backend:
- loads the authoritative order by `orderId`
- uses the persisted `Order.total` as the payment amount
- creates the payment record
- does not trust a payment amount supplied by React

Example response:

```json
{
  "id": "pay1",
  "orderId": "o100",
  "paymentMethod": "CREDIT_CARD",
  "amount": 9.0,
  "paymentStatus": "COMPLETED",
  "paymentDate": "2026-09-22T10:30:00Z"
}
```

## Analytics

### GET `/api/analytics/summary`

Optional query parameter:
- `storeId` — required for manager-scoped analytics; omitted for owner-wide analytics.

Example response:

```json
{
  "totalOrders": 12,
  "revenue": 428.5,
  "averageOrderValue": 35.71,
  "topSellingProducts": [
    {
      "productId": "p1",
      "name": "Chocolate Cupcake",
      "quantitySold": 24
    }
  ]
}
```

Calculate analytics from persisted application orders. Do not use an external analytics platform.

## Franchise Locations

### GET `/api/locations/nearest`

Returns franchise stores ordered from closest to farthest for an approved demo address or ZIP code.

Query parameters:
- `demoAddressOrZip` (required) — must match one approved demo ZIP code or address configured in Spring Boot.

Backend behavior:
- resolves the approved demo address or ZIP to predefined coordinates
- calculates straight-line distance using the Haversine formula
- returns distances in miles
- identifies the nearest store
- sorts stores from closest to farthest
- rejects unsupported demo inputs with a `400 Bad Request`

Example request:

```text
GET /api/locations/nearest?demoAddressOrZip=97205
```

Example response:

```json
{
  "customerLocation": {
    "matchedDemoLocation": "97205",
    "latitude": 45.5152,
    "longitude": -122.6784
  },
  "nearestStore": {
    "id": "store24",
    "storeName": "Frosted Corner - Portland",
    "street": "2490 Burnside Street",
    "city": "Portland",
    "state": "OR",
    "zipCode": "97205",
    "managerName": "Sage Peterson",
    "latitude": 45.5152,
    "longitude": -122.6784,
    "distanceMiles": 0.0
  },
  "stores": [
    {
      "id": "store24",
      "storeName": "Frosted Corner - Portland",
      "street": "2490 Burnside Street",
      "city": "Portland",
      "state": "OR",
      "zipCode": "97205",
      "managerName": "Sage Peterson",
      "latitude": 45.5152,
      "longitude": -122.6784,
      "distanceMiles": 0.0
    },
    {
      "id": "store17",
      "storeName": "Frosted Corner - Seattle",
      "street": "1755 Pine Street",
      "city": "Seattle",
      "state": "WA",
      "zipCode": "98101",
      "managerName": "Harper Adams",
      "latitude": 47.6062,
      "longitude": -122.3321,
      "distanceMiles": 145.42
    }
  ]
}
```

## Conversational Ordering

### POST `/api/assistant/chat`

Request:

```json
{
  "customerId": "c1",
  "message": "I need desserts for a birthday party for 10 people and prefer chocolate."
}
```

Expected behavior:
- `DemoAiClient` deterministically identifies supported intents and preferences.
- Supported demo intents include birthday or event requests, chocolate or other flavor preferences, serving count, product recommendations, add-to-cart requests, and a small set of basic Frosted Corner questions.
- Spring Boot retrieves real products and customer data from MongoDB Atlas.
- The response references real product IDs and authoritative product details.
- `DemoAiClient` must not determine price, inventory, totals, discounts, customer records, or product existence.
- Add-to-cart behavior must reuse existing cart and inventory validation.
- Voice input sends browser speech-to-text results to this same endpoint.

Suggested response:

```json
{
  "interpretedRequest": {
    "event": "Birthday",
    "servings": 10,
    "preferences": ["chocolate"]
  },
  "message": "Here are a few birthday-friendly chocolate options.",
  "suggestedAction": "ADD_TO_CART",
  "offer": {
    "eligible": true,
    "discountPercent": 10,
    "message": "You qualify for a 10% birthday offer. Final pricing is calculated by the backend."
  },
  "recommendations": [
    {
      "productId": "p1",
      "name": "Chocolate Cupcake",
      "price": 4.5,
      "suggestedQuantity": 10,
      "reason": "Matches the chocolate preference and serving count."
    }
  ]
}
```

Pricing, eligibility, discounts, inventory validation, and cart/order totals remain authoritative in Spring Boot.

## Subscriptions

### GET `/api/subscription-plans`

Returns fixed MVP subscription plan definitions.

Example response:

```json
[
  {
    "planCode": "monthly-deluxe",
    "name": "Monthly Deluxe Box",
    "description": "4 premium desserts delivered monthly"
  }
]
```

### POST `/api/subscriptions`

Creates an active customer subscription.

Request:

```json
{
  "customerId": "c1",
  "planCode": "monthly-deluxe"
}
```

Example response:

```json
{
  "id": "sub1",
  "customerId": "c1",
  "planCode": "monthly-deluxe",
  "status": "ACTIVE",
  "startedAt": "2026-09-22T10:30:00Z",
  "nextDeliveryDate": "2026-10-22"
}
```

### GET `/api/customers/{customerId}/subscription`

Returns the customer's active subscription and next delivery date.

Example response:

```json
{
  "id": "sub1",
  "customerId": "c1",
  "planCode": "monthly-deluxe",
  "status": "ACTIVE",
  "startedAt": "2026-09-22T10:30:00Z",
  "nextDeliveryDate": "2026-10-22"
}
```

## Personalization

Keep personalization simple.

Suggested endpoints:
- `GET /api/customers/{customerId}/recommendations` for customer-specific product recommendations.
- `GET /api/customers/{customerId}/orders` for prior orders and an "Order Again" experience.

Recommendations must use real products and persisted customer/order data. Do not add a separate recommendation platform.