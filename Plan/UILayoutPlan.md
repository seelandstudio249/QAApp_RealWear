# UI Layout Plan — QA App for RealWear Navigate 520

This document defines the visual design system and per-screen layout specs for the QA App. `implementationPlan.md` references this file for all UI/UX details; this file does not repeat flow/API logic already defined there.

**Design base resolution:** 1280 × 720 (16:9), scalable/responsive layout — all dimensions below given as % of screen or relative units, not fixed px, so the layout adapts if resolution differs.
**Theme:** Industrial Dark Theme.
**Voice interaction:** Handled by RealWear's OS-level "look and speak" (WearHF) overlay — no app-side voice command mapping needed. However, per RealWear's own UX guidelines, button **labels must be all-caps** so WearHF can cleanly match spoken commands to visible text, and UI should stay lean/uncluttered so WearHF overlays don't compete with too many simultaneous interactive elements.

---

## 1. Design Tokens

### 1.1 Color Palette (Industrial Dark Theme)

| Token | Hex (suggested) | Usage |
|---|---|---|
| `bg-primary` | `#121417` | Main screen background (near-black, high contrast) |
| `bg-surface` | `#1E2126` | Cards, bars, panels sitting above background |
| `bg-surface-raised` | `#2A2E35` | Buttons, list items (unselected/idle state) |
| `accent-blue` | `#2F8FFF` | Primary actions, focus/selection highlight, links, active tab |
| `accent-green` | `#3DCB6C` | Success, "Fixed", "Complete", verified states |
| `accent-red` | `#FF4D4D` | Errors, "Login Fail", failure states, destructive actions |
| `text-primary` | `#F5F6F7` | Primary text on dark background |
| `text-secondary` | `#A7ADB6` | Secondary/meta text (timestamps, helper text) |
| `divider` | `#33373E` | Borders, separators |

Rule: never use color alone to convey status — always pair with an icon or text label (accessibility, and because glare on industrial displays can wash out color).

### 1.2 Typography

Baseline per RealWear's own UX guidance (no text smaller than 16sp, ideally 20sp+ for anything important):

| Style | Size | Weight | Case | Usage |
|---|---|---|---|---|
| Display / Status | 32sp+ | Bold | Sentence | Login status text, Popup title |
| Screen Title | 26sp | Bold | Sentence | Top bar titles ("Job Hub", "Steps - 2") |
| Body | 20–22sp | Regular | Sentence | List items, descriptions, Detect List rows |
| Button Label | 20sp+ | Bold | **ALL CAPS** | All buttons, per RealWear guidance |
| Meta / Timestamp | 16sp (minimum) | Regular | Sentence | Job created date/time |

Font: system default sans-serif (avoid ambiguous character pairs like `l`/`I`/`1` — verify chosen font disambiguates these, given glare/distance conditions typical on industrial displays).

Line height: 1.4–1.5× font size across all styles for legibility under motion/vibration.

### 1.3 Spacing & Grid

- Base spacing unit: 8px grid (multiples of 8 for all padding/margins).
- Top bar height: 10% of screen height.
- Bottom bar height: 14% of screen height (larger — holds primary action buttons, needs bigger tap/focus targets).
- Content area: remaining ~76% of screen height.
- Minimum interactive element size: 15% of screen height OR 64px, whichever is larger (hands-free/gloved-adjacent precision is lower than touch; WearHF cursor targeting needs generous hit areas).

### 1.4 Focus / Selection State

RealWear's cursor (head-tracking) highlights the focused element before "look and speak" confirms it — but per RealWear's own guidance, apps should still use their standard UI kit button/group styling so behavior is predictable for users already familiar with the device. This plan defines a consistent visual style so appearance stays consistent with our theme:

- **Focus ring:** 3px solid `accent-blue`, 4px outer glow at 30% opacity, applied to any element under the WearHF cursor.
- **Selected/active state** (e.g. active tab, chosen Detect List item): filled `accent-blue` background at 15% opacity + solid `accent-blue` left border (4px).
- **Pressed/confirmed state:** brief 150ms scale-down (0.96×) + flash to full `accent-blue` fill, then transitions to result state (e.g. navigates, or shows success/error).
- Disabled buttons: `bg-surface-raised` at 40% opacity, no focus ring, `text-secondary` label.

