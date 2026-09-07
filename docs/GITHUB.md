# GitHub setup and releases

## First push

Extract the complete source ZIP into your local Git repository, including the hidden `.github` folder. Keep `.git` if you already initialized a repository. Do not commit `local.properties`, build outputs or signing keys.

Create an empty GitHub repository, then run these commands locally, replacing the remote URL:

```sh
git init
git add .
git commit -m "Add WortWerk Android app, screenshots and CI"
git branch -M main
git remote add origin https://github.com/YOUR-USER/YOUR-REPOSITORY.git
git push -u origin main
```

If a remote named `origin` already exists, check `git remote -v` and reuse it instead of adding it again. You can keep a different branch name: CI runs on all pushed branches.

No GitHub push or release has been performed by this project setup.

## Continuous integration

The Android workflow runs tests, lint and a debug build on every push, pull request and manual run. Open the Actions tab to see results.

- `WortWerk-debug`: installable development APK, retained for 30 days.
- `test-and-lint-reports`: HTML/XML reports, retained for 14 days.
- If a check fails, the APK upload and draft-release job do not run.
- No repository secrets are needed. The release job uses GitHub's built-in token.
- If an organization restricts Actions or write permissions, allow these pinned actions and release-job access under its policy.

## Draft preview releases

Update `versionName` and increment `versionCode` in `app/build.gradle` when preparing a new app version, commit the change, then tag that commit:

```sh
git tag v1.0.0
git push origin v1.0.0
```

A passing tag build creates a draft prerelease with `WortWerk-debug.apk` and `SHA256SUMS.txt`. Review and publish it manually in Releases. Re-running the job updates an existing draft; it leaves an already published release unchanged.

The tag is a release label; it does not automatically rewrite the Android app's version. Keep the tag and `versionName` aligned.

## Signing

CI builds are debug-signed with a temporary runner key. They are suitable for trying the app, not production distribution. Installing over a differently signed APK can require uninstalling first, losing local progress.

For production, configure a stable private signing key in GitHub secrets and a release build before adding Play Store deployment. Never commit a keystore or signing password.

## Workflow maintenance

Actions are pinned by commit and have version comments. Dependabot proposes Action updates monthly. The Android command-line tools version is fixed in `scripts/setup-android-ci.sh`.

The workflow verifies the wrapper JAR and the Gradle download. When intentionally upgrading the wrapper, regenerate its checksum file and update `distributionSha256Sum` using the official Gradle checksum.

References: [GitHub Actions artifacts](https://docs.github.com/en/actions/how-tos/store-and-share-data-from-a-workflow) and [Gradle wrapper verification](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:verification).
