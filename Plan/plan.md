# QAApp Development Plan - RealWear Navigator 520

## 1. Project Overview
**App Name:** QAApp
**Target Device:** RealWear Navigator 520
**Orientation:** Forever Landscape
**Purpose:** Workflow-based task management and image capture for AI-driven analysis.

---

## 2. UX/UI Design Guidelines (RealWear Specific)
*   **Voice-First Design:** All interactive elements must have clear, unique text labels for voice triggering (e.g., "SELECT ACCOUNT", "START CAPTURE").
*   **High Contrast:** Use high-contrast colors and large fonts for readability in varied light conditions.
*   **Large Click Targets:** Elements should be easy to "see" and "select" using head-tracking or voice.
*   **No Multi-Touch/Gestures:** Rely strictly on selectable elements.
*   **Navigation:** Breadcrumbs or clear "Previous Page" voice commands on every screen.

---

## 3. Module Specifications

### 3.1 Login Module
*   **Seed Data:** 2 User Accounts (Admin, Inspector).
*   **UI:** Horizontal list of cards.
*   **Features:**
    *   Display User Name and Avatar.
    *   Voice command "SELECT [User Name]" to log in.

### 3.2 Task List Module
*   **Seed Data:** 3 Tasks.
*   **Sorting:** Latest tasks first.
*   **UI:** Card list displaying:
    *   Task ID
    *   Date & Time
    *   Status:
        *   `Outstanding` (Default)
        *   `InProgress` (Yellow)
        *   `Complete` (Green)
*   **Navigation:** Select a task card to navigate to the Task Module.

### 3.3 Task Module (Center Hub)
*   **3.3.0 Center Hub:**
    *   Button: "Capture Photo" -> Opens Capture Mode.
    *   Button: "Review Photo" -> Opens Review Mode.
    *   Button: "Previous Page" -> Returns to Task List.

*   **3.3.1 Capture Mode:**
    *   Full-screen camera preview.
    *   Button: "Capture" (Bottom center).
    *   Feature: Save image locally upon capture.
    *   Button: "Previous Page" -> Returns to Center Hub.

*   **3.3.2 Review Mode:**
    *   Thumbnail list of images filtered by `TaskID`.
    *   **Selection:** Voice/Select to scale up image (70% screen size).
    *   **Scale-Up View Buttons:**
        *   "Delete Photo": Removes file and returns to list.
        *   "Previous Page": Scales down and returns to list.
    *   Button: "Previous Page" (from list) -> Returns to Center Hub.

### 3.4 SaveLoad Module
*   **Storage Path:** `QAApp/Task/{TaskID}/`
*   **File Naming:** `{TaskID}_{YYYYMMDD_HHMMSS}.jpg`
*   **Accessibility:** Publicly accessible folder for system gallery/MTP access.

### 3.5 API Module (Future Integration)
*   `POST /upload`: Upload captured image for AI analysis.
*   `GET /tasks`: Retrieve updated task list.
*   `PATCH /tasks/{id}`: Update task status (e.g., to Complete).

---

## 4. Application Flow
1.  **Login Screen:** User selects an account.
2.  **Task List Screen:** User views sorted tasks and selects one.
3.  **Task Center Hub:** User chooses between "Capture Photo" or "Review Photo".
4.  **Capture/Review:** User performs action and returns to Hub or Task List.

---

## 5. Technical Requirements
*   **Minimum SDK:** API 33 (Android 13).
*   **Target SDK:** Latest (API 35).
*   **Language:** Kotlin with Jetpack Compose (Landscape optimized).
*   **Permissions:** Camera, READ_MEDIA_IMAGES (Android 13+ requirement).
*   **Orientation Lock:** Forced landscape in `AndroidManifest.xml`.
