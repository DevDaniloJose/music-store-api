#  Vada Store: Music Store API

A REST API for a music store that sells items such as shirts, posters, CDs and vinyl records. It was built with Java and Spring Boot. The main focus of this version is secure authentication and a shopping cart.

>  **Please note:** this is an initial version, currently being refactored. I'm rewriting the project with better modeling, and this version will be replaced soon. Below is exactly what works today and what isn't ready yet.

---

##  Technologies

- Java 21
- Spring Boot (Web, Data JPA, Security, Validation, Mail)
- PostgreSQL (the upcoming rewrite will use MySQL)
- JWT (jjwt) for authentication
- Bucket4j for rate limiting
- Lombok
- Maven

---

## What works right now

- **Sign-up:** creates a user with the buyer role (`BUYER`) and a cart
- **Login:** returns an access token (JWT, 15 min) and a refresh token (7 days)
- **Token refresh:** generates a new access token without logging in again
- **Logout:** invalidates the refresh token
- **Profile:** returns the data of the logged-in user
- **Cart:** returns the logged-in user's cart
- **Discount coupons:** applies a coupon to the cart (`VADA10`, `VADA15`, `VADA20`, `VADAVADA`)
- **Rate limiting:** limits requests to 10 per minute per IP on `/auth/**` and `/reset-password`
- **Access control:** routes protected by token and role

### Security

- Passwords hashed with BCrypt
- Stateless authentication with signed JWTs (HS256)
- Refresh tokens stored in the database and removed on logout
- JWT secret read from an environment variable
- Administration routes restricted to the `ADMIN` role
- Rate limiting against brute-force attacks on authentication routes

---

## Still in development

These parts exist in the code, but have **not been tested yet** or depend on features I haven't built:

- Password recovery (`/reset-password`): requires a configured mail server
- Cart items (add, remove, update quantity, clear): depend on the product catalog
- Checkout and order creation: depend on products and addresses
- Guest cart (merged into the user's cart on login): implemented, not tested
- Admin creation (`/users/admin/createAdmin`): requires an initial admin in the database

**Doesn't exist yet:** endpoints for products, orders, sellers and addresses; shipping fee calculation; payment integration.

---

## Let's get started already!!

### Prerequisites

- Java 21
- Maven
- PostgreSQL

### Step by step

**1. Clone the repository**

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
cd YOUR_REPOSITORY
```

**2. Create the database**

```sql
CREATE DATABASE vadastore;
```

Check the database username and password in `src/main/resources/application.properties` and change them to match yours.

**3. Set the JWT secret key**

The key must be a **64-character hexadecimal** string. To generate one:

```bash
openssl rand -hex 32
```

Then set the environment variable before running the app:

```bash
# Linux / Mac / Git Bash
export JWT_SECRET_KEY=your_generated_key_here

# Windows (PowerShell)
$env:JWT_SECRET_KEY="your_generated_key_here"
```

**4. Run the application**

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`. The tables are created automatically on the first run.

---

## Endpoints

### Authentication (no token required)

| Method | Route | Body | Response |
|---|---|---|---|
| POST | `/auth/signup` | `username`, `email`, `password` | 201 + email, id, token |
| POST | `/auth/login` | `email`, `password` | 200 + tokens |
| POST | `/auth/refresh` | `refreshToken` | 200 + new `accessToken` |
| POST | `/auth/logout` | `refreshToken` | 204 |

### User and cart (token required)

| Method | Route | Body | Description |
|---|---|---|---|
| GET | `/users/me` | none | Data of the logged-in user |
| GET | `/cart` | none | Cart of the logged-in user |
| POST | `/cart/applyCoupon` | `coupon` | Applies a coupon to the cart |

Protected routes require this header:

```
Authorization: Bearer <accessToken>
```

### Examples

**Sign-up**

```http
POST /auth/signup
Content-Type: application/json

{
  "username": "maria",
  "email": "maria@email.com",
  "password": "password1234"
}
```

**Login**

```http
POST /auth/login
Content-Type: application/json

{
  "email": "maria@email.com",
  "password": "password1234"
}
```

Response:

```json
{
  "username": "maria",
  "email": "maria@email.com",
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "68f41d0a-5be8-481c-941c-af9320602ce1",
  "userId": 1
}
```

**Refresh the token**

```http
POST /auth/refresh
Content-Type: application/json

{ "refreshToken": "68f41d0a-5be8-481c-941c-af9320602ce1" }
```

**Apply a coupon**

```http
POST /cart/applyCoupon
Authorization: Bearer <accessToken>
Content-Type: application/json

{ "coupon": "VADA10" }
```

### Available coupons

| Coupon | Discount |
|---|---|
| `VADA10` | 10% |
| `VADA15` | 15% |
| `VADA20` | 20% |
| `VADAVADA` | 25% |

---

##  Manual testing

Tested with Postman:

- ✅ Sign-up, login, token refresh and logout
- ✅ Profile (`/users/me`) and cart (`/cart`) queries
- ✅ Valid and invalid coupons
- ✅ Requests without a token return 401
- ✅ Login with a wrong password is rejected
- ✅ Rate limit: the 11th consecutive request to `/auth/login` returns 429
- ✅ A buyer can't access the admin creation route (403)
- ✅ A refresh token stops working after logout

---

##  Project structure

```
src/main/java/com/vadastore/music_store_api/
├── controller/   # REST endpoints
├── service/      # Business logic
├── repository/   # Database access (Spring Data JPA)
├── domain/       # JPA entities
├── record/       # Request/response DTOs
├── enums/        # Roles, order status, coupons, etc.
├── exceptions/   # Custom exceptions
├── security/     # JWT, filters and Spring Security configuration
└── util/         # Helper classes
```

---

##  Next steps

- [ ] Endpoints for products, orders, sellers and addresses
- [ ] Shipping fee calculation
- [ ] Global exception handling
- [ ] Automated tests
- [ ] Payment gateway integration
- [ ] Swagger/OpenAPI documentation
