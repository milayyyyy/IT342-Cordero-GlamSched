# GlamSched - Beauty Booking Platform

GlamSched is a full-stack beauty appointment booking platform that connects clients with beauty artists. Clients can browse services, book appointments, make payments, and leave reviews, while artists can manage services and bookings.

## Project Structure

```
IT342-Cordero-GlamSched/
├── backend/        # Spring Boot REST API
├── web/            # React.js web frontend
└── mobile/         # Android (Kotlin) mobile app
```

## Architecture

This project now follows **Vertical Slice Architecture**. Code is organized by feature/module instead of technical layers.

### Backend slices
- `features/auth`
- `features/booking`
- `features/service`
- `features/payment`
- `features/review`
- `features/user`
- `features/web`
- `shared`

### Web slices
- `web/src/features/auth`
- `web/src/features/booking`
- `web/src/features/dashboard`
- `web/src/features/payment`
- `web/src/features/review`
- `web/src/features/services`
- `web/src/features/user`
- `web/src/shared`

### Mobile slices
- `mobile/app/src/main/java/edu/cit/cordero/glamsched/features/auth`
- `mobile/app/src/main/java/edu/cit/cordero/glamsched/features/booking`
- `mobile/app/src/main/java/edu/cit/cordero/glamsched/features/dashboard`
- `mobile/app/src/main/java/edu/cit/cordero/glamsched/features/user`
- `mobile/app/src/main/java/edu/cit/cordero/glamsched/shared`

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Architecture | REST API                            |
| Backend      | Spring Boot 3.5, Java 17           |
| Database     | PostgreSQL (Supabase)               |
| Web          | React.js, React Router              |
| Mobile       | Android (Kotlin), Material Design 3 |
| API Client   | Retrofit 2.9 + OkHttp 4.12         |

## Features

### Authentication
- User registration with role selection (Client / Artist)
- Login with email and password
- Password hashing with BCrypt

### Client
- Browse services and artist profiles
- Book appointments (date/time/notes)
- Make payments and view payment history
- Leave reviews and manage profile/settings

### Artist
- Manage services (create/update/delete)
- View and manage incoming appointments
- Update profile and account settings

## Prerequisites

- **Java 17** (JDK)
- **Maven** (or use included `mvnw` wrapper)
- **Node.js** (v16+) and npm
- **Android Studio** (Hedgehog or later) with SDK 34

## Getting Started

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

### Web

```bash
cd web
npm install
npm start
```

The web app will start on `http://localhost:3000`.

### Mobile

1. Open the `mobile/` folder in Android Studio.
2. Sync Gradle and let dependencies download.
3. Run on an emulator (API 24+) or physical device.

> Mobile app backend URL for Android emulator: `http://10.0.2.2:8080/`.

## Testing Documentation

- `SoftwareTestPlan_GlamSched.md` - complete software test plan with FR traceability, manual scripts, and automated coverage.
- `FullRegressionReport_GlamSched.md` - regression summary with structure updates, execution evidence, issues, and fixes.

## Core API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login with credentials |
| GET | `/api/services` | Get service listings |
| POST | `/api/services/create` | Create artist service |
| POST | `/api/appointments/book` | Create booking |
| GET | `/api/appointments?clientId={id}` | Client appointments |
| GET | `/api/appointments/artist/{artistId}` | Artist appointments |
| PUT | `/api/appointments/{id}/status` | Update appointment status |
| POST | `/api/payments` | Process payment |
| GET | `/api/payments?clientId={id}` | Payment history |
| POST | `/api/reviews` | Add review |
| GET | `/api/reviews/artist/{artistId}` | Get artist reviews |
| GET | `/api/users/{id}` | Get user profile |
| PUT | `/api/users/{id}` | Update user profile |
| PUT | `/api/users/{id}/password` | Change password |

## Project Configuration

- **Backend port:** 8080
- **Database:** Supabase PostgreSQL (configured in `application.properties`)
- **Android SDK:** compileSdk 34, minSdk 24, targetSdk 34
- **Kotlin JVM target:** 17

## Dependencies

### Backend
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- PostgreSQL Driver
- Spring Boot Starter Test + H2 (test scope)

### Mobile
- AndroidX Core KTX 1.12.0
- Material Components 1.11.0
- Retrofit 2.9.0 + Gson Converter
- OkHttp 4.12.0 + Logging Interceptor
- Fragment KTX 1.6.2

### Web
- React 19
- React Router DOM 7.13

## Notes

- On Windows PowerShell, use `.\mvnw.cmd` for backend Maven wrapper commands.
- Backend test runs require a valid Java/JDK setup (`JAVA_HOME` configured).

## Author

**Cordero** – IT342
