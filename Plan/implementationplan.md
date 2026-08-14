# Implementation Plan — QA App for RealWear Navigate 520

**Target platform:** Native Android, RealWear WearHF SDK
**Voice control:** Handled entirely by RealWear OS-level "look and speak" overlay — no app-side voice command mapping required. All UI just needs standard tappable/focusable elements.
**Phase strategy:** Phase 1 ships with all backend calls stubbed against local seed data. Phase 2 swaps stubs for real API Gateway endpoints without touching UI/flow logic (see "Seed Data & Stub Layer" below).
**UI/visual reference:** This document covers flow, state, and API logic only. All visual specs — colors, typography, spacing, button styling, icons, focus states — are defined in [`UILayoutPlan.md`](./UILayoutPlan.md); each module section below links to its corresponding layout section.

---

## 1. Architecture Overview

```
Device (RealWear Navigate 520, WearHF SDK)
   │
   ▼
Application (Native Android)
   ├─ UI Layer (Login, Job Hub, Procedures, InProgress, InVerify, Popup, Camera)
   ├─ State Layer (session, job, step state models)
   ├─ Repository Layer (interface only — swappable impl)
   │     ├─ Phase 1: SeedDataRepository (local, in-memory/seed script)
   │     └─ Phase 2: ApiRepository (real HTTP/WebSocket calls)
   ▼
Backend (Phase 2 only)
   ▼
Database (Phase 2 only)
```

**Key principle:** All screens call a `Repository` interface, never a concrete data source directly. Phase 1 binds that interface to seed data; Phase 2 rebinds it to real network calls. This is what lets Phase 2 "implement faster" — no UI/flow rewrite, only swap the implementation behind the interface.

---

## 2. Seed Data & Stub Layer

**Requirement:** All seed/mock data lives in **one single script**, not scattered per module, so it's easy to reference and update.

**File:** `SeedData.kt` (single object/file)

Contents:
- Fixed login string (e.g. `"QA249LOGIN"`) — any other scanned string = fail
- Fake session token generator (returns a static/dummy token string on successful login, stored via the real token-storage plumbing described in §3)
- Seed job lists for Outstanding / In Progress / In Verify / Completed (enough items to exercise 5-per-page lazy loading, e.g. 12–15 each)
- Seed `GetProcedures` response per job ID (steps, titles, images)
- Seed `GetInProgressProcedures` / `GetInVerifyProcedures` responses
- Seed Detect List: `["Detect 1", "Detect 2", "Detect 3", "Detect 4"]` (flat, global, fetched once)
- Simulated network delay + a togglable "simulate failure" flag per call, to test the Pop Up + Retry error path

All repository stub methods pull exclusively from this file. Nothing else in the codebase should hardcode sample data.

---

## 3. Login Module

