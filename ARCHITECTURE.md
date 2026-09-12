# Reading Tutor - System Architecture

## Architectural Principles
1. **Offline-First**: All curriculum, audio logic, progress storage, and UI run without network access.
2. **Separation of Concerns**:
   - `content/`: Structured curriculum JSON assets (`course.json`, `day_01.json` - `day_07.json`).
   - `models/`: Data models for curriculum, activities, attempts, and parent reports.
   - `data/`: Local Room persistence (`ChildProfile`, `DayProgressEntity`, `ActivityAttemptEntity`, `SoundMasteryEntity`).
   - `audio/`: `AudioService` handling pure phonemes, word pronunciation, spoken guidance, and zero-latency synthesized sound effects.
   - `ui/`: Child-friendly Jetpack Compose screens (Home, Lesson Runner, 12 Reusable Activity Engines, Protected Parent Dashboard).
3. **Mastery Tracking**:
   - Differentiates strictly between **INDEPENDENT**, **ASSISTED**, and **RETRY** attempts.
   - Computes strong sounds and areas needing review for actionable parent insights.
4. **Future-Proof**:
   - Extensible for additional weeks (Weeks 2-10).
   - Localized instruction architecture (e.g. English + Tamil helper prompts).
