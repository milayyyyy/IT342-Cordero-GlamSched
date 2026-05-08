# Software Test Plan
## GlamSched - Vertical Slice Architecture

**Document Version:** 3.0  
**Date:** 2026-05-08  
**Project:** GlamSched Booking Platform  
**Branch:** `refactor/vertical-slice-main`

---

## 1. Purpose and Scope

This document defines the complete test plan for GlamSched after Vertical Slice refactoring. It covers:

- Functional requirements coverage for backend, web, and mobile.
- Manual test cases with explicit test scripts/steps.
- Automated test cases for major modules.
- Regression execution flow and acceptance criteria.

Out of scope: load/performance testing, penetration testing, and cross-browser matrix beyond Chrome for this phase.

---

## 2. System Under Test

### 2.1 Applications

- `backend/` - Spring Boot REST API (Java 17, Maven wrapper).
- `web/` - React web app (`react-scripts`).
- `mobile/` - Android Kotlin app.

### 2.2 Vertical Slice Modules

- `auth` (register, login, role-aware entry)
- `service` (create/read/update/delete services, reactions)
- `booking` (bookings + status management)
- `payment` (payment processing + history)
- `review` (artist reviews)
- `user` (profile, password, follow, account delete)
- `dashboard` (role-based UX entry and navigation)

---

## 3. Functional Requirements Coverage Matrix

| FR-ID | Requirement | Backend Endpoints | Web Coverage | Mobile Coverage | Manual TC | Automated TC |
|---|---|---|---|---|---|---|
| FR-001 | User registration | `POST /auth/register` | Register page | RegisterActivity | TC-FR001-01..03 | AT-AUTH-REG-01 |
| FR-002 | User login | `POST /auth/login` | Login page | LoginActivity | TC-FR002-01..03 | AT-AUTH-LOGIN-01 |
| FR-003 | Role-based routing/dashboard | login response role + user info | Dashboard route behavior | Client/Artist dashboard activities | TC-FR003-01..02 | AT-WEB-ROUTE-01 |
| FR-004 | Browse services | `GET /api/services` | Dashboard feed / Browse Services | Search/Home/Feed fragments | TC-FR004-01..02 | AT-SVC-GET-01 |
| FR-005 | Book appointment | `POST /api/appointments/book`, `POST /api/appointments` | BookingFlow/BookAppointment | BookingActivity flow | TC-FR005-01..03 | AT-BOOK-CREATE-01 |
| FR-006 | View client appointments | `GET /api/appointments?clientId` | MyAppointments | AppointmentsFragment | TC-FR006-01 | AT-BOOK-QUERY-01 |
| FR-007 | View artist profile | `GET /api/users/{id}/profile` + services by artist | ArtistProfile | ArtistProfileActivity | TC-FR007-01 | AT-USER-PROFILE-01 |
| FR-008 | Booking confirmation | booking save + displayed booking details | Booking confirmation step/page | BookingConfirmationActivity | TC-FR008-01 | AT-WEB-BOOKING-UI-01 |
| FR-009 | Artist dashboard overview | appointments + services stats endpoints | Artist dashboard panel | ArtistDashboardActivity | TC-FR009-01 | AT-WEB-DASH-ARTIST-01 |
| FR-010 | Artist appointment management | `GET /api/appointments/artist/{id}`, `PUT /api/appointments/{id}/status` | ArtistAppointments page | ArtistBookingsFragment | TC-FR010-01..02 | AT-BOOK-STATUS-01 |
| FR-011 | Add/manage service | `POST /api/services/create`, `PATCH /api/services/{id}`, `DELETE /api/services/{id}` | AddServiceModal + services list | AddServiceActivity | TC-FR011-01..03 | AT-SVC-CRUD-01 |
| FR-012 | Payment processing | `POST /api/payments` | PaymentPage | Payment flow screens | TC-FR012-01..02 | AT-PAY-POST-01 |
| FR-013 | Payment history | `GET /api/payments?clientId` | PaymentHistory | PaymentHistoryActivity | TC-FR013-01 | AT-PAY-GET-01 |
| FR-014 | Leave review | `POST /api/reviews`, `GET /api/reviews/artist/{id}` | LeaveReview | Review UI in booking/profile | TC-FR014-01..02 | AT-REV-POST-01 |
| FR-015 | User profile management | `GET/PUT /api/users/{id}`, `PUT /api/users/{id}/password` | UserProfile + Settings | ProfileFragment/UserProfileActivity | TC-FR015-01..03 | AT-USER-UPDATE-01 |
| FR-016 | Logout/session clear | UI session clear logic | Dashboard logout | Profile/settings logout action | TC-FR016-01 | AT-WEB-LOGOUT-01 |

