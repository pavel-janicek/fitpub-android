# FitPub Android — Status Assessment & Finalization Plan

Assessment date: 2026-08-25 · App version `0.1.0` (`versionCode 1`)

## Current state

**Stack:** Kotlin + Jetpack Compose (Material3, BOM 2024.09), Navigation-Compose,
Retrofit + kotlinx-serialization, OkHttp, DataStore Preferences, Coil, osmdroid.
Hand-rolled DI via `AppContainer`, MVVM (ViewModels + Repositories). Targets
self-hosted FitPub instances (dynamic base URL via interceptor).

**What exists and looks complete:** full auth flow (server setup, register +
verify code, login, password reset), timeline/discover/analytics/notifications
tabs, activity detail with osmdroid track map, comments & likes, activity
creation (file upload FIT/GPX/TCX + manual entry), profile + edit profile,
settings incl. change password and privacy zones CRUD. The API layer
(`FitPubApi.kt`, ~70 endpoints) is broad and mostly mirrored by repositories.

**Blocking problem: the project does not compile.**
`./gradlew assembleDebug` fails with **28 Kotlin errors across 8 files**:

| File | Errors | Nature |
|---|---|---|
| `ui/discover/DiscoverTab.kt` | 15 | Missing imports (`LoadingIndicator`/`ErrorState`/`EmptyState` from CommonUi, `UserDto`, `UserAvatar`) + generic type mismatch at line 80 |
| `ui/notifications/NotificationsTab.kt` | 3 | Missing `Icon` import, `ArrowBack` needs `Icons.AutoMirrored.Filled.ArrowBack`, experimental M3 API needs OptIn |
| `ui/profile/EditProfileScreen.kt` | 2 | Missing `AppViewModel` import; smart-cast on delegated property |
| `ui/profile/ProfileScreen.kt` | 1 | Missing `androidx.lifecycle.viewmodel.compose.viewModel` import |
| `ui/create/ManualFormParts.kt` → `ManualForm.kt` | 4 | Helpers declared `private` but used from another file in same package |
| `ui/create/ManualFormParts.kt` | 1 | Smart cast impossible on delegated property (`error`) |
| `ui/components/CommonUi.kt` | 1 | Invalid Dp math: `Modifier.padding(size.dp.value / 4.dp)` |
| `ui/activity/ActivityDetailWidgets.kt` | 1 | `c.content` type mismatch (comment DTO field vs String) |

Other observations:
- Dead directories `ui/map/`, `ui/screens/` (empty leftovers).
- `.gradle/`, `app/build/`, `.kotlin/` artifacts are **committed to git**; no `.gitignore`.
- No tests of any kind (no test source sets).
- No README, LICENSE, or CI.
- `ApiClient.isDebug` hardcoded to `true`; AGP 8 has `buildConfig=false` by default so `BuildConfig.DEBUG` isn't available yet.
- Auth token stored unencrypted in DataStore (acknowledged in code comment).
- API endpoints defined but **not wired to any UI**: heatmap (own/user),
  batch import jobs, activity route download, activity image, profile header
  upload/delete, delete account, monthly & yearly summaries, training load,
  timezones. Routes `SEARCH` / `CREATE_MANUAL` / `ANALYTICS_DETAIL` defined but unused.

## Finalization roadmap — one prompt per iteration

### Iteration 1 — Make it compile
> "FitPub Android does not compile — `./gradlew assembleDebug` reports 28 Kotlin
> errors (see PLAN.md table). Fix every error following existing conventions:
> add the missing imports in DiscoverTab.kt, NotificationsTab.kt,
> EditProfileScreen.kt and ProfileScreen.kt; make the ManualFormParts helpers
> non-private; fix the two delegated-property smart casts by capturing locals;
> fix `CommonUi.kt:123` padding math (`size.dp / 4`); resolve the comment
> content type mismatch in ActivityDetailWidgets.kt:87 against CommentDtos.kt;
> resolve the generic mismatch in DiscoverTab.kt:80. Iterate until
> `./gradlew assembleDebug` succeeds, then report the diff summary."

### Iteration 2 — Repo & build hygiene
> "Clean up the project infrastructure: add a proper .gitignore (.gradle/,
> build/, .kotlin/, local.properties, .idea/) and remove tracked build
> artifacts from git; delete empty dirs ui/map and ui/screens; enable
> buildFeatures.buildConfig and replace ApiClient's hardcoded isDebug=true
> with BuildConfig.DEBUG; add a README describing the app, how to point it at
> a self-hosted instance, and how to build; verify assembleDebug still passes."

### Iteration 3 — Runtime verification pass
> "Run the app on an emulator/device and verify each critical flow end-to-end:
> server setup → register/verify → login → timeline browse → open activity
> detail (map renders) → like/comment → create activity via file upload and
> manual entry → edit profile → privacy zones → logout/login. Fix any crash,
> broken state handling, or navigation issue you find. Confirm osmdroid tile
> loading works (user agent configured) and file picker + upload work."

