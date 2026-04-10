# Bujo — A Game of Movies

Guess the film from four snapshots and an iconic dialogue.

v0.1 is an Android-only, fully-local MVP. Three curated questions, one dialogue hint per question, zero backend, zero analytics. Made by Amit.

## Repository layout

```
.
├── android/                    # Android Studio project (Kotlin + Jetpack Compose)
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradle/wrapper/         # wrapper properties (jar added on first sync)
│   └── app/
│       ├── build.gradle.kts
│       ├── proguard-rules.pro
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── java/com/bujo/movies/
│           │   ├── MainActivity.kt
│           │   ├── data/       # ContentLoader, ProfileStore, AudioPlayer
│           │   ├── nav/        # BujoApp (NavHost + routes)
│           │   ├── ui/theme/   # Bujo brand palette
│           │   └── ui/screens/ # Splash, Username, Question, Success, …
│           ├── assets/
│           │   ├── bundled_content.json   # copied from /content/
│           │   ├── images/                # snapshots + poster (PLACEHOLDERS)
│           │   └── lottie/success.json    # success animation
│           └── res/            # strings, colors, themes, launcher icons
├── brand/                      # icon, feature graphic, splash background (SVG + PNG)
├── content/                    # bundled_content.json + source download manifest
├── docs/                       # PRD + Brand & Content Pack
└── README.md
```

## Build

1. Open the `android/` folder in Android Studio (Giraffe or newer).
2. Let Gradle sync. Android Studio will create `android/local.properties` pointing at your SDK and download the Gradle wrapper jar on first run.
3. Run on an emulator (Pixel class, API 34 recommended).

The app builds offline. No API keys, no backend, no content fetched at runtime.

## Content status

The three questions in `content/bundled_content.json` are wired up end-to-end, but the snapshots and audio clips are **placeholders** for v0.1 until the real media is downloaded from the sources in `content/content_sources.csv`. To replace the placeholders:

1. Download the 12 snapshots and 1 poster from the Flickr gallery URLs in the CSV. Use the **large** version of each photo.
2. Download the 3 dialogue `.m4a` files from the Dropbox URLs (change `?dl=0` to `?dl=1` for direct download).
3. Compress the JPGs to WebP (e.g. `cwebp input.jpg -o q1_snap1.webp`).
4. Replace the files in `android/app/src/main/assets/images/` using the exact filenames in `content_sources.csv`.
5. Put the `.m4a` files in `android/app/src/main/assets/audio/` keeping the filenames `q1_dialogue.m4a`, `q2_dialogue.m4a`, `q3_dialogue.m4a`.
6. Rebuild.

## Scope

v0.1 is deliberately minimal. See `docs/Bujo PRD v1.0.docx` for the full spec and `docs/Bujo Brand and Content Pack.docx` for brand, animation, and legal references. Section 11 of the PRD lists everything that was cut from v0.1 and is on the roadmap for v1.0.
