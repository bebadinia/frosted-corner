# Backend

Developer 2 should scaffold one Spring Boot application in this directory.

Recommended initial dependencies:

- Spring Web
- Spring Data MongoDB
- Bean Validation
- Spring Boot Test

Runtime MongoDB configuration comes from environment variables:

- `MONGODB_URI` for the MongoDB Atlas connection string
- `MONGODB_DATABASE` for the Atlas database name

Suggested base package:

`com.frostedcorner`

Do not create separate services for the business capabilities.
