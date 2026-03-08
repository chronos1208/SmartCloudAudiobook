# 📚 SmartCloud Audiobook

SmartCloud Audiobook is an advanced audiobook player for Android that can **stream and play content directly from Google Drive**, with a built-in **PDF viewer** for companion materials.

> In short: a high-function cloud audiobook experience with seamless playback, rich metadata, and practical reading support.

---

## 🚀 Project Overview

**SmartCloud Audiobook** is designed for users who manage audiobook files in Google Drive and want a polished mobile listening workflow:

- ☁️ Stream audiobooks without manually copying all files to local storage first.
- 📄 Open related PDF documents while listening.
- 🎧 Enjoy modern playback UX powered by Android Media3/ExoPlayer.

This project focuses on practical daily use: smooth playback, reliable account authentication, clean UI, and metadata-driven library presentation.

---

## ✨ Features

### 1) Google Drive integration (on-demand sync + streaming)
- Connect your Google account securely.
- Scan/sync audiobook content from Drive.
- Stream files on demand with cloud-first behavior.

### 2) Advanced playback with Media3 (ExoPlayer)
- Gapless-style sequential playback across multiple files.
- Background playback via media session/service architecture.
- Playback controls suitable for long-form audio.

### 3) iTunes API metadata enrichment
- Automatically fetch cover art and metadata.
- Present a visually rich library view.
- Improve discoverability and navigation of audiobook content.

---

## 🧰 Tech Stack

Core technologies used in this project include:

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Design System**: Material Design 3
- **Media Engine**: Media3 (ExoPlayer)
- **Local Storage**: Room
- **Dependency Injection**: Hilt
- **Authentication**: Credential Manager + Google Identity
- **Networking**: Retrofit (+ Moshi converter)
- **Image Loading**: Coil
- **Navigation**: Navigation Compose

---

## ⚠️ Important Setup / Prerequisites (MUST DO)

Before building the app, you **must complete all steps below**.
If you skip these, Google authentication and Drive integration will not work.

### ✅ Step 1: Create a Google Cloud project and enable Drive API
1. Open Google Cloud Console.
2. Create (or select) a project for this app.
3. Enable **Google Drive API** for that project.

### ✅ Step 2: Create OAuth 2.0 Client ID as **Web application**
1. Go to OAuth credentials.
2. Create a new OAuth 2.0 Client ID.
3. Select application type: **Web application**.

> ❗ Important: **Do not use “Android” client type** for this field in this project setup.

### ✅ Step 3: Add the client ID to `strings.xml`
Paste your client ID into:

`app/src/main/res/values/strings.xml`

```xml
<string name="google_web_client_id">YOUR_CLIENT_ID</string>
```

---

## 🛠 Getting Started / How to Run

### Environment requirements
- Latest **Android Studio** (recommended stable release)
- **JDK 17**
- Android SDK compatible with this project (compile/target SDK 34)

### 1) Clone the repository
```bash
git clone <your-repo-url>
cd SmartCloudAudiobook
```

### 2) Configure Google OAuth client ID
- Complete the prerequisite section above.
- Update `google_web_client_id` in `app/src/main/res/values/strings.xml`.

### 3) Open in Android Studio
- Open the project folder.
- Let Gradle sync complete.

### 4) Build the app
You can build via Android Studio UI, or with CLI:

```bash
./gradlew assembleDebug
```

### 5) Run the app

#### Option A: Emulator (recommended for auth testing)
Use an emulator image with the **Google Play** badge (Play Store enabled).

Why this matters:
- Google sign-in and related services behave more reliably with Play-enabled system images.

Suggested steps:
1. Android Studio → Device Manager.
2. Create Virtual Device.
3. Choose a system image labeled **Google Play**.
4. Start emulator and run the app (`app` configuration).

#### Option B: Physical Android device
1. On your device, enable **Developer options**:
   - Open Settings → About phone.
   - Tap Build number multiple times until developer mode is enabled.
2. Enable **USB debugging**:
   - Settings → Developer options → USB debugging.
3. Connect device to your PC via USB.
4. Accept RSA/debugging prompt on the device when prompted.
5. In Android Studio, select your device and click **Run**.

---

## 📦 Useful Gradle Commands

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew installDebug         # Install on connected device/emulator
./gradlew test                 # Run unit tests
./gradlew connectedAndroidTest # Run instrumentation tests (device required)
```

---

## 🧪 Troubleshooting Tips

- **Sign-in fails immediately**
  - Verify you used a **Web application** OAuth client ID.
  - Re-check `google_web_client_id` in `strings.xml`.

- **No Google account selection / auth issues on emulator**
  - Confirm emulator image is **Google Play enabled**.

- **Drive content not loading**
  - Confirm Drive API is enabled in the same Google Cloud project tied to your OAuth client.

- **Gradle/JDK errors**
  - Verify Android Studio uses **JDK 17**.

---

## 🤝 Contributing

Contributions are welcome! If you plan significant changes, please open an issue first with:
- problem statement,
- proposal,
- expected UX/technical impact.

---

## 📄 License

Add your preferred license information here (e.g., MIT, Apache-2.0).


## 🆕 Phase 6 Implementation Notes

- ExoPlayer now streams tracks from Google Drive `files/{fileId}?alt=media` with OAuth Bearer token headers.
- Player screen opens per-audiobook playlists loaded from Room `AudioTrackEntity` records.
- Added an in-app PDF viewer screen that downloads Drive PDFs to cache and renders pages via `PdfRenderer` + Compose `LazyColumn`.
- Added navigation route parameters for `Player(audiobookId)` and `PdfViewer(pdfFileId)`.
