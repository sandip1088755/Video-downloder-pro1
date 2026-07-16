# Video Downloader Pro

A production-ready Android app (Kotlin + Jetpack Compose + MVVM) for downloading files from
**direct HTTPS links** — with pause/resume, queueing, retry, speed/ETA, a built-in player,
file manager, history/search, Material You theming, and more.

## Important scope note

This app downloads only from the exact direct URL you give it (e.g. `https://cdn.example.com/video.mp4`).
It does **not** scrape or bypass protections on platforms like YouTube/Instagram/Facebook — doing so
would violate their Terms of Service and can enable copyright infringement. Only download files you
own or have the right to download.

## Tech stack
Kotlin · Jetpack Compose · Material 3 (+ dynamic color) · MVVM · Hilt · Room · WorkManager · OkHttp/Retrofit · Media3/ExoPlayer · DataStore

## Building — no Android Studio required

Since this is meant to be built from a phone, the easiest path is **GitHub Actions**:

1. Create a new **private** GitHub repo and push this entire folder to it (GitHub's mobile
   app, or Working Copy / Termux + git, can do this from a phone).
2. That's it for an unsigned/debug-keyed build — the included workflow at
   `.github/workflows/release.yml` runs automatically on every push to `main` and also via
   **Actions tab → Build Release APK & AAB → Run workflow**.
3. Download the built `.apk`/`.aab` from the workflow run's **Artifacts** section.

### Getting a real Play Store signing key (optional but recommended)

Without a keystore, the release build falls back to the debug key (installs fine, but Play
Store requires your own key for publishing). To add one:

1. On any machine with Java installed (a free GitHub Codespace works from a phone browser),
   run:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias video_downloader_pro \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Base64-encode it: `base64 -w0 release.keystore > release.keystore.b64`
3. In your GitHub repo: **Settings → Secrets and variables → Actions → New repository secret**,
   add:
   - `KEYSTORE_BASE64` — paste the contents of `release.keystore.b64`
   - `KEYSTORE_PASSWORD`, `KEY_ALIAS` (`video_downloader_pro`), `KEY_PASSWORD`
4. Re-run the workflow — the APK/AAB will now be signed with your real key.

### Building locally instead (if you ever get access to a computer)
```bash
./gradlew assembleRelease   # -> app/build/outputs/apk/release/
./gradlew bundleRelease     # -> app/build/outputs/bundle/release/
```
`gradlew`/`gradlew.bat` are included, but `gradle/wrapper/gradle-wrapper.jar` (a binary file)
is intentionally **not** pre-committed since it can't be produced from a text-only tool. Run
this once before the first local build (or let the CI workflow do it automatically, as configured):
```bash
gradle wrapper --gradle-version 8.9
```

## Known limitation — audio extraction

"Extract audio" produces a `.m4a` (AAC) file by directly copying the audio track out of the
video container (via `MediaExtractor`/`MediaMuxer`) — fast, no quality loss, and needs no
external codec library. It does not re-encode to literal `.mp3`; bundling a general-purpose
MP3 encoder would add a large native dependency. If you specifically need `.mp3` output later,
that's a follow-up (e.g. via a licensed encoder library) rather than a one-line change.

## Project structure
```
app/src/main/java/com/sandipdigital/videodownloaderpro/
  data/        Room entities/DAO, repository, models
  di/          Hilt modules
  ui/          Compose screens, navigation, theme, shared components
  util/        URL validation, file utils, notifications, prefs, audio extraction
  worker/      WorkManager download engine
```