---

## 4. Test Environment and Data

### 4.1 Environment

- Backend URL: `http://localhost:8080`
- Web URL: `http://localhost:3000`
- Mobile API URL: `http://10.0.2.2:8080` (emulator)
- Database: PostgreSQL (Supabase config in backend properties)

### 4.2 Roles and Accounts

- Client account: `client1@test.com / Test@123`
- Artist account: `artist1@test.com / Test@123`
- Duplicate-email account for negative tests.

### 4.3 Test Data Seeds

- At least 2 artists, 5 services, 3 client bookings, 2 completed payments, 2 reviews.
- Reset strategy: truncate test rows or restore DB snapshot before each full regression run.

---

## 5. Manual Test Cases and Test Scripts

Format:
- **ID**
- **Preconditions**
- **Test Steps**
- **Expected Result**

### 5.1 Authentication (`auth`)

**TC-FR001-01: Register valid client**
- Preconditions: user email not existing.
- Steps:
  1. Open Register screen (web/mobile).
  2. Enter valid name, email, password, role `CLIENT`.
  3. Submit.
- Expected: 201/success response and user can log in.

**TC-FR001-02: Register duplicate email**
- Steps: register again with existing email.
- Expected: validation error response; no new user.

**TC-FR002-01: Login with valid credentials**
- Steps: enter valid email/password.
- Expected: success response + user data persisted in session storage/local storage.

**TC-FR002-02: Login invalid password**
- Expected: unauthorized/auth error message.

**TC-FR003-01: Role-based landing for client**
- Steps: login as client.
- Expected: client dashboard/menu shown.

**TC-FR003-02: Role-based landing for artist**
- Steps: login as artist.
- Expected: artist dashboard/menu shown.

### 5.2 Services (`service`)

**TC-FR004-01: Browse all services**
- Steps: open services feed.
- Expected: service cards with artist name, price, photos (if available).

**TC-FR011-01: Artist create service**
- Steps:
  1. Login as artist.
  2. Open Add Service form.
  3. Enter name, description, price, category and submit.
- Expected: service appears in artist list and global browse list.

**TC-FR011-02: Edit service**
- Steps: update service fields.
- Expected: updated values persist after refresh.

**TC-FR011-03: Delete service**
- Steps: delete target service.
- Expected: removed from all relevant lists.

### 5.3 Booking (`booking`)

**TC-FR005-01: Book appointment happy path**
- Steps:
  1. Login as client.
  2. Select service.
  3. Select date and time.
  4. Add notes.
  5. Confirm booking.
- Expected: booking created with `PENDING` status.

**TC-FR005-02: Book with missing required selections**
- Steps: skip date or time then continue.
- Expected: validation alert; booking not submitted.

**TC-FR006-01: Client view own appointments**
- Steps: open My Appointments.
- Expected: list contains only client-owned bookings.

**TC-FR010-01: Artist view own appointments**
- Steps: login as artist and open appointment management.
- Expected: appointments for that artist only.

**TC-FR010-02: Artist update booking status**
- Steps: update a booking status from `PENDING` to `CONFIRMED`/`COMPLETED`.
- Expected: saved status reflected in artist and client views.

**TC-FR008-01: Booking confirmation details**
- Steps: complete booking flow.
- Expected: confirmation view shows correct service, artist, date, time, amount.

### 5.4 Payment (`payment`)

**TC-FR012-01: Process payment**
- Steps:
  1. Open payment page from booking.
  2. Submit payment.
- Expected: payment record created with `COMPLETED`.

**TC-FR012-02: Payment with invalid payload**
- Steps: submit missing fields from API client.
- Expected: error or rejection (no invalid row persisted).

**TC-FR013-01: View payment history**
- Steps: open payment history.
- Expected: user sees their own historical payment records.

### 5.5 Review (`review`)

**TC-FR014-01: Submit review**
- Steps: submit review for artist from eligible client.
- Expected: review saved and visible in artist review list.

**TC-FR014-02: Prevent duplicate review**
- Steps: submit second review for same artist-client pair.
- Expected: duplicate review error returned.

### 5.6 User Profile and Account (`user`)

**TC-FR015-01: View profile**
- Steps: open profile page.
- Expected: profile data loaded correctly.

