# QAApp Implementation Plan - RealWear Navigator 520

This document outlines the phased implementation strategy for the QAApp, ensuring compliance with RealWear UX guidelines and technical requirements.

## Phase 1: Foundation & Setup
*   **1.1 Project Configuration:**
    *   Verify `minSdk 33` and `targetSdk 35` in `build.gradle.kts`.
    *   Add dependencies: `Navigation Compose`, `CameraX (view, lifecycle, camera2)`, `Coil (image loading)`.
*   **1.2 Manifest & Permissions:**
    *   Ensure `screenOrientation="landscape"` for all activities.
    *   Add permissions: `CAMERA`, `READ_MEDIA_IMAGES`.
*   **1.3 RealWear Theme:**
    *   Define a high-contrast color palette (Accessible black/white/yellow/green).
    *   Set default large font sizes for text elements.

## Phase 2: Domain Models & Mock Data
*   **2.1 Data Classes:**
    *   `User(id, name, avatarUrl)`
    *   `Task(id, dateTime, status)` (Status: Outstanding, InProgress, Complete)
*   **2.2 Repositories:**
    *   `UserRepository`: Seed with 2 mock accounts.
    *   `TaskRepository`: Seed with 3 mock tasks, sorted by date.
*   **2.3 Storage Utility:**
    *   Helper class for directory creation: `QAApp/Task/{TaskID}/`.
    *   Naming convention logic: `{TaskID}_{timestamp}.jpg`.

## Phase 3: RealWear Optimized UI Components
*   **3.1 Voice Selectable Components:**
    *   `VoiceCard`: A card with a clear label for voice triggering (e.g., "SELECT 1").
    *   `VoiceButton`: High-visibility button with centered text.
*   **3.2 Navigation Setup:**
    *   Define routes: `Login`, `TaskList`, `TaskHub`, `Capture`, `Review`.

## Phase 4: Login Module Implementation
*   **4.1 UI:** Horizontal `LazyRow` of User Cards.
*   **4.2 Logic:** Selecting a card saves user state and navigates to Task List.

## Phase 5: Task List Module Implementation
*   **5.1 UI:** Vertical `LazyColumn` of Task Cards.
*   **5.2 Indicators:** Color-coded status badges (Outstanding: Gray, InProgress: Yellow, Complete: Green).
*   **5.3 Interaction:** Selecting a task navigates to the Task Hub for that specific Task ID.

## Phase 6: Task Module (Center Hub & Capture)
*   **6.1 Center Hub:** Large 3-button layout ("Capture Photo", "Review Photo", "Previous Page").
*   **6.2 Capture Mode:**
    *   Integration of CameraX `PreviewView`.
    *   Bottom-aligned "Capture" voice-trigger button.
    *   Post-capture: Save file and provide visual feedback before returning to Hub.

## Phase 7: Review Mode Implementation
*   **7.1 Gallery UI:** Grid of thumbnails for the current Task ID.
*   **7.2 Detail View:**
    *   70% screen width overlay for selected image.
    *   Overlay buttons: "Delete Photo" and "Previous Page".
*   **7.3 File Management:** Implement physical file deletion from storage.

## Phase 8: API & Final Polishing
*   **8.1 API Placeholders:**
    *   Service interface for `uploadImage` and `syncTasks`.
*   **8.2 RealWear Voice Validation:**
    *   Ensure every clickable element has a `contentDescription` or visible text that matches expected voice commands.
*   **8.3 Cleanup:** Remove debug logging and finalize seed data.
