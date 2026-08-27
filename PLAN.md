# FitPub Android — Project Roadmap

Assessment date: 2026-08-25 · Last updated: after completion of Iteration 3
Current app version: **`0.4.1`** (`versionCode 5`)

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

## Versioning policy

The roadmap is grouped into three release gates:

| Gate | Contents | Ships as |
|---|---|---|
| Core finalization | Iterations 1–6 | **v1.0** |
| On-device recording ("Record" feature) | Iteration 7 | **v2.0** |
| Wear OS companion app | Iteration 8 | **v3.0** |

Pre-1.0 policy: each completed roadmap iteration bumps the app to
`0.<N>.0` (`versionName`) and increments `versionCode` by one, so every
shipped APK reflects real, verified progress. Feature work done outside
the numbered iterations (guest mode, instance switching from login/settings,
federated search, layout/inset fixes) folds into the next pre-release bump.
From v1.0 onward the gates above are the versions — recorded here and set
in `app/build.gradle.kts` at each release.

Progress ledger (kept up to date per iteration):

| Version | Milestone | Status |
|---|---|---|
| 0.1.0 | Initial assessment snapshot | ✅ superseded |
| 0.2.0 | Iterations 1+2 — compiles; repo/build hygiene, README | ✅ done |
| 0.4.0 | Iteration 4 — feature completion (heatmap, summaries + training load, batch import, profile header, delete account, route cleanup) | ✅ done |
| **0.4.1 (current)** | Patch — activity descriptions now shown on timeline cards (were dropped by ActivityCard) | ✅ done |
| 0.5.0 | Iteration 5 — tests & CI | ⬜ |
| 0.6.0 | Iteration 6 — release hardening (token encryption, R8, signing) | ⬜ |
| **1.0** | All of the above → first stable release | ⬜ |
| **2.0** | + Iteration 7 — record workouts on-device and share | ⬜ |
| **3.0** | + Iteration 8 — FitPub Wear companion app | ⬜ |

## Roadmap — one prompt per iteration

### Iteration 1 — Make it compile ✅
> "FitPub Android does not compile — `./gradlew assembleDebug` reports 28 Kotlin
> errors (see PLAN.md table). Fix every error following existing conventions:
> add the missing imports in DiscoverTab.kt, NotificationsTab.kt,
> EditProfileScreen.kt and ProfileScreen.kt; make the ManualFormParts helpers
> non-private; fix the two delegated-property smart casts by capturing locals;
> fix `CommonUi.kt:123` padding math (`size.dp / 4`); resolve the comment
> content type mismatch in ActivityDetailWidgets.kt:87 against CommentDtos.kt;
> resolve the generic mismatch in DiscoverTab.kt:80. Iterate until
> `./gradlew assembleDebug` succeeds, then report the diff summary."

### Iteration 2 — Repo & build hygiene ✅
> "Clean up the project infrastructure: add a proper .gitignore (.gradle/,
> build/, .kotlin/, local.properties, .idea/) and remove tracked build
> artifacts from git; delete empty dirs ui/map and ui/screens; enable
> buildFeatures.buildConfig and replace ApiClient's hardcoded isDebug=true
> with BuildConfig.DEBUG; add a README describing the app, how to point it at
> a self-hosted instance, and how to build; verify assembleDebug still passes."

### Iteration 3 — Runtime verification pass ✅
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

> 🚩 Release gate: this iteration ships as **v2.0** (everything through Iteration 6 was v1.0).

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

> 🚩 Release gate: this iteration ships as **v3.0**.

### Iteration 8 — Wear OS companion app ("FitPub Wear")
Goal: a Wear OS companion module so athletes can leave the phone at home,
start/record a workout from the wrist using the watch's internal monitors
(GPS, heart rate, step counter), and have it shared to their FitPub
instance — the open-source equivalent of Strava's wearable experience.
Delivered as a new Gradle module `:wear` alongside `:app`; the phone remains
the sign-in authority and the upload relay.

Suggested split into sub-steps (one prompt each if done iteratively):

