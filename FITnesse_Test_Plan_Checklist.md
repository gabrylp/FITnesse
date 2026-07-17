# FITnesse - QA Test Plan Checklist

**Owner:** QA/Presentation role
**Scope:** Android capstone app, 3 screens, Firebase + Gemini Flash-Lite pipeline
**Status legend:** ☐ Not started · 🔄 In progress · ✅ Verified

---

## 1. Auth Flow
- ☐ Signup with valid email/password succeeds
- ☐ Signup with invalid/malformed email is rejected with clear error
- ☐ Signup with weak/short password is rejected
- ☐ Login with correct credentials succeeds
- ☐ Login with incorrect password shows proper error, doesn't crash
- ☐ Login with non-existent account shows proper error
- ☐ Logout returns user to auth screen and clears session
- ☐ Session persists correctly after app is closed/reopened (if applicable)

## 2. Wardrobe Interface (Core Navigation)
- ☐ Tapping the wardrobe handle opens Main/OOTD screen with correct animation
- ☐ Tapping the left door (calendar sticker) opens Calendar/History screen
- ☐ Tapping the right door (profile sticker) opens Settings/Profile screen
- ☐ All navigation animations run smoothly (no stutter/frame drop) on mid-tier emulator
- ☐ Back navigation from each screen returns to wardrobe correctly
- ☐ No dead zones or mis-tap areas on the illustrated wardrobe

## 3. Photo Upload & Item Analysis (Gemini Vision)
- ☐ Photo upload succeeds from camera
- ☐ Photo upload succeeds from gallery
- ☐ Upload failure (no network) shows appropriate error/retry state
- ☐ Gemini vision analysis returns valid structured JSON (category, subcategory, dominant color, secondary color, pattern, length/fit)
- ☐ Analysis result displays correctly in the item detail view
- ☐ Malformed/unexpected Gemini response is handled gracefully (no crash)
- ☐ Manual override of any AI-detected field saves correctly to Firestore
- ☐ Overridden fields persist after app restart

## 4. Recommendation Engine
- ☐ Daily outfit generates correctly once per day
- ☐ Reasoning text (from Gemini) displays alongside the recommended outfit
- ☐ Reasoning text is coherent and references actual selected items
- ☐ Laundry cooldown correctly excludes recently worn items from recommendations
- ☐ Cooldown toggle in Settings enables/disables the exclusion logic correctly
- ☐ Cooldown duration (default 3 days) is respected precisely — check boundary (day 2 vs day 3 vs day 4)
- ☐ `lastWorn` timestamp updates correctly when an item is marked worn
- ☐ Recommendation regenerates sensibly if wardrobe changes (new item added, item deleted)

## 5. Calendar / History Screen
- ☐ Weekly view displays past recommended outfits accurately
- ☐ Marking an outfit "confirmed worn" updates `confirmedWorn` boolean correctly
- ☐ Confirming worn status triggers `lastWorn` update and cooldown as expected
- ☐ Historical log entries persist correctly across sessions
- ☐ Calendar navigation (week forward/back, if implemented) works without errors

## 6. Settings / Profile Screen
- ☐ Laundry cooldown toggle saves and persists
- ☐ Cooldown duration field accepts valid input and rejects invalid input (negative, non-numeric)
- ☐ Theme preference toggle (light/dark) saves and applies immediately
- ☐ Account/profile settings save correctly to Firestore

## 7. Theming
- ☐ Light mode (white/gold, vintage wood grain) renders correctly on all 3 screens
- ☐ Dark mode (black/gold, same wardrobe silhouette) renders correctly on all 3 screens
- ☐ Theme switch applies instantly without requiring app restart
- ☐ Text contrast is readable in both themes (manual check + Layout Inspector)
- ☐ No leftover light-mode assets bleeding into dark mode or vice versa

## 8. Edge Cases
- ☐ Empty wardrobe (no items uploaded yet) — app doesn't crash, shows sensible empty state
- ☐ No internet connection during item upload — Gemini call fails gracefully
- ☐ No internet connection during daily recommendation — ML Kit offline fallback triggers correctly
- ☐ Duplicate item upload (same photo twice) — handled without data corruption
- ☐ Very large wardrobe (50+ items) — recommendation engine still performs reasonably
- ☐ All items in cooldown simultaneously — app handles "no available items" case without crashing

## 9. Cross-Device / Cross-Version Testing
- ☐ Emulator config 1: _____________ (device + API level) — result: _____
- ☐ Emulator config 2: _____________ (device + API level) — result: _____
- ☐ Emulator config 3: _____________ (device + API level) — result: _____
- ☐ Physical device test (if available): _____________ — result: _____
- ☐ Camera/photo upload flow specifically re-tested on physical device

---

## Bug Log Template
| ID | Screen | Steps to Reproduce | Expected | Actual | Severity | Status |
|----|--------|--------------------|----------|--------|----------|--------|
| 1  |        |                    |          |        | Blocker/Major/Minor | Open |

---

## Pre-Demo Day Sign-Off
- ☐ All blocker/major bugs resolved or have documented workarounds
- ☐ App tested on 2+ device configurations without crashes
- ☐ Both light and dark themes verified across all 3 screens
- ☐ Demo script rehearsed at least once, full run-through
- ☐ Backup screen recording ready
- ☐ Presentation deck includes diagrams from Docs/Diagrams role
