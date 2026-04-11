# Changelog

Date: 2026-04-11

Below is the full scope of work I completed in this branch.

## What I changed

### 1) YouTube integration in `/cd download`

I extended the download command and added a complete YouTube flow:

- Updated usage to `/cd download <url> [filename.extension]`.
- Added a dedicated YouTube branch using `yt-dlp + ffmpeg`, saving output as `mp3`.
- If `filename` is not provided, I use the video title and sanitize it into a safe file name.
- Kept the existing behavior for direct file URLs and Filebin.
- Added external process timeout handling and clearer failure handling.

### 2) Built-in tool installation

I implemented automatic binary installation directly inside the plugin:

- Added `YouTubeToolManager`.
- If `yt-dlp`/`ffmpeg` are not found via config paths, the plugin downloads them into `plugins/CustomDiscs/tools/`.
- Auto-install support: Linux x64 and Windows x64.
- For unsupported platforms, manual configuration remains available through `yt-dlp-path` / `ffmpeg-path`.

### 3) Config, localization, and documentation

I added new config options:

- `youtube-download-enable`
- `youtube-auto-install-tools`
- `yt-dlp-path`
- `ffmpeg-path`
- `youtube-process-timeout-seconds`

I also added new language keys in `lang.yml`/`Lang.java` and updated `readme.md` for the new flow.

### 4) Purpur support and 1.21.11 update

I updated `ServerVersionChecker` and project docs:

- Added explicit Purpur support checks.
- Updated `plugin.yml` description to `Paper/Purpur/Folia`.
- Finalized 1.21.11 support messaging and updated README text.

### 5) Build updates

To unpack `ffmpeg` archives, I added:

- `org.apache.commons:commons-compress`
- `org.tukaani:xz`

Build verification passed on Java 21:

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build` — successful.

## Comparison with prev commits

I compared my branch against upstream:

- Prev current `origin/main` (`19c465b`) only includes a `HopperManager` performance optimization (`onChunkLoad`).
- Prev `1.21.11` support commit (`a2da4e4`) updated `gradle.properties`, `CustomDiscs.java`, and `readme.md`, but did not add full Purpur support in the way this branch does.

Summary: this branch adds a new functional block (YouTube + auto-install of tools) and separately expands compatibility/communication for Purpur and 1.21.11.
