# GlamSched – Beauty Booking Platform

GlamSched is a full-stack beauty appointment booking platform that connects clients with beauty artists. The system allows clients to browse artists, view services, and book appointments, while artists can manage their profiles and schedules.

## Project Structure

```
IT342-Cordero-GlamSched/
├── backend/        # Spring Boot REST API
├── web/            # React.js web frontend
└── mobile/         # Android (Kotlin) mobile app
```

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Spring Boot 3.5, Java 17           |
| Database | PostgreSQL (Supabase)               |
| Web      | React.js, React Router              |
| Mobile   | Android (Kotlin), Material Design 3 |
| API      | Retrofit 2.9 + OkHttp 4.12         |

## Features

### Authentication
- User registration with role selection (Client / Artist)
- Login with email and password
- Password hashing with BCrypt

### Client Mobile App
- **Home** – Search bar, category chips (Nail Art, Makeup, Lashes, Brows, Hair), featured artists grid
- **Search** – Browse and search for artists by name, specialty, or service
- **Bookings** – View upcoming and past appointments with status (Confirmed / Pending)
- **Profile** – View account info, edit profile, change password, logout
- **Artist Profile** – View artist details, services with pricing, portfolio
- **Booking Flow** – 4-step process: Select Date → Select Time → Additional Notes → Confirmation

### Web Frontend
- Dashboard, Login, and Register pages
- Responsive UI with React Router navigation

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

> The mobile app connects to the backend at `http://10.0.2.2:8080/` (Android emulator localhost alias).

## API Endpoints

| Method | Endpoint          | Description              | Request Body                                          |
|--------|-------------------|--------------------------|-------------------------------------------------------|
| POST   | `/auth/register`  | Register a new user      | `{ firstName, lastName, email, password, role }`      |
| POST   | `/auth/login`     | Login with credentials   | `{ email, password }`                                 |

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

### Mobile
- AndroidX Core KTX 1.12.0
- Material Components 1.11.0
- Retrofit 2.9.0 + Gson Converter
- OkHttp 4.12.0 + Logging Interceptor
- Fragment KTX 1.6.2

### Web
- React 18
- React Router DOM 7.13

## Author

**Cordero** – IT342
