# Implementation Plan & Assumptions - Assignment 03: FUMiniHotelSystem

## 1. Project Identifiers
- **Student Name**: HanNH (CE192048)
- **Class Code**: SE1910
- **Spring Boot Artifact / DB Name**: `A3HanNH_SE1910`
- **React Client Name**: `a3hannh_se1910`

## 2. Key Assumptions & Specifications
1. **Staff Account**:
   - Hardcoded in `application.properties` (not in DB): `app.staff.email=staff@fuminihotel.com`, `app.staff.password=Staff@123`.
   - Role is `STAFF`.
2. **Customer Account & Status**:
   - Stored in `Customer` table with BCrypt hashed password.
   - Role is `CUSTOMER`.
   - Status: `"1"` (Active), `"0"` (Inactive). Inactive customers are rejected at login with 403 Forbidden.
3. **Room Deletion Policy**:
   - If a room is referenced in `BookingDetail`, physical deletion is prevented; `RoomStatus` is updated to `"0"` (Inactive) and a 200 OK message is returned.
   - If not referenced, it is physically deleted.
4. **Customer Deletion Policy**:
   - If a customer is referenced in `BookingReservation`, `CustomerStatus` is updated to `"0"` (Inactive) and a 200 OK message is returned.
   - If not referenced, physically deleted.
5. **Booking Business Logic**:
   - `StartDate < EndDate` and `StartDate >= today`.
   - Room must be active (`roomStatus = "1"`).
   - Overlap check: `newStartDate < detailEndDate && newEndDate > detailStartDate` for non-cancelled reservations.
   - `ActualPrice = RoomPricePerDay * numberOfDays`.
   - `TotalPrice = sum(ActualPrice)`.
   - `BookingDate = today`.
6. **Security & RBAC**:
   - Public: `GET /api/rooms/**`, `GET /api/room-types/**`, `POST /api/auth/login`, `POST /api/auth/register`.
   - Staff: Full management of rooms, room types, customers, bookings.
   - Customer: `GET/PUT /api/customers/me`, `POST /api/bookings/online`, `GET /api/bookings/my-history`.
   - Customers cannot access or modify other customers' data (enforced in service).

## 3. Architecture Roadmap
- **Backend (Spring Boot 3.4.x, Java 17, Maven)**:
  - Strict 3-Layer + Repository: `Controller -> Service (interface + impl) -> Repository -> Entity`
  - DTOs for all requests and responses; no entity or password hash exposed.
  - Global `@RestControllerAdvice` error responses (`timestamp, status, message, errors[]`).
  - H2 test profile for fast, standalone `mvn test`.
- **Frontend (Vite + React, axios)**:
  - `react-router-dom`, `AuthContext`, `ProtectedRoute`.
  - Axios interceptor for JWT bearer header and 401 redirect.
  - Clean modular components with client-side validation and feedback toasts.
  - Vitest + RTL tests.
- **Postman Collection & Documentation**:
  - Full collection with pre-configured requests and sample environments.
