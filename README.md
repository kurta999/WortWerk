# WortWerk 🇭🇺 → 🇩🇪

**Basic German for Hungarian speakers — at work, in the workshop and around the table.**

WortWerk is a small native Android vocabulary app with illustrated word cards, bilingual example sentences and matching exercises. It is built with Java and Android framework views.

## Screenshots

Real screenshots from the app running on an Android 16 emulator.

| Pick a category | Learn a word |
| :---: | :---: |
| <img src="docs/screenshots/01-categories.png" width="280" alt="Category list with software engineering, welding and learning progress"> | <img src="docs/screenshots/02-vocabulary.png" width="280" alt="German source code vocabulary card with Hungarian translation and sample sentence"> |
| **Match Hungarian and German** | **See your result** |
| <img src="docs/screenshots/03-matching.png" width="280" alt="Five Hungarian and German words in a matching exercise"> | <img src="docs/screenshots/04-results.png" width="280" alt="Completed matching exercise with accuracy and attempts"> |

## What you can learn

| Category | Hungarian | Words |
| --- | --- | ---: |
| Software engineering | Szoftverfejlesztés | 12 |
| Welding | Hegesztés | 12 |
| Electrical engineering | Elektrotechnika | 12 |
| Cooking | Főzés | 12 |
| Ordering food and drinks | Rendelés | 12 |

Each of the **60 entries** includes an emoji icon, a German word (with its article for nouns), a Hungarian meaning and an example sentence in both languages.

- Listen to German words and sentences through Android text-to-speech.
- Mark words as learned and track progress in each category.
- Practice five randomly chosen pairs within a category.
- Get immediate feedback, accuracy and a saved personal best.
- Continue the current card or exercise after screen rotation.
- Use the vocabulary and exercises offline, without an account.

German pronunciation depends on the phone's TTS engine and installed German voice data. Install an offline German voice if you want offline pronunciation. Learning progress stays on the device; the app has no internet permission or analytics.

## Try it

Requires **Android 8.0 or newer**.

After a successful workflow run, open **Actions → Android CI and preview release → Artifacts → WortWerk-debug**, extract the APK and transfer it to your phone. Open the APK and allow installation from that source if prompted.

With a phone or emulator connected through ADB:

```sh
adb install -r app-debug.apk
adb shell am start -n hu.wortwerk/.MainActivity
```

These are debug builds. APKs signed with different debug keys cannot update one another; uninstalling an older build removes its local progress.

For a short Hungarian user guide, see [Getting started](docs/GETTING_STARTED.md).

## Build locally

Open this folder in Android Studio. Use JDK 17 or newer, Android SDK platform **36.1**, Android build tools **36.0.0**, and the included Gradle wrapper.

**Windows**
```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

**macOS / Linux**
```sh
bash ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Android Studio can configure `local.properties` with your SDK path. Build dependencies need internet access the first time.

See [Development](docs/DEVELOPMENT.md) for project structure, vocabulary editing, tests and the Windows SDK-only fallback build.

## GitHub CI/CD

The included [workflow](.github/workflows/android.yml) runs on pushes, pull requests and manual dispatch:

1. Verify the Gradle wrapper checksum.
2. Run unit tests and Android lint.
3. Build and upload the debug APK.
4. Upload test and lint reports, including on failed checks.

Pushing a `v*` tag also prepares a **draft prerelease** with the tested APK and its SHA-256 checksum. Review it under GitHub Releases and publish it when ready. No signing secrets are required. Play Store deployment and production signing are not configured.

The workflow uses pinned Action commits, read-only permissions for checks and write permission only for the draft-release job. Dependabot checks Action updates monthly.

See [GitHub setup and releases](docs/GITHUB.md) for the first push and version-tag workflow.

## Validation

- Seven vocabulary and matching unit tests pass locally.
- The SDK-only debug build and APK signature/alignment checks pass.
- App installation and launch were verified on an Android 16 emulator.
- Screenshots show the category screen, vocabulary, matching and a completed round.
- The GitHub workflow has not run yet; it will run after you push.
- Xiaomi 15T Pro hardware, HyperOS-specific behavior, speech and rotation have not been separately verified.

## License

No license has been selected yet. Add a `LICENSE` file before offering the project for reuse.
