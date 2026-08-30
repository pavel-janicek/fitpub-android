# Version Checklist

Run this on **every release** before committing release binaries, so the
version is propagated everywhere it appears. Checked items mean "verified for
this release".

## 1. Bump the version

- [ ] `app/build.gradle.kts` — `versionCode` incremented by 1, `versionName` set to the new version
- [ ] `app/src/main/java/com/fpclient/android/FitPubApplication.kt` — osmdroid `Configuration.getInstance().userAgentValue = "FP-Client/<version>"`

## 2. Automatic propagation (no manual edit, but verify)

- [ ] HTTP `User-Agent` header — `ApiClient.kt` builds it from `BuildConfig.VERSION_NAME`; nothing to edit
- [ ] About screen (Settings → About this app) — version comes from `BuildConfig`; nothing to edit

## 3. Documentation

- [ ] `PLAN.md` — header line "Current app version" + `versionCode`, and a ledger row for the release
- [ ] `README.md` — no app version kept here on purpose (it tracks the *server* API version); update only if API compatibility changed
- [ ] Release notes / GitHub release published for the new version

## 4. Verify the build

- [ ] `./gradlew testDebugUnitTest assembleDebug` — tests pass, APK builds
- [ ] APK reports the new version: `aapt dump badging app-debug.apk | grep versionName`
- [ ] Install the release/minified build and check Settings → About shows the new version
- [ ] (If touching the network layer) confirm server logs / instance admin sees `FP-Client/<version>` in the User-Agent