**UI spec:** [`UILayoutPlan.md` §2 — Login Module](./UILayoutPlan.md#2-login-module) (square camera frame, dot-animation status states, failure popup styling)

**Flow (Phase 1):**
1. QR scanner starts, status text animates "Scanning…"
2. On scan, decode string, status → "Login…"
3. Compare scanned string against the single fixed seed login string.
   - **Match:** status → "Login Complete…", generate and store a fake session token, navigate to Job Hub.
   - **No match / camera error / timeout:** Pop Up Module shows "Login Fail" (red, 3s auto-dismiss), resume scanning.
4. Login does **not** return job list data — Job Hub fetches its own lists on demand (see §4).

**Token-storage plumbing (build now, use fake token now):**
- `SessionManager` component: stores current token (in-memory + persisted, e.g. EncryptedSharedPreferences), exposes `getToken()`, `setToken()`, `clearToken()`.
- All future authenticated calls (Phase 2) pull from `SessionManager`, so switching from fake to real token in Phase 2 requires no call-site changes.
- No refresh/expiry logic needed yet — deferred along with the rest of the auth security tier decision (parked, to finalize Phase 2).

**API:** `PostLogin` — stub in Phase 1, real in Phase 2.

---

## 4. Job Hub Module

**UI spec:** [`UILayoutPlan.md` §3 — Job Hub Module](./UILayoutPlan.md#3-job-hub-module) (2×2 icon grid, sub-list row layout, lazy-load indicator)

**Flow:**
1. After login, land on Job Hub with the 4-square grid. **No list data fetched yet.**
2. Selecting a tab (e.g. Outstanding) calls the corresponding API (`GetOutstandingList`, etc.) for the **first page only**.
3. Sub-list renders with pagination: **5 items per page**, sorted by date/time descending.
4. **Lazy-load trigger:** auto-fetch the next 5 items when the scroll position nears the bottom of the currently loaded list (no manual "Load More" button — fits hands-free/voice usage).
5. Selecting a job item → `GetProcedures` (single ID param, not two duplicate calls) → Procedures Module.
6. "Previous Page" → back to 4-square grid, hides sub-list and the Previous Page button.

**API:** `GetOutstandingList`, `GetInProgressList`, `GetInVerifyList`, `GetCompletedList`, `GetProcedures(id)` — all stubbed against seed lists in Phase 1.

---

## 5. Procedures Module

**UI spec:** [`UILayoutPlan.md` §4 — Procedures Module](./UILayoutPlan.md#4-procedures-module) (step-progress indicator, equal-weight buttons, 70/30 Review Pattern split, plain-text Detect List rows, Retake placement)

### 5.1 Step State Model

Each step is tracked as an object, not a bare index, so revisiting works cleanly:

```
Step {
  index: Int
  title: String
  stepImage: String           // for Steps Pattern
  status: Pending | Captured | Verified
  capturedImage: File?
  verifyResponse: Any?        // cached PostVerifyImage result
  selectedDetectItem: String? // user's Detect List choice, if any
}
```

The Procedures screen holds a `List<Step>` plus a `currentStepIndex` and a `furthestStepIndex` (highest step reached).

### 5.2 Detect List

- `GetDetectList` is called **once, right after login**, and cached for the whole session/job (not per step, not per Review Pattern entry).
- Phase 1 seed: `["Detect 1", "Detect 2", "Detect 3", "Detect 4"]`, no params.
- Noted for later: server may eventually scope this per-step or make it dynamic — repository interface should accept an optional context param even if unused in Phase 1, so Phase 2 can extend without a signature change.

### 5.3 Step-by-Step Flow

- Starts at Step 1, Steps Pattern → Next Step → Capture Pattern → Verify Capture → Review Pattern → Next Step → Step 2 Steps Pattern, etc.
- Last step's Review Pattern: "Next Step" becomes "Complete Job".
- "Complete Job" calls `CompletedJob(jobId)` → triggers Popup ("completed" message) → navigate to Job Hub.

### 5.4 Revisiting Steps (Previous Step from Review Pattern)

Adopting the agreed approach:

- **Review Pattern gets a "Previous Step" control** (currently hidden per the base spec — re-enabled here on the left side).
- Going "Previous Step" from Review Pattern jumps to the **previous step's Review Pattern**, showing its cached `capturedImage` + `verifyResponse` + `selectedDetectItem` — it does **not** re-enter Capture Pattern or re-call `PostVerifyImage`.
- The user can move forward again ("Next Step") through already-verified steps without re-capturing, up to `furthestStepIndex`.
- **"Retake"** action added inside Review Pattern: sends the user back into that step's Capture Pattern, discards its cached image/verify result, and re-runs Capture → `PostVerifyImage` → Review for that step only.
- **`PostUpdateVerifyImage` firing rule:** only fires once, at the moment the user advances **forward past** a step (Next Step / Complete Job) — never on backward navigation or passive re-viewing. If the user changes the Detect List selection on a revisited step and then moves forward again, it fires again with the updated selection (overwrite, not append).

### 5.5 Capture & Verify

- Capture Pattern: Capture → preview; Capture Again → discard preview; Verify Capture → `PostVerifyImage(image)` → Review Pattern.
- Review Pattern: shows response image + Detect List (0 or 1 selection supported, per current spec — multi-select not in scope).
- On Next Step / Complete Job: call `PostUpdateVerifyImage` with the selected Detect item, or with no param if nothing selected.

### 5.6 Error Handling (applies to `PostVerifyImage`, `PostUpdateVerifyImage`, and by extension the InProgress/InVerify update calls too)

- On failure (network drop, bad response, timeout): show Pop Up Module with an error message.
- Popup includes a **"Retry" action** that automatically resubmits the same failed call (same payload) rather than just dismissing.
- User stays on the current screen/state throughout — no forced navigation away on failure.
- This same pattern (Popup + auto-resubmit Retry) should be implemented as one shared component so InProgress and InVerify failures reuse it rather than each module rebuilding it.

**API:** `GetProcedures(id)`, `PostVerifyImage(image)`, `PostUpdateVerifyImage(detectItem?)`, `GetDetectList` (new), `CompletedJob(jobId)` (new, added to this module + API Gateway).

---

## 6. InProgress Module

**UI spec:** [`UILayoutPlan.md` §5 — InProgress Module](./UILayoutPlan.md#5-inprogress-module) (single-button bottom bar, camera-active state swap)

**Clarification applied:** "Defects Fixed" is the **only** action button. The "Defects not Fixed" reference in the original flow text was a copy artifact from InVerify and is dropped from this plan. Bottom bar = "Defects Fixed" only (right-most), consistent with the UI Layout section.

**Flow:**
1. `GetInProgressProcedures(id)` sets step count; each step shows title + image.
2. "Defects Fixed" → starts Camera module → Capture Pattern (Capture / Capture Again / Verify Capture).
3. "Verify Capture" → `PostUpdateInProgressJob(image, stepIndex, "Fixed")`. Requires a captured image to proceed — button disabled/no-op without one.
4. Advances to next step on success; on the final step, success response triggers Popup ("completed") → Job Hub.
5. Applies the same shared Popup+Retry error pattern from §5.6 on failure.

**API:** `GetInProgressProcedures(id)`, `PostUpdateInProgressJob(image, stepIndex, "Fixed")`.

---

## 7. InVerify Module

**UI spec:** [`UILayoutPlan.md` §6 — InVerify Module](./UILayoutPlan.md#6-inverify-module) (two-button bottom bar, red/green fill convention)

No changes from base spec — two-button choice retained (this module is the correct home for "Defects Fixed" / "Defects Not Fixed").

**Flow:**
1. `GetInVerifyProcedures(id)` sets step count; each step shows title + image.
2. "Defects Fixed" → `PostUpdateInVerifyJob(stepIndex, "Fixed")`; "Defects Not Fixed" → `PostUpdateInVerifyJob(stepIndex, "NotFixed")`.
3. Either button advances to next step; on final step, success triggers Popup ("completed") → Job Hub.
4. Same shared Popup+Retry error pattern from §5.6 on failure.

**API:** `GetInVerifyProcedures(id)`, `PostUpdateInVerifyJob(stepIndex, "Fixed"|"NotFixed")`.

---

## 8. Pop Up Module

**UI spec:** [`UILayoutPlan.md` §7 — Pop Up Module](./UILayoutPlan.md#7-pop-up-module) (fixed center card sizing, success/error/retry icon differentiation, animation timing)

Single reusable component, extended slightly beyond the base spec to support the Retry pattern:

```
showPopup(
  title: String,
  message: String,
  durationMs: Int = 3000,      // 5000 for errors
  isError: Boolean = false,
  onRetry: (() -> Unit)? = null,
  onDismiss: (() -> Unit)? = null
)
```

- Non-error popups (Login Fail, Job Completed): fade in 0.5s → hold → fade out 0.5s → `onDismiss()`.
- Error popups with `onRetry` set: show a Retry affordance; selecting it re-invokes the original failed call instead of auto-dismissing.

---

## 9. Camera Module

**UI spec:** [`UILayoutPlan.md` §8 — Camera Module](./UILayoutPlan.md#8-camera-module) (viewfinder framing, alignment frame styling)

Unchanged from base spec: activate hardware → capture full-res photo on trigger → shut down feed. Shared by Procedures (Capture Pattern) and InProgress.

---

## 10. API Gateway

**Phase 1:** every endpoint below is served by `SeedDataRepository` reading from the single `SeedData.kt` script.
**Phase 2:** endpoints implemented for real; error-handling conventions (status codes, retry/backoff policy, offline queueing) to be defined at that time.

| Method | Endpoint | Params | Notes |
|---|---|---|---|
| POST | PostLogin | login string | fixed seed string in Phase 1 |
| POST | PostVerifyImage | image file | |
| POST | PostUpdateVerifyImage | detect item (string/int, optional) | |
| POST | PostUpdateInProgressJob | image, step index, "Fixed" | |
| POST | PostUpdateInVerifyJob | step index, "Fixed"/"NotFixed" | |
| POST | CompletedJob | job ID | **added** |
| GET | GetOutstandingList | — | paginated, 5/page |
| GET | GetInProgressList | — | paginated, 5/page |
| GET | GetInVerifyList | — | paginated, 5/page |
| GET | GetCompletedList | — | paginated, 5/page |
| GET | GetProcedures | job ID | duplicate entry removed, single endpoint |
| GET | GetInProgressProcedures | job ID | |
| GET | GetInVerifyProcedures | job ID | |
| GET | GetDetectList | — (flat/global for now) | **added**, fetched once after login |

---

## 11. Deferred to Phase 2

- Final QR login security tier (Level 1.2 vs 1.3 number-matching) — not finalized.
- Real `PostLogin` implementation, token refresh/expiry, logout.
- API error/retry/timeout conventions at the network layer (offline queueing for spotty field connectivity).
- `GetDetectList` becoming per-step/dynamic instead of flat/global.
- Speaker verification — explicitly out of scope, not planned.

---

## 12. Suggested Build Order

1. Seed data script + Repository interface + SessionManager (token plumbing).
2. Login Module (against seed string).
3. Job Hub Module (lists, pagination, navigation).
4. Pop Up Module (with Retry support) — build early since Procedures/InProgress/InVerify all depend on it.
5. Camera Module.
6. Procedures Module (steps, capture/review, revisit/retake logic, Detect List).
7. InProgress Module.
8. InVerify Module.
9. End-to-end pass: Login → Job Hub → Procedures → Complete Job → back to Job Hub, and same for InProgress/InVerify paths.

---

## 13. Related Documents

- [`UILayoutPlan.md`](./UILayoutPlan.md) — full visual design system (dark theme palette, typography baseline per RealWear UX guidelines, focus/selection states, spacing grid) and per-module layout specs referenced throughout this document. Build design tokens and shared components (Popup, buttons, focus ring) from §1 of that file before implementing individual screens, so every module stays visually consistent from the start.
