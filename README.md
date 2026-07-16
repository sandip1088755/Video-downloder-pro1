# Video Downloader Pro — Android (Kotlin + Jetpack Compose)

## ⚠️ Important scope note (read first)

Platforms like YouTube, Instagram, TikTok, Facebook, etc. **forbid third-party
downloading in their Terms of Service.** There is no technical or legal way
to "paste a URL and detect downloadable media" from those platforms without
scraping or reverse-engineering their private APIs, which would violate their
ToS regardless of stated intent — so that feature is intentionally **not**
implemented here.

What *is* implemented, fully and legitimately:
- Downloading **direct HTTP/HTTPS media links** (a `.mp4`/`.m4a`/`.mp3` URL,
  your own CDN, a self-hosted file server, or any link a server exposes with
  a proper `video/*` / `audio/*` Content-Type).
- Everything else you asked for (queueing, pause/resume/retry, multi-thread
  resume via HTTP Range, background downloads, player, file manager,
  settings, dark/light theme, etc.) is built on top of that, so if you later
  get access to an official platform SDK/API, you can plug it into
  `UrlValidator` / `DownloadRepository` without touching the rest of the app.

## Architecture

MVVM + Clean layering:

```
ui/            Jetpack Compose screens + ViewModels (Home, Downloads, Files, Player, Settings)
domain/        Pure Kotlin models (DownloadItem, DownloadStatus, VideoQuality, ...)
data/
  local/       Room database (DownloadEntity, DownloadDao, AppDatabase)
  repository/  DownloadRepository — single source of truth, bridges Room <-> WorkManager
  network/     UrlValidator — safe URL validation + HEAD probe
  datastore/   SettingsDataStore — Jetpack DataStore (Preferences) for all Settings
worker/        DownloadWorker — the actual download engine (OkHttp + Range resume)
di/            Hilt modules
util/          FileUtils, NotificationHelper
```

- **Dependency Injection:** Hilt end-to-end (`@HiltAndroidApp`, `@HiltViewModel`, `@HiltWorker`).
- **Persistence:** Room for download history/queue/favorites; DataStore for settings.
- **Background work:** WorkManager (`DownloadWorker`), so downloads survive
  process death and Doze; foreground notification keeps the OS from killing
  active transfers.
- **Resume:** HTTP `Range` header + `RandomAccessFile.seek()` — if a download
  is paused, cancelled by the OS, or the network drops, it resumes from the
  last byte written rather than restarting.
- **Player:** Media3 ExoPlayer, shared between the video and audio screens.
- **Theming:** Material 3, dynamic color (Material You) on Android 12+, with
  manual System/Light/Dark override in Settings.

## What's fully built vs. what's a documented extension point

| Feature | Status |
|---|---|
| Paste direct URL → validate → detect media | ✅ Full (`UrlValidator`) |
| Download engine: multi-thread resume, pause/resume/retry/cancel | ✅ Full (`DownloadWorker`, `DownloadRepository`) |
| Background downloads + notifications | ✅ Full |
| Speed / ETA / progress | ✅ Full |
| Download queue, history, favorites, search | ✅ Full (Room-backed) |
| Video + audio player | ✅ Full (ExoPlayer) |
| File manager: browse, rename, delete, move, share intent | ✅ Full (`FileUtils`, `FilesScreen`) |
| Storage analyzer | ✅ Basic total-usage rollup — extend with a per-type breakdown chart if needed |
| Duplicate detection | ✅ Size + partial-hash fingerprint scan |
| Settings: quality, parallelism, Wi-Fi only, notifications, theme, language, auto-update | ✅ Full (DataStore-backed) |
| Choose storage location (Internal/SD card) | ⚙️ Wired via `storageLocation` setting; hook up Android's `ACTION_OPEN_DOCUMENT_TREE` (SAF) picker in Settings for full SD-card support |
| Built-in browser | ⚙️ Not included — would be a `WebView`-based screen with a "download this page's media" affordance; straightforward to add following the existing screen pattern |
| Batch downloads (multiple URLs at once) | ⚙️ `DownloadRepository.enqueue()` already supports being called N times; add a multi-line paste UI on Home to batch-submit |
| Auto file naming | ✅ Full (`UrlValidator.suggestFileName`) |
| Multi-language UI strings | ⚙️ Language *setting* persists; add `values-hi/strings.xml` and wrap remaining hardcoded UI strings as string resources for full i18n |

## Setup

1. Open the project root folder in Android Studio (Koala/2024.1+).
2. Let Gradle sync — everything needed is declared in `app/build.gradle.kts`
   (Compose BOM 2024.06, Hilt 2.51.1, Room 2.6.1, Media3 1.4.0, WorkManager 2.9.0).
3. Run on a device/emulator with API 24+.
4. Min SDK 24, target/compile SDK 34.

## Security & privacy notes

- `network_security_config.xml` disables cleartext traffic by default —
  downloads must be HTTPS in production.
- No analytics, ads, or third-party trackers are included anywhere in this codebase.
- `UrlValidator` rejects non-http(s) schemes and URLs with embedded credentials
  before they ever reach the download engine.
- Room database is excluded from Android auto-backup (`data_extraction_rules.xml`)
  since download history can reference user file paths.
- Runtime permissions requested: notifications (Android 13+) and legacy
  storage (API ≤ 28 only) — modern Android's scoped storage means no broad
  storage permission is needed for the app's own Downloads folder.

## Known TODOs before a Play Store release

- Add proper Room migrations before shipping v2 schema changes (currently `fallbackToDestructiveMigration()`).
- Add unit tests for `DownloadWorker`'s resume logic and `UrlValidator`.
- Wire a real Storage Access Framework picker for the "choose SD card" setting.
- Add `values-hi` (or your target languages) string resources.
- Generate real PNG/adaptive launcher icon assets (a placeholder vector icon is included).
