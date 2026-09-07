# WortWerk implementation plan

Native Android app for a Hungarian-speaking beginner, built with Java and Android framework views.

1. Home: five illustrated categories, word counts, locally saved progress.
2. Learn: German article and word, Hungarian meaning, icon, bilingual example, pronunciation through Android TTS, mark/unmark learned.
3. Practice: five Hungarian–German pairs per round, independent shuffling, immediate feedback, score, and category-specific exercises.
4. Save learned word IDs and best category scores locally. Preserve active exercise on rotation. No account or internet permission.
5. Validate data and matching logic, build debug APK, and document device checks.

Content: 500 entries (100 per category) across software engineering, welding, electrical engineering, cooking, and ordering food. Technical examples teach vocabulary, not procedures.
Expansion: dynamic totals, direct word picker, saved category practice coverage, and 20 non-repeating five-pair rounds per 100-word cycle. Original entry indices remain unchanged. App version: 1.1.0.
