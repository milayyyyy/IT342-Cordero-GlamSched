# Full Regression Test Report
## GlamSched Booking Platform

**Project Name:** GlamSched  
**Repository:** `IT342-Cordero-GlamSched`  
**Branch Under Test:** `refactor/vertical-slice-main`  
**Report Date:** 2026-05-08  
**Prepared By:** Cordero Group

---

## 1. Project Information

### 1.1 System Components

- **Backend:** Spring Boot 3.5.11, Java 17, JPA, PostgreSQL
- **Web:** React app with `react-scripts`
- **Mobile:** Android Kotlin app

### 1.2 Test Context

- Regression run performed after full Vertical Slice refactoring work.
- Validation focused on structure integrity, buildability, and critical flow continuity.

---

## 2. Refactoring Summary

The project was reorganized from layer-based packages/folders to feature-based slices across all applications.

### 2.1 Backend

- Old layer folders removed from active use (`controller`, `service`, `repository`, `model`, `dto`, `config`).
- New feature slices active under `features/`:
  - `auth`, `booking`, `service`, `payment`, `review`, `web`
- Shared resources moved to `shared/` (e.g., response DTO/config).

### 2.2 Web

- Replaced old `pages/components/styles/utils` pattern with:
  - `web/src/features/auth`
  - `web/src/features/booking`
  - `web/src/features/dashboard`
  - `web/src/features/payment`
  - `web/src/features/review`
  - `web/src/features/services`
  - `web/src/features/user`
  - `web/src/shared`
- Routing imports in `App.js` now target feature folders.

### 2.3 Mobile

- Old `api/model/ui` package layout migrated to feature packages:
  - `features/auth`, `features/booking`, `features/dashboard`, `features/user`
  - `shared`
- `AndroidManifest.xml` activity declarations updated to `.features.*` namespaces.

---

## 3. Updated Project Structure

### 3.1 Backend

```text
backend/src/main/java/edu/cit/cordero/glamsched/
  features/
    auth/
    booking/
    payment/
    review/
    service/
    web/
  shared/
```

### 3.2 Web

```text
web/src/
  features/
    auth/
    booking/
    dashboard/
    payment/
    review/
    services/
    user/
  shared/
  App.js
```

### 3.3 Mobile

```text
mobile/app/src/main/java/edu/cit/cordero/glamsched/
  features/
    auth/
    booking/
    dashboard/
    user/
  shared/
```

---

## 4. Test Plan Documentation

Regression execution was based on the prepared test plan:

- **Document:** `SoftwareTestPlan_GlamSched.md`
- **Scope covered in plan:**
  - Functional requirements coverage matrix (FR-001 to FR-016)
  - Manual test cases with scripts/steps
  - Automated test set for major modules
  - Entry/exit criteria and evidence checklist

---

## 5. Automated Test Evidence

### 5.1 Executed Commands and Evidence

| Module | Command | Result | Evidence |
|---|---|---|---|
| Web | `npm run build` | PASS | `Compiled successfully.` |
| Backend | `mvn -q -DskipTests compile` | BLOCKED | `mvn` not found in PATH |
| Backend | `.\mvnw.cmd -q -DskipTests compile` | BLOCKED | `JAVA_HOME environment variable is not defined correctly` |
| Mobile | `./gradlew.bat :app:assembleDebug` | BLOCKED | Gradle wrapper script not found (`./gradlew.bat` unresolved) |

### 5.2 Web Build Output Snapshot

```text
> web@0.1.0 build
> react-scripts build

Creating an optimized production build...
Compiled successfully.
```

### 5.3 Automated Test Execution Status

- **Web automated build verification:** completed successfully.
- **Backend automated execution:** blocked by local environment configuration (`JAVA_HOME`).
- **Mobile automated execution:** blocked due to missing wrapper script in project root command path.

---

## 6. Regression Test Results

### 6.1 Summary

| Area | Result | Notes |
|---|---|---|
| Vertical Slice structure integrity | PASS | Feature folders confirmed in backend/web/mobile |
| Web compile regression | PASS | Build successful after refactor cleanup |
| Backend compile regression | BLOCKED | Environment issue (`JAVA_HOME`) |
| Mobile assemble regression | BLOCKED | Wrapper invocation unavailable |
| Import/path migration checks | PASS | No old-layer path references found in targeted scans |

### 6.2 Functional Regression Observation

- No web build regressions detected after feature-based import migration.
- Cross-feature routing in web remained operational at compile-time.
- Backend/mobile functional runtime verification is pending once environment blockers are resolved.

---

## 7. Issues Found

### Issue REG-001 - Backend compile blocked
- **Severity:** High (execution blocker)
- **Description:** Maven wrapper cannot run because `JAVA_HOME` is not configured correctly.
- **Impact:** Backend automated tests/compile could not be executed in this run.

### Issue REG-002 - Mobile assemble blocked
- **Severity:** High (execution blocker)
- **Description:** Gradle wrapper command used in shell did not resolve (`./gradlew.bat` not found from invocation).
- **Impact:** Mobile automated build/tests were not executed from CLI in this run.

### Issue REG-003 - Web lint warnings after migration
- **Severity:** Medium
- **Description:** Unused variables/imports reported post-refactor in feature files.
- **Impact:** No build failure, but code quality warning noise.

---

## 8. Fixes Applied

### FIX-001 - Web unused import cleanup
- **File:** `web/src/App.js`
- **Change:** Removed unused `PaymentPage` import.

### FIX-002 - Booking flow dead code cleanup
- **File:** `web/src/features/booking/BookingFlow.js`
- **Change:** Removed unused `services` state and unused `handleServiceSelect` function.

### FIX-003 - Dashboard favorites tracking adjustment
- **File:** `web/src/features/dashboard/Dashboard.js`
- **Change:** Refactored local favorites tracking to `useRef`-based current-state persistence for reaction sync path.

### FIX-004 - Web build re-validation
- **Action:** Re-ran `npm run build`
- **Result:** `Compiled successfully`

---

## 9. Final Regression Verdict

**Partial PASS (with environment blockers).**

- **Passed:** Refactoring structure checks, web compile regression, and web cleanup verification.
- **Blocked:** Backend and mobile automated execution due to local tooling/environment setup.

### Required Follow-up Before Final Sign-off

1. Configure `JAVA_HOME` and rerun backend compile/tests via `.\mvnw.cmd test`.
2. Run mobile build/tests through Android Studio or correct Gradle wrapper invocation.
3. Attach backend/mobile execution logs to this report and update verdict to final PASS once green.