**TC-FR015-02: Update profile fields**
- Steps: edit name/phone/bio then save.
- Expected: values persist and reload correctly.

**TC-FR015-03: Change password**
- Steps:
  1. Provide current password and valid new password.
  2. Save.
- Expected: success message; old password fails on next login, new password works.

**TC-FR016-01: Logout**
- Steps: click/tap logout.
- Expected: local session cleared and redirected to login screen.

---

## 6. Manual Test Execution Procedure

1. Start backend (`backend`):
   - `.\mvnw.cmd spring-boot:run` (Windows)
2. Start web (`web`):
   - `npm install`
   - `npm start`
3. Start mobile from Android Studio/emulator.
4. Execute tests in sequence:
   - Authentication -> Services -> Booking -> Payment -> Review -> Profile/Logout
5. Record each TC as `PASS/FAIL/BLOCKED` with evidence:
   - screenshots
   - request/response payloads (API tests)
   - logs for failures

---

## 7. Automated Test Cases (Major Modules)

### 7.1 Backend Automated Tests

Current framework and dependency support exists in `backend/pom.xml` (`spring-boot-starter-test`, `spring-security-test`, `h2`).

| Auto TC ID | Module | Type | Target | Expected |
|---|---|---|---|---|
| AT-AUTH-REG-01 | Auth | Integration | `POST /auth/register` valid/duplicate payloads | Correct status and ApiResponse |
| AT-AUTH-LOGIN-01 | Auth | Integration | `POST /auth/login` success/failure | 200 for valid, 401 for invalid |
| AT-SVC-CRUD-01 | Service | Integration | create/update/delete service APIs | Entity lifecycle correct |
| AT-SVC-GET-01 | Service | Integration | list services with DTO mapping | reaction/follow flags mapped |
| AT-BOOK-CREATE-01 | Booking | Integration | `POST /api/appointments/book` | row created as `PENDING` |
| AT-BOOK-QUERY-01 | Booking | Integration | client/artist list endpoints | scoped records returned |
| AT-BOOK-STATUS-01 | Booking | Integration | status update endpoint | status persisted |
| AT-PAY-POST-01 | Payment | Integration | process payment endpoint | status `COMPLETED` |
| AT-PAY-GET-01 | Payment | Integration | payment history endpoint | returns user records |
| AT-REV-POST-01 | Review | Integration | create review + duplicate guard | duplicate blocked |
| AT-USER-UPDATE-01 | User | Integration | profile/password updates | validations enforced |

Run command:
```bash
cd backend
.\mvnw.cmd test
```

### 7.2 Web Automated Tests

Framework available via `react-scripts test`.

| Auto TC ID | Module | Type | Coverage |
|---|---|---|---|
| AT-WEB-ROUTE-01 | Auth/Dashboard | Component + routing | login role route behavior |
| AT-WEB-BOOKING-UI-01 | Booking | Component | step validation and progression |
| AT-WEB-DASH-ARTIST-01 | Dashboard | Component | artist panel rendering/actions |
| AT-WEB-LOGOUT-01 | Auth | Component | logout clears storage and redirects |

Run command:
```bash
cd web
npm test -- --watchAll=false
```

### 7.3 Mobile Automated Tests

Planned major-feature UI automation (Espresso/Instrumentation):

| Auto TC ID | Module | Type | Coverage |
|---|---|---|---|
| AT-MOB-LOGIN-01 | Auth | UI instrumentation | login success/failure |
| AT-MOB-BOOKING-01 | Booking | UI instrumentation | end-to-end booking flow |
| AT-MOB-DASH-01 | Dashboard | UI instrumentation | role-based dashboard landing |
| AT-MOB-PROFILE-01 | User | UI instrumentation | profile update and logout |

Run command (when Gradle wrapper is available in mobile project):
```bash
cd mobile
gradlew connectedAndroidTest
```

---

## 8. Entry / Exit Criteria

### Entry Criteria

- Refactor branch is checked out and synced.
- Backend, web, mobile build/start pre-check complete.
- Test data seeded and known-good credentials available.

### Exit Criteria

- 100% of FR-001 to FR-016 mapped to executed test cases.
- No open Critical/High defects.
- Major-module automated suite executed (or clearly reported blocked with reason).
- Final regression report approved.

---

## 9. Deliverables

- Completed test execution sheet (manual results per TC).
- API request collection / script bundle.
- Automated test run logs:
  - backend test output
  - web test output
  - mobile instrumentation output (when available)
- Defect list with retest evidence.
