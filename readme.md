# Custom Discs v6.0.1
### For Paper, Folia and Forks (Purpur, Leaf, ...)
### 1.21.7 to 26.1.2

[![GitHub Total Downloads](https://img.shields.io/github/downloads/Navoei/CustomDiscs/total?style=plastic&label=GitHub%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/customdiscs-plugin) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Modrinth Downloads](https://img.shields.io/modrinth/dt/customdiscs-plugin?style=plastic&label=Modrinth%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/customdiscs-plugin)

---

## ⚠️ Dependency notice — PacketEvents required

**Since version 5.2.0, [PacketEvents](https://modrinth.com/plugin/packetevents) (v2.11.2+) is required.**\
ProtocolLib is no longer used — it can be safely removed if no other plugin needs it.

## ⚠️ Java 25 required

**Starting with version 6.0.0, this plugin requires Java 25 or newer to run on your server.**

---

A Paper fork of henkelmax's Audio Player. Special thanks to Athar42 for maintaining this plugin.

> Any Paper forks should be supported. In case of issues, reach us on our [Discord server](https://discord.gg/rJtBRmRFCr).

Play custom music discs, goat horns and player heads using the [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) API.

> The Simple Voice Chat mod is required on both the client and the server.

- Music files go into `plugins/CustomDiscs/musicdata/`
- Subdirectories can be enabled in `config.yml` via the `subdirectory-depth` setting:
  - `none` (default): all files must be in the root of `musicdata/`
  - `single`: one level of subdirectory (e.g. `musicdata/rock/song.mp3`)
  - `unrestricted`: unlimited depth (e.g. `musicdata/rock/metal/song.mp3`)
- Supported formats: `.wav`, `.flac`, `.mp3`
- Use `/customdisc` or `/cd` to see available commands

Join our Discord for support: https://discord.gg/rJtBRmRFCr

---

## Dependencies

| Plugin | Required | Notes                              |
|--------|----------|------------------------------------|
| [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) | ✅ Required | v2.6.1 minimum                     |
| [PacketEvents](https://modrinth.com/plugin/packetevents) | ✅ Required | Since v5.2.0 — tested with v2.11.2 |
| [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) | ⛔ Up to v5.1.4 only | No longer required as of v5.2.0    |

---

## Downloading Files

Use the command `/cd download <url> <filename.extension>` to download an audio file directly to the server.

> **Always write the URL between double quotes.**\
> Without this, you'll get a command error.

### Direct URL

Any URL that starts an immediate file download when opened in a browser.

```
/cd download "https://example.com/mysong.mp3" mysong.mp3
```

- The file extension in `<filename.extension>` must match the actual format of the file. Providing a wrong extension (e.g. naming a `.wav` file as `.mp3`) will cause an `UnsupportedAudioFileException` in the server console.
- **Google Drive** — Convert your sharing link to a direct download link: https://lonedev6.github.io/gddl/

### Filebin

[Filebin](https://filebin.net) is a free, no-registration file hosting service. Two URL formats are supported (final `/` is optional, `www.` prefix is accepted in both cases):

**Bin URL** — `https://filebin.net/<bin>`\
The plugin queries the Filebin API and automatically downloads the first (and only the first) supported audio file found in the bin.

```
/cd download "https://filebin.net/mybinname" mysong.mp3
```

**Direct Bin file URL** — `https://filebin.net/<bin>/<filename>`\
Downloads a specific file from the bin.

```
/cd download "https://filebin.net/mybinname/mysong.mp3" mysong.mp3
```

> In all cases, if a file with the requested name already exists on the server, a unique name is automatically assigned (e.g. `mysong_1.mp3`) and you are notified in chat.

---

## Permissions

| Permission | Description                                                          |
|------------|----------------------------------------------------------------------|
| `customdiscs.*` | Grants all CustomDiscs permissions                                   |
| `customdiscs.create` | Create a custom disc, goat horn or player head                       |
| `customdiscs.download` | Download a file from a URL                                           |
| `customdiscs.range` | Set the audio range of a disc                                        |
| `customdiscs.horncooldown` | Set the cooldown for custom goat horns                               |
| `customdiscs.revert` | Revert a custom item back to its original vanilla state              |
| `customdiscs.reload` | Reload the plugin configuration and language files                   |
| `customdiscs.update` | Receive an update notification on join if a new version is available |

> Playing discs does not require any permission.

---

## Setting the range

Use `/cd range <value>` while holding a custom disc, goat horn, or player head to set its hearing range.\
The range must be between 1 and the maximum value set in `config.yml` for the respective item type (default: 256).

```
/cd range 100
```

---

## Reverting a custom item

Use `/cd revert` while holding a custom disc, goat horn, or player head to revert it back to its original vanilla state.\
All custom data (sound file, lore, range, cooldown) is then removed.

```
/cd revert
```

---

## Languages

Language files are located in `plugins/CustomDiscs/langs/`.\
Each player receives the messages in their own **client language** automatically — no configuration needed per player!

### Available languages

The following languages are included by default:

| Code | Language       | Code | Language    |
|------|----------------|------|-------------|
| `en` | English        | `nl` | Dutch       |
| `fr` | French         | `it` | Italian     |
| `de` | German         | `tr` | Turkish     |
| `ru` | Russian        | `cs` | Czech       |
| `es` | Spanish        | `hu` | Hungarian   |
| `pt` | Portuguese (BR/PT) | `ko` | Korean  |
| `zh` | Chinese (Simplified/Traditional) | `tt` | Tatar |
| `pl` | Polish         |      |             |

> `pt` covers both `pt_BR` and `pt_PT`.\
> `zh` covers both `zh_CN` and `zh_TW`.

> **Found a translation error or just want your language added?**\
> Reach us on our [Discord](https://discord.gg/rJtBRmRFCr) or submit a [Pull Request on GitHub](https://github.com/Navoei/CustomDiscs/pulls) — your contributions are always welcome!

### Server default language

Set the fallback language used for console output and when a player's language has no matching file:

```yaml
# config.yml
default-lang: en
```

### Adding a custom language

Create a new file in `plugins/CustomDiscs/langs/` named after the [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes) two-letter language code "Set 1" (e.g. `ja.yml` for Japanese).\
Copy the contents of `en.yml` and translate the values.\
Use `/cd reload` to load it without restarting the whole server.

### Notice on updates behaviour

When the plugin is updated with new messages:
- **Bundled language files** (`en`, `fr`, `de`, …): new messages are automatically added to your existing files using the plugin original translation.\
Keys that you have already customised are **never** overwritten.
- **Custom language files** (made by you): new messages are added using the default English language.\
So keep an eye in your own lang if you don't wan't to have a mix with the default values.

---

## Reloading the configuration

Use `/cd reload` to reload `config.yml` and all language files in `langs/` without restarting the server.

```
/cd reload
```

---

## Configuration

**config.yml**

```yaml
# [General CustomDiscs Config]

# The maximum download size in megabytes.
max-download-size: 50

# The maximum length the file name (including the file extension) should be when downloaded. Using a too high value could crash the server.
filename-maximum-length: 100

# The master volume of music discs from 0-1. (You can set values like 0.5 for 50% volume).
music-disc-volume: 1

# Subdirectory depth allowed in musicdata folder. Possible values are :
# none: all files must be in the root of musicdata (default)
# single: one level of subdirectory allowed (e.g., musicdata/rock/song.mp3)
# unrestricted: unlimited subdirectory depth (e.g., musicdata/rock/metal/song.mp3)
subdirectory-depth: none

# Default language for player messages.
# Must match a file name in the langs/ folder (e.g. "en", "fr", "de", "ru").
# Players whose client language has no matching file will fall back to this selected language.
default-lang: en

# Debug Mode - To display some more logging information and Stack Trace informations
debugMode: false

# [Music Discs Config]

# Enable custom music discs.
music-disc-enable: true

# Enable "Now playing" message for custom music discs.
music-disc-playing-enable: true

# The distance from which music discs can be heard in blocks.
music-disc-distance: 16

# The max distance from which music discs can be heard in blocks.
music-disc-max-distance: 256

# [Goat Horns Config]

# Enable custom goat horns.
custom-horn-enable: true

# Enable "Now playing" message for custom horns.
custom-horn-playing-enable: true

# The distance from which custom horns can be heard in blocks.
custom-horn-distance: 16

# The max distance from which custom horns can be heard in blocks.
custom-horn-max-distance: 256

# The default instrument to restore when reverting a custom goat horn.
# Used only if the original instrument could not be saved at creation time.
# Valid values: ponder_goat_horn, sing_goat_horn, seek_goat_horn, feel_goat_horn,
#               admire_goat_horn, call_goat_horn, yearn_goat_horn, dream_goat_horn
default-horn-instrument: ponder_goat_horn

# The default cooldown time for horns in ticks from 1 to the max value of horn-max-cooldown (1 second is 20 ticks).
horn-cooldown: 140

# The default max cooldown time for horns in ticks (1 second is 20 ticks).
horn-max-cooldown: 6000

# [Player Heads Config]

# Enable custom player heads.
custom-head-enable: true

# Enable "Now playing" message for player heads.
custom-head-playing-enable: true

# The distance from which player heads can be heard in blocks.
custom-head-distance: 16

# The max distance from which player heads can be heard in blocks.
custom-head-max-distance: 256

# [Update Checker Config]

# Enable automatic update checks against Modrinth.
# Operators or players with permission "customdiscs.update" are notified on join when an update is available.
# An initial check is performed on startup, then every 24 hours as long as the server is running.
update-checker:
  enabled: true
  # Release channel to check against: release, beta
  channel: release

# Do not modify this value — it is used for automatic config migration.
config-version: 2
```

---

## Version Support Matrix

| Minecraft version                  | Paper & Forks (Purpur, Leaf, ...)                                             | Folia & Forks                                                                                     |
|------------------------------------|-------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **1.19**                           | 1.1.0 – [2.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.1)        | —                                                                                                 |
| **1.19.1, 1.19.2, 1.19.3**         | 2.2 – [2.2.3](https://github.com/Navoei/CustomDiscs/releases/tag/v2.2.3)      | —                                                                                                 |
| **1.19.4**                         | 2.3 – [2.3.2](https://github.com/Navoei/CustomDiscs/releases/tag/v2.3.2)      | —                                                                                                 |
| **1.20, 1.20.1**                   | 2.4 – [2.4.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.4.1)      | —                                                                                                 |
| **1.20.2**                         | 2.5 – [2.5.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.5.1)      | —                                                                                                 |
| **1.20.3, 1.20.4, 1.20.5, 1.20.6** | 2.6 – [2.6.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.6.1)      | —                                                                                                 |
| **1.21, 1.21.1**                   | [3.0](https://github.com/Navoei/CustomDiscs/releases/tag/v3.0)                | —                                                                                                 |
| **1.21.2, 1.21.3**                 | [4.1](https://github.com/Navoei/CustomDiscs/releases/tag/v4.1)                | —                                                                                                 |
| **1.21.4**                         | 4.2 – [4.4](https://github.com/Navoei/CustomDiscs/releases/tag/v4.4)          | —                                                                                                 |
| **1.21.5**                         | [4.4](https://github.com/Navoei/CustomDiscs/releases/tag/v4.4)                | —                                                                                                 |
| **1.21.6, 1.21.7-8**               | [4.5](https://github.com/Navoei/CustomDiscs/releases/tag/v4.5)                | —                                                                                                 |
| **1.21.7-9, 1.21.8**               | 5.0 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1)   | (only since 1.21.8) 5.1.1 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1) |
| **1.21.9, 1.21.10**                | 5.1.2 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1) | 5.1.2 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1)                     |
| **1.21.11**                        | 5.1.3 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1) | 5.1.3 – [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1)                     |
| **26.1, 26.1.1, 26.1.2**           | [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1)         | [6.0.1](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.1)                             |

---

**Demo video:**

https://user-images.githubusercontent.com/64107368/178426026-c454ac66-5133-4f3a-9af9-7f674e022423.mp4