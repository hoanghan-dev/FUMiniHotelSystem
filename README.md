# FUMiniHotelSystem (Assignment 03 - Securing REST Services with Spring Boot)

## 1. Project Overview & Deliverables
- **Course**: SBA301 - Java Web Application Development / Spring Boot
- **Assignment**: Assignment 03 - Securing REST Services with Spring Boot
- **Student Name**: HanNH (CE192048)
- **Class Code**: SE1910
- **Backend Project Name & Artifact**: `A3HanNH_SE1910` (Spring Boot 3, Java 17, Maven)
- **Frontend Project Name**: `a3hannh_se1910` (Vite + React 18, Axios, React Router 6)
- **Database Name**: `A3HanNH_SE1910` (Microsoft SQL Server, Spring Data JPA `ddl-auto=update`)
- **Postman Collection**: `postman/FUMiniHotel.postman_collection.json`

---

## 2. Default Accounts & Credentials

### Staff Account (Configured in `application.properties`, NOT stored in DB)
- **Email**: `staff@fuminihotel.com` (configurable via `STAFF_EMAIL` env)
- **Password**: `Staff@123` (configurable via `STAFF_PASSWORD` env)
- **Role**: `STAFF`

### Demo Customer Accounts (Pre-seeded by `DataInitializer`)
- **Customer 1**:
  - **Email**: `customer1@hotel.com`
  - **Password**: `Customer@123`
  - **Full Name**: John Doe
  - **Phone**: `0912345678`
  - **Role**: `CUSTOMER`
  - *Note*: Has an existing booking history with Room 101 to verify the room & customer deactivation rules.
- **Customer 2**:
  - **Email**: `customer2@hotel.com`
  - **Password**: `Customer@123`
  - **Full Name**: Jane Smith
  - **Phone**: `0987654321`
  - **Role**: `CUSTOMER`

---

## 3. How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.8+ (or use the provided `./mvnw` wrapper)
- Node.js 18+ and npm
- Microsoft SQL Server running on `localhost:1433` (optional for testing, test suite uses in-memory H2)

### Backend Execution
```bash
# Navigate to backend directory (or stay in root and use wrapper)
cd backend
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`.

To customize the SQL Server connection:
```bash
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=A3HanNH_SE1910;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YourPassword"
./mvnw spring-boot:run
```

### Frontend Execution
```bash
# Navigate to frontend directory
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`.

---

## 4. How to Run Automated Tests

### Backend Unit & Integration Tests (H2 in-memory profile)
```bash
# Run from workspace root or backend folder
./mvnw test
```
- Includes JUnit 5 and Mockito unit tests for service business logic.
- Includes Spring Boot MockMvc integration tests for RBAC, public vs 401 vs 403 matrix, and customer isolation.
- Fast, standalone execution using H2 database (no active SQL Server required).

### Frontend Component & Integration Tests (Vitest)
```bash
# Run from workspace root or frontend folder
npm test -- --run
# Or inside frontend:
cd frontend
npm test -- --run
```
- Covers login form client-side validation.
- Covers `ProtectedRoute` authentication and role redirection.
- Covers date calculation and multi-room total price computations.

### Frontend Production Build
```bash
npm run build
```

---

## 5. REST API Endpoints Specification

