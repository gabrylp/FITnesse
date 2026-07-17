# FITnesse - Demo Day Presentation Outline
**Target length:** 10-15 minutes · **Format:** Drop into Google Slides or Canva

---

### Slide 1 — Title
- App name: FITnesse
- One-line tagline (e.g., "Your wardrobe, styled automatically")
- Team names + roles

### Slide 2 — Problem Statement
- Choosing an outfit daily is a small but real friction point
- Existing closet apps require manual outfit-building; FITnesse removes that step
- Keep this to 2-3 sentences max — this is the hook, not the pitch

### Slide 3 — Solution Overview
- FITnesse analyzes your own wardrobe photos with AI and picks a daily outfit for you
- Core differentiator: illustrated interactive wardrobe as the entire navigation model (no bottom nav bar / drawer)
- Explains itself: every recommendation comes with a short natural-language reason

### Slide 4 — The Wardrobe Interface (Concept Slide)
- Visual mockup or screenshot of the wardrobe illustration
- Handle → Main/OOTD, Left door → Calendar/History, Right door → Settings/Profile
- One sentence on why: makes the app feel personal/tactile instead of generic

### Slide 5 — Tech Stack
- Android Studio, Kotlin, Jetpack Compose (Material 3, custom-themed)
- Firebase: Firestore, Auth, Cloud Storage (all free tier)
- Gemini Flash-Lite (vision + text), Google ML Kit for offline fallback
- MVVM architecture
- Callout: entire stack built on free tiers, $0 budget

### Slide 6 — AI Pipeline (Two Distinct Uses)
- **Item Analysis** (once per item): photo → Gemini vision → structured JSON (category, color, pattern, fit)
- **Outfit Reasoning** (once per day): filtered available wardrobe → Gemini text → outfit pick + explanation
- Why split this way: keeps API usage low, well within free daily quota

### Slide 7 — Algorithm Explanation
- **Sandwich method**: light-dark-light layering
- **Color theory**: complementary/analogous color matching
- **Rule of thirds**: proportion balance via length/fit data
- **Laundry cooldown**: excludes recently worn items (default 3-day window, toggleable)
- Emphasize: hybrid approach — deterministic Kotlin filtering + AI selection, not a black box

### Slide 8 — Live Demo Walkthrough
*(This is a placeholder slide — transition point, not content-heavy)*
- Suggested demo flow:
  1. Open app → tap wardrobe handle → show today's outfit + reasoning
  2. Tap left door → show Calendar/History, confirm a past outfit as worn
  3. Tap right door → show Settings, toggle laundry cooldown / theme
  4. Show light/dark mode switch live
- Have backup recording cued up and ready in case live demo fails

### Slide 9 — Architecture / Data Flow Diagram
- Pull directly from Docs/Diagrams role (ERD or architecture diagram)
- Don't rebuild it yourself — just import and cite

### Slide 10 — Lessons Learned
- 2-3 honest bullets: what was harder than expected, what you'd do differently, what worked well
- Good candidates: keeping Gemini calls within free quota, tuning styling rules, cross-device testing surprises

### Slide 11 — What's Next (Stretch Goals)
- Per-category laundry cooldown customization
- Any other explicitly-scoped stretch goals the team has discussed
- Keep brief — this is a capstone, not a pitch deck; don't oversell roadmap

### Slide 12 — Thank You / Q&A
- Team names again
- Optional: GitHub repo link if the team wants to share it

---

## Speaking Part Suggestions (adjust to team)
- **Problem + Solution (Slides 2-4):** whoever pitches best / QA-Presentation role
- **Tech Stack + AI Pipeline (Slides 5-6):** Backend role
- **Algorithm + Live Demo (Slides 7-8):** Backend or Frontend, whoever is most comfortable driving the demo device
- **Architecture Diagram (Slide 9):** Docs/Diagrams role
- **Lessons Learned + Next Steps (Slides 10-11):** whole team, split 1-2 bullets each

## Timing Guide
| Section | Suggested time |
|---|---|
| Slides 1-4 (intro) | 2 min |
| Slides 5-7 (tech/algorithm) | 3-4 min |
| Slide 8 (live demo) | 4-5 min |
| Slide 9 (diagram) | 1 min |
| Slides 10-12 (wrap-up) | 2-3 min |
| **Total** | **~12-15 min** |