**8a — Module & project scaffolding**
> "Create a :wear Wear OS module (build.gradle.kts with com.android.application +
> wearApp wiring in :app via wearApp/unstable bundled dependency, wear_app.xml
> pairing metadata, minSdk matching Wear OS 3+ = API 26–30 target latest),
> set up Compose for Wear OS (androidx.wear.compose:material, navigation),
> round-display-safe layouts (BoxWithConstraints / curved modifiers where useful),
> and a minimal launcher activity proving install-on-watch works from Studio.
> Keep the module independent of :app code except a small :core-shared set or
> duplicated DTOs — decide and document which."

**8b — Phone↔watch sign-in handshake**
> "Implement sign-in relay from the mobile app using the Android Data Layer:
> CapabilityClient advertising 'fitpub_phone' capability, MessageClient handshake
> when the watch requests credentials, phone responds with serverUrl + bearer
> token (+ username/displayName) pulled from SessionStore; watch stores them via
> its own DataStore (document that Data Layer payloads are TLS-equivalent
> protected on the transport but still encrypt-at-rest later per Iteration 6).
> Add 'device signed in as @user' state UI on the watch and a revoke/sign-out
> path both directions; handle no-phone-paired and stale-token states."

**8c — Watch sensor recording engine**
> "Build WorkoutRecordingService on the watch: a foreground service (location +
> bodySensors + activityRecognition types) capturing GPS (onboard GNSS via
> FusedLocationProvider or LocationManager — decide per minSdk/target), heart
> rate (Health Services androidx.health:health-services-client on Wear OS 3+
> with SensorManager TYPE_HEART_RATE fallback), step counter/detector, and
> elapsed time; same state machine semantics as Iteration 7b (recording ⇄
> paused → stopped), incremental persistence to survive process death, live
> StateFlow of HR/distance/pace/steps. Declare BODY_SENSORS (runtime),
> ACTIVITY_RECOGNITION, location permissions; add availability detection
> (no-GPS watches degrade gracefully to HR+steps+time)."

**8d — On-watch recording UX**
> "Compose-for-Wear recording screens optimized for glanceability: big live HR
> (color-coded zones) + duration + distance on one swipeable screen, map-less
> by default (save battery; optional breadcrumb view later); start flow with
> activity-type picker; long-press or dedicated button to pause/stop; Ongoing
> Activity API integration so the workout appears on the watch face/in the
> recents tray; optional Tile ('Start workout') and complication. Handle
> always-on/ambient rendering with burn-in protection."

**8e — Sync & share to FitPub**
> "Post-workout sync: serialize the recorded session (GPX 1.1 for the track +
> JSON sidecar or FIT fields for HR series/steps) on the watch; attempt direct
> multipart upload from the watch when it has connectivity (Wi-Fi/BLE-to-phone/
> LTE) reusing the FitPub upload endpoint and stored token; if offline or upload
> fails, queue locally and relay through the phone via Data Layer (phone performs
> the upload with its own session) — implement both paths with a single
> WorkManager-style retry queue on each side. Surface pending-sync count on the
> watch and in the phone app (e.g., banner on Timeline). End-to-end test:
> record on watch offline → phone comes online → activity appears in FitPub web."

**8f — Hardening & docs**
> "Battery profiling (target: >1h continuous GPS+HR recording), sensor accuracy
> validation against a reference device, round/chin-offset layout QA on multiple
> form factors, permission-denial and unpaired-phone flows, README section for
> the Wear app (pairing, sign-in, what's recorded), and CI build for :wear."

Notes:
- Reuses concepts and formats from Iteration 7 (state machine, GPX writer,
  upload plumbing) — implement 7 first; the watch module duplicates rather than
  shares the tracking engine initially because :wear can't depend on Androidx
  ViewModel/service classes compiled for phone-only APIs without care.
- Dependency decisions to make explicitly in 8a/8c: Data Layer (play-services-wear)
  requires Play Services on BOTH devices — acceptable default, but document a
  degoogle'd fallback (direct watch→instance HTTP sign-in via a short-lived
  pairing code shown on the phone) as a stretch goal.
- Privacy: heart-rate series are health data — note in README what leaves the
  device and that FitPub server handling follows the same privacy-zone rules as
  any uploaded track.