| Method | Endpoint | Access Role | Description |
|---|---|---|---|
| **POST** | `/api/auth/login` | Public | Authenticate with email & password, returns JWT and user profile |
| **POST** | `/api/auth/register` | Public | Customer self-registration (name, phone, email, birthday, password) |
| **GET** | `/api/rooms` | Public | List hotel rooms with search & filters (number, type, status, capacity) |
| **GET** | `/api/rooms/{id}` | Public | Get specific room details by ID |
| **POST** | `/api/rooms` | `STAFF` | Create new room with validation (unique number, price > 0, capacity >= 1) |
| **PUT** | `/api/rooms/{id}` | `STAFF` | Update room information |
| **DELETE** | `/api/rooms/{id}` | `STAFF` | Delete room (Soft deactivates if booked; physically deletes if unreferenced) |
| **GET** | `/api/room-types` | Public | List all room types |
| **GET** | `/api/room-types/{id}` | Public | Get room type detail by ID |
| **POST** | `/api/room-types` | `STAFF` | Create room type |
| **PUT** | `/api/room-types/{id}` | `STAFF` | Update room type |
| **DELETE** | `/api/room-types/{id}` | `STAFF` | Delete room type |
| **GET** | `/api/customers` | `STAFF` | List customers with search (name, email, phone) and status filter |
| **GET** | `/api/customers/{id}` | `STAFF` | Get customer details by ID |
| **POST** | `/api/customers` | `STAFF` | Create customer account |
| **PUT** | `/api/customers/{id}` | `STAFF` | Update customer account |
| **DELETE** | `/api/customers/{id}` | `STAFF` | Delete customer (Soft deactivates if has bookings; deletes if unreferenced) |
| **GET** | `/api/customers/me` | `CUSTOMER` | View current logged-in customer's profile |
| **PUT** | `/api/customers/me` | `CUSTOMER` | Update current logged-in customer's profile |
| **POST** | `/api/bookings/online` | `CUSTOMER` | Customer creates online booking with 1 or multiple rooms |
| **GET** | `/api/bookings/my-history` | `CUSTOMER` | Customer views their own booking reservation history |
| **GET** | `/api/bookings` | `STAFF` | List all bookings with customer, status, and date range filters |
| **GET** | `/api/bookings/{id}` | `STAFF` / `CUSTOMER` | Get reservation detail (customer can only access their own reservation) |
| **POST** | `/api/bookings` | `STAFF` | Create booking reservation on behalf of a customer |
| **PUT** | `/api/bookings/{id}` | `STAFF` | Update booking reservation status or details |
| **DELETE** | `/api/bookings/{id}` | `STAFF` | Delete booking reservation |

---

## 6. Business Rules & Architecture Details

1. **Strict 3-Layer + Repository Architecture**:
   - `Controller -> Service (Interface + Implementation) -> Repository -> Entity`
   - Data Transfer Objects (DTOs) used exclusively for API communication; passwords and entity models are never exposed directly.

2. **Room Deletion Policy**:
   - When `DELETE /api/rooms/{id}` is executed:
     - If the room is referenced in any `BookingDetail`, the room is NOT deleted; its `roomStatus` is updated to `"0"` (Inactive), and a response `{"success": true, "message": "Room has booking history and was deactivated instead of deleted.", "actionTaken": "DEACTIVATED"}` is returned.
     - If the room has no booking history, it is physically removed from the database (`"actionTaken": "DELETED"`).

3. **Customer Deletion Policy**:
   - When `DELETE /api/customers/{id}` is executed:
     - If the customer has created any `BookingReservation`, the customer is NOT deleted; `customerStatus` is set to `"0"` (Inactive), returning `"actionTaken": "DEACTIVATED"`.
     - If no reservations exist, the customer is physically deleted (`"actionTaken": "DELETED"`).

4. **Booking Business Logic & Validation**:
   - `StartDate` < `EndDate` and `StartDate` must not be in the past (`StartDate >= today`).
   - Rooms must exist and be active (`roomStatus = "1"`).
   - Overlap validation: A room cannot be booked if an existing non-cancelled reservation has `detailStartDate < newEndDate && detailEndDate > newStartDate`.
   - `ActualPrice = RoomPricePerDay * numberOfDays`.
   - `TotalPrice = sum(ActualPrice)` of all rooms in the reservation.
   - `BookingDate = today`.

5. **Customer Isolation**:
   - Customer accounts can only view and modify their own profile (`/api/customers/me`) and their own reservations (`/api/bookings/my-history`, `/api/bookings/{id}`). Any attempt to access another customer's data throws `403 Forbidden`.

6. **Consistent JSON Error Format**:
   - Global exception handling (`@RestControllerAdvice`) maps all errors to:
     ```json
     {
       "timestamp": "2026-10-27T23:50:00.000Z",
       "status": 400,
       "message": "Validation failed",
       "errors": ["telephone: Telephone must be 10-12 digits"]
     }
     ```

---

## 7. Assumptions
- **Staff Credentials**: Stored in `application.properties` with keys `app.staff.email` and `app.staff.password`, overridable by environment variables. Staff has role `ROLE_STAFF`.
- **Customer Status**: `"1"` indicates Active, `"0"` indicates Inactive. Inactive customers are rejected upon login with `403 Forbidden`.
- **Composite Primary Key**: `BookingDetail` maps `(BookingReservationID, RoomID)` as a composite primary key using `@IdClass(BookingDetailId.class)`.
- **Test Database**: Spring profile `test` activates H2 in MSSQLServer mode with `application-test.properties`, providing self-contained, reproducible testing without requiring external database setup.