### 1.5 Icons

Minimal, per RealWear guidance to avoid clutter. Line-style icons, 2px stroke, sized to match adjacent text line-height. Icons never used as the *only* indicator of meaning — always paired with a text label.

---

## 2. Login Module

**Layout**
- Top bar (10%): "LOGIN" title, centered.
- Content area (76%): centered **square** camera frame, sized to 60% of the shorter screen dimension, `accent-blue` 2px border, rounded corners (8px).
- Status area (14%, bottom of camera frame / above bottom edge): status text, centered, Display style (32sp+).

**Status states (animated dots style)**
| State | Text |
|---|---|
| Scanning | `SCANNING.` → `SCANNING..` → `SCANNING...` (loop, ~400ms/step) |
| Logging in | `LOGGING IN.` → `..` → `...` (loop) |
| Complete | `LOGIN COMPLETE` (static, no dots, held ~1s before navigating) |

**Failure:** Pop Up Module (see §7) — Title "LOGIN FAILED", `accent-red`, error icon, 3s duration, auto-dismiss, resumes Scanning state.

---

## 3. Job Hub Module

**Top bar (10%):** Title left-aligned — "JOB HUB" default, or "JOB – OUTSTANDING" / "JOB – IN PROGRESS" / "JOB – IN VERIFY" / "JOB – COMPLETED" when a tab is open. "PREVIOUS PAGE" button top-right, visible only in sub-list view.

**Content area — Grid view (76%):** 2×2 grid, each tile ~45% width × 45% height with 8px-grid gutters. Each tile: icon (top, ~40% of tile height) + label below (ALL CAPS, Button Label style).

| Tile | Icon concept | Color accent on focus |
|---|---|---|
| OUTSTANDING | Clipboard/list | `accent-blue` |
| IN PROGRESS | Wrench/gear | `accent-blue` |
| IN VERIFY | Magnifying glass/checkmark-pending | `accent-blue` |
| COMPLETED | Checkmark/badge | `accent-green` |

**Content area — Sub-list view (76%):** Vertical scrollable list. Each Job Tile row (fields: title + created date/time only, per confirmed scope):
- Row height: ~12% of content area.
- Left: Job title (Body style, bold).
- Right-aligned: created date/time (Meta style, `text-secondary`).
- Full row is the focusable/selectable target (large hit area).
- Lazy-load: 5 items per page; next 5 auto-fetch when scroll nears bottom (last ~1 item visible triggers next fetch); show a small inline loading indicator at list bottom during fetch, no separate "Load More" control.
- Sort: descending by date/time (latest first) — already reflected in list order, no manual sort UI needed.

---

## 4. Procedures Module

**Top bar (10%):** Left-aligned title in format `STEP {n} - {TextTitle}` (omit `- {TextTitle}` if not provided). Right side of top bar: step-progress indicator, format `STEP {n} OF {total}` as a small text + dot-progress bar (filled dots for completed/current, hollow for upcoming).

**Bottom bar (14%), button weight:** All bottom-bar buttons share equal visual weight (same size, same `bg-surface-raised` idle style, `accent-blue` focus ring) — no primary/secondary distinction, per confirmed scope. Only color-coding exception: "COMPLETE JOB" gets an `accent-green` fill (idle, not just on focus) since it's a terminal/confirming action distinct from mid-flow navigation.

### 4.1 Steps Pattern
- Content area: full-bleed step image, centered, `contain` fit (no cropping), `bg-surface` letterboxing if aspect ratio doesn't match.
- Bottom bar: "PREVIOUS STEP" (left, hidden on Step 1) / "NEXT STEP" (right).

### 4.2 Capture Pattern
- Content area: Camera Module full-screen viewfinder (see §6), centered alignment frame overlay.
- Bottom bar: "CAPTURE AGAIN" (left, disabled/hidden until a photo is captured) / "CAPTURE" (center, primary trigger) / "VERIFY CAPTURE" (right, disabled until a photo is captured).

