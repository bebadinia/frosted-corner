# Minimum Data Model

Keep models small. Add fields only when the UI or business rule needs them.

## User

Suggested fields:
- id
- email
- passwordHash
- role (CUSTOMER, EMPLOYEE, MANAGER, OWNER)
- customerId (links to Customer, required when role = CUSTOMER and absent for other roles)
- storeId (optional - links to FranchiseLocation, required when role = EMPLOYEE or MANAGER)
- createdAt

Role-based access (enforced in Spring Boot, not in MongoDB):
- CUSTOMER: place orders, view/edit their own preferences, and manage their own subscription.
- EMPLOYEE: view and update inventory only for their assigned `storeId`.
- MANAGER: everything an EMPLOYEE can do, plus create supply requests and view revenue analytics only for their assigned `storeId`.
- OWNER: view revenue analytics across all stores with no `storeId` restriction.

Do not build a permissions/roles collection. Use a single role enum field and enforce access rules in service-layer checks.

## Product

Suggested fields:
- id
- name
- description
- price
- category
- imageFileName (stored locally in backend/src/main/resources/static/images/products/)
- active

## Customer

Suggested fields:
- id
- name
- email
- phone
- street
- city
- state
- zipCode
- preferences
- favoriteCategories

Each customer account has exactly one `User` with role `CUSTOMER`; the user stores the authoritative link using `customerId`.

Order history can be queried from orders instead of duplicated unless implementation simplicity clearly favors a small reference list.

## FranchiseLocation

Suggested fields:
- id
- storeName
- street
- city
- state
- zipCode
- managerName

## Event

Suggested fields:
- id
- name
- startDate
- endDate
- active

## EventMenu (join table for products in events)

Suggested fields:
- id
- eventId
- productId

## Order

Suggested fields:
- id
- customerId
- storeId (required - links to FranchiseLocation)
- fulfillmentType (LOCAL_DELIVERY, SHIPPING, TAKEOUT)
- fulfillmentFee
- fulfillmentProvider (for simulated local delivery only)
- customer
- deliveryAddress (required for delivery orders only)
- items
- total
- status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- createdAt

Suggested embedded `customer` fields:
- name
- email
- phone

Suggested embedded `deliveryAddress` fields:
- street
- city
- state
- zipCode

Each persisted item should preserve enough order-time information to keep historical totals stable:
- productId
- productName
- unitPrice
- quantity
- lineTotal

## Inventory

Suggested fields:
- id
- storeId (required - links to FranchiseLocation)
- productId
- quantity
- lowStockThreshold

## SupplyRequest

Suggested fields:
- id
- storeId (required - links to FranchiseLocation)
- productId
- quantityRequested
- status (ORDERED, SHIPPED, DELIVERED)
- createdAt

## Payment

Suggested fields:
- id
- orderId
- paymentMethod (CREDIT_CARD, DEBIT_CARD, CASH)
- amount
- paymentDate
- paymentStatus (PENDING, COMPLETED, FAILED)

## Subscription

Suggested fields:
- id
- customerId
- planCode
- status (ACTIVE, PAUSED, CANCELLED)
- startedAt
- nextDeliveryDate
