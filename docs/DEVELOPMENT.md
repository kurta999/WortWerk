# Development

## Stack and layout

Native Java with Android framework views, minSdk 26, targetSdk 36, compile SDK 36.1, AGP 9.1.0, Gradle 9.3.1 and JUnit 4.13.2. There are no third-party runtime dependencies.

| File | Purpose |
| --- | --- |
| `app/src/main/java/hu/wortwerk/MainActivity.java` | Screens, navigation, TTS and local preferences |
| `app/src/main/java/hu/wortwerk/Vocabulary.java` | Vocabulary loader |
| `app/src/main/java/hu/wortwerk/MatchingRound.java` | Matching and scoring state |
| `app/src/main/assets/vocabulary.txt` | Bilingual content |
| `app/src/test/java/hu/wortwerk/LearningTest.java` | Seven data/behavior tests |
| `.github/workflows/android.yml` | CI and draft preview delivery |
| `docs/screenshots/` | Actual emulator screenshots |

## Edit vocabulary

UTF-8 text with pipe-separated fields:

```text
category-id|icon|Hungarian category name|Hungarian description
icon|German word|Hungarian meaning|German sample sentence|Hungarian sample sentence
```

Each category currently contains twelve words. Word IDs use the category ID and entry index; append words instead of reordering existing ones to preserve saved learning progress. If changing the number of entries, update the fixed home-screen total and the content-count assertions in the tests. Avoid pipe characters inside fields.

Use a noun's article in the German field. Keep Hungarian meanings unique within a category so matching exercises remain unambiguous.

## Tests

```sh
bash ./gradlew :app:testDebugUnitTest :app:lintDebug
```

Tests cover missing selections, wrong answers, duplicate matches, scoring, shuffled column integrity, vocabulary completeness and malformed data.

Manual device checklist:
- Browse all categories and sample sentences.
- Mark/unmark learned words and restart the app.
- Finish a round with correct and incorrect answers.
- Rotate during a round; selected/matched words and attempts should survive.
- Check speech with and without German voice data.
- Check large fonts, scrolling, system Back and HyperOS on a physical Xiaomi phone.

## Windows fallback build

On the development host, Gradle could not establish its Java loopback connection. The included fallback script uses Android SDK tools directly:

```powershell
.\build-local.ps1
.\build-local.ps1 -Sdk 'C:\path\to\Android\Sdk' -Jdk 'C:\path\to\jdk'
```

It requires build tools **36.1.0** and platform **android-36.1**. It compiles Java, runs D8, packages resources, aligns the APK and signs it. Output: `app/build/direct/WortWerk-debug.apk`.

The script does not run JUnit or lint. Standard Gradle builds and CI are the primary development path. Keep the generated debug key in the ignored build folder if you need to update an existing direct-build installation.

## Screenshots

Images were captured at 1080 × 2400 on the `medium_phone` Android 16 emulator. They are unmodified app screenshots; they are not Xiaomi HyperOS screenshots. The results image was captured after completing a real matching round.

To replace a screenshot, navigate to the intended app screen, then:

```sh
adb shell screencap -p /sdcard/wortwerk.png
adb pull /sdcard/wortwerk.png docs/screenshots/01-categories.png
```

Review images before committing, particularly for personal notifications. The screenshot gallery is embedded with repository-relative paths so it works after you push to any repository.