### 4.3 Review Pattern
- Content area split 70/30:
  - **Left (70%):** captured/response photo, `contain` fit, `bg-surface` background.
  - **Right (30%):** "DETECT LIST" label (Body style, bold, top) above a scrollable list. Rows are **plain text** (no icons/thumbnails), Body style, with the §1.4 selection style (blue left-border + 15% fill) applied to the chosen row. Only one row selectable at a time (single-select).
- Bottom bar: "PREVIOUS STEP" (left, re-enabled here per revisit flow — navigates to prior step's Review Pattern without re-capturing) / "RETAKE" (secondary text-link style, small, placed just above or beside the photo area rather than the main button row, so it's available but not visually competing with primary bottom-bar actions) / "NEXT STEP" (right) — becomes **"COMPLETE JOB"** (accent-green fill) on the final step.

---

## 5. InProgress Module

**Top bar (10%):** Left-aligned `STEP {n} - {TextTitle}` + step-progress indicator (same style as Procedures §4).

**Content area (76%):** Full-bleed step image (same treatment as Steps Pattern above).

**Bottom bar (14%):**
- Default state: single button, **"DEFECTS FIXED"**, right-aligned (confirmed: only action button — no "Defects Not Fixed" here).
- Camera-active state (after tapping Defects Fixed): swaps to Capture Pattern-style bar — "CAPTURE AGAIN" (left, disabled until captured) / "CAPTURE" (center) / "VERIFY CAPTURE" (right, disabled until captured, `accent-green` fill since it's the confirming action for this module).

---

## 6. InVerify Module

**Top bar (10%):** Same as InProgress — `STEP {n} - {TextTitle}` + step-progress indicator.

**Content area (76%):** Full-bleed step image, same treatment.

**Bottom bar (14%):** Two equal-weight buttons — "DEFECTS NOT FIXED" (left, `accent-red` fill since it flags an unresolved issue) / "DEFECTS FIXED" (right, `accent-green` fill).

---

## 7. Pop Up Module

**Layout:** Fixed-size center card (not size-varying by message length — fixed card, text wraps/scrolls within if needed) over a full-screen dimmed overlay (`bg-primary` at 70% opacity).

- Card: ~55% width × ~35% height, centered, `bg-surface` fill, 12px corner radius, subtle drop shadow.
- Header: icon + title, centered, Display style.
- Body: message text, centered, Body style, `text-secondary`.
- Optional footer: "RETRY" button (only present on error popups with a retry action) — full-width within card, `accent-blue` fill.

**Icon differentiation:**
| Type | Icon | Color | Example |
|---|---|---|---|
| Success | ✓ checkmark (circle) | `accent-green` | "Job Completed" |
| Error | ✕ or ! (triangle/circle) | `accent-red` | "Login Failed", "Upload Failed" |
| Retry / in-progress | ⟳ (circular arrow) | `accent-blue` | shown transiently while a Retry attempt is in flight |

**Animation:** fade-in 0.5s → hold (3s default, 5s for errors, indefinite if awaiting Retry interaction) → fade-out 0.5s → callback.

---

## 8. Camera Module

**Layout:** Full-screen viewfinder, `bg-primary` letterboxing if sensor aspect ≠ screen aspect. Centered alignment frame: thin `accent-blue` rectangle/square outline (matches the capture target shape used by the calling module — square for consistency with Login's frame convention), subtle corner brackets rather than a full border to keep the live feed unobstructed.

No persistent text label needed in-frame; capture controls live in the calling module's bottom bar (Procedures §4.2, InProgress §5).

---

## 9. Cross-Screen Consistency Checklist

- All button labels: ALL CAPS, minimum 20sp.
- All body/meta text: minimum 16sp, 20sp+ preferred for anything status-critical.
- All interactive elements: minimum 15% screen height or 64px hit area, visible `accent-blue` focus ring on WearHF cursor hover.
- Color never the sole status indicator — always paired with icon and/or text.
- Top bar always 10% height, bottom bar always 14% height, across all screens, for predictable cursor targeting.
- Every screen keeps interactive element count low per RealWear guidance — grouping/pagination (e.g. Job Hub's 5-per-page) exists partly to keep WearHF overlay density manageable, not just for performance.