### Iteration 4 — Feature completion
> "Wire the implemented-but-unreachable backend features into the UI, mirroring
> web app parity: heatmap on ProfileScreen (myHeatmap/userHeatmap); monthly &
> yearly summaries and training load in the Analytics tab; batch import
> (GPX/FIT export upload + job status) in Settings or Create; profile header
> image upload/remove in EditProfileScreen; account deletion in Settings;
> remove or implement the unused Routes SEARCH / CREATE_MANUAL /
> ANALYTICS_DETAIL. Keep patterns consistent with existing screens."

### Iteration 5 — Tests & CI
> "Add unit tests: SessionStore.normalizeServerUrl, TrackParser, Format,
> UrlBuilder, repository mapping/error handling (MockWebServer), ViewModel
> logic; plus Compose UI smoke tests for auth and timeline. Add a GitHub
> Actions workflow running ./gradlew assembleDebug testDebugUnitTest lint.
> Get the suite green."

### Iteration 6 — Release hardening
> "Prepare for release: encrypt the auth token (or use EncryptedSharedPreferences);
> centralize error messaging (ErrorMessages.kt) and add retry/offline states;
> enable R8 minification with correct keep rules for kotlinx-serialization/
> Retrofit/osmdroid; add release signing config (via env vars); bump
> versionName/versionCode; final lint cleanup; produce a signed release APK."

### Iteration 7 — On-device activity recording ("Record" feature)
Goal: start an exercise inside the app, record the track with the phone's GPS
while the screen is off / app is backgrounded, then review and share the
resulting activity to the configured FitPub instance. This is a new feature
(no backend changes needed — the existing single-file upload endpoint is reused).

Suggested split into sub-steps (one prompt each if done iteratively):

**7a — Permissions & service skeleton**
> "Add location-recording groundwork: manifest entries for ACCESS_FINE_LOCATION
> (+ COARSE), FOREGROUND_SERVICE, FOREGROUND_SERVICE_LOCATION, POST_NOTIFICATIONS
> (API 33+), and a declared foreground Service with
> android:foregroundServiceType=\"location\"; runtime permission request flow from
> Compose (rememberLauncherForActivityResult) with rationale + settings fallback;
> create TrackRecordingService as a lifecycle-aware foreground service showing an
> ongoing notification (elapsed time, pause & stop actions). Verify the service
> survives backgrounding and process death restarts into the right state."

**7b — Tracking engine**
> "Implement GPS tracking inside TrackRecordingService using Android framework
> LocationManager (the project deliberately avoids Google Play services; revisit
> only if needed): requestUpdates with ~1–3 s interval / ~2 m min distance,
> filtering of inaccurate fixes (accuracy > ~20 m discarded), a state machine
> (idle → recording ⇄ paused → stopped), and incremental persistence of track
> points (Room entity lat/lon/ele/time/accuracy or append-only file) so nothing
> is lost on process death. Expose live state via a shared StateFlow
> (elapsed time, distance via haversine sum, current pace, elevation gain with
> smoothing). Add unit tests for distance/elevation math."

**7c — Recording UI**
> "Build the Record flow in Compose: entry point from a 'Record' button next to
> the + on Timeline/Me (new Route RECORD); pre-start screen with activity type
> picker (reuse ActivityTypes); live recording screen with big elapsed timer,
> distance, pace, elevation gain, and an optional osmdroid mini-map showing the
> live position dot + drawn polyline; pause/resume/stop controls; keep-screen-on
> toggle. Wire UI to the service StateFlow; handle 'recording in progress' state
> app-wide (e.g., banner + guard against starting a second session)."

**7d — Save & share to FitPub**
> "On stop: assemble the recorded session into a GPX 1.1 file (trk/trkseg/trkpt
> with ele + time; separate trkseg per paused segment) stored in app-private
> storage; show a post-workout summary screen (stats + mini-map) where the user
> sets title, description, visibility, activity type; upload via the existing
> multipart upload endpoint (same path as UploadForm); mark pending uploads in
> local storage and retry failed uploads later; after successful server import,
> offer navigation to the created ActivityDetail. Confirm privacy zones are
> applied server-side as with any uploaded track."

**7e — Polish & edge cases**
> "Handle battery/Doze behavior (foreground service exemption check, guidance to
> disable battery optimization), GPS-off prompts, no-fix handling (warn when no
> point captured for N minutes), discard confirmation dialog, low-storage
> behavior, and emulator testing via mock locations. Update README features list
> and take fresh screenshots."

Notes:
- Reuses existing pieces: osmdroid (live map), Format.kt (stat formatting),
  ActivityTypes icons, upload endpoint/multipart plumbing from CreateViewModel.
- New dependencies to consider (keep minimal): none strictly required — Room
  optional (could start with a simple file-backed log); avoid play-services-location.

