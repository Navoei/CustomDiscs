# Custom Discs v6.0.0
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

## Reloading the configuration

Use `/cd reload` to reload `config.yml` and `lang.yml` without restarting the server.

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
config-version: 1
```

---

## Language

**lang.yml**

```yaml
prefix: '&8[&6CustomDiscs&8]&r'
invalid-filename: '&cThis is an invalid filename!'
invalid-format: '&cFile must be in wav, flac, or mp3 format!'
file-not-found: '&cFile not found!'
not-holding-correct-item: '&cYou must either hold a disc, goat horn or player head in your main hand.'
create-filename: '&7Your filename is: &a"%filename%".'
create-custom-name: '&7Your custom name is: &a"%custom_name%".'
downloading-file: '&7Downloading file...'
file-too-large: '&cThe file is larger than %max_download_size%MB.'
successful-download: '&aFile successfully downloaded to &7%file_path%&a.'
create-disc: '&aCreate a disc by doing &7/cd create %filename% "Custom Lore"&a.'
download-error: '&cAn error has occurred while downloading.'
now-playing: '&6Now playing: %song_name%'
disc-converted: '&aConverted disc to new format! &fThis is due to changes in newer Minecraft versions which introduced &7ToolTipDisplay&f.'
invalid-range: '&cYou need to chose a range between 1 and %range_value%'
create-custom-range: '&7Your range is set to: &a"%custom_range%".'
not-holding-custom-goathorn: '&cYou must hold a custom goat horn in your main hand.'
invalid-cooldown: '&cYou need to chose a cooldown between 1 and %cooldown_value% (in ticks).'
create-custom-goat-cooldown: '&7Your goat horn cooldown is set to: &a"%custom_goat_cooldown%" ticks.'
custom-music-disabled: '&7Custom music discs are disabled in the configuration.'
custom-head-disabled: '&7Custom player heads are disabled in the configuration.'
custom-horn-disabled: '&7Custom goat horns are disabled in the configuration.'
invalid-protocol: '&cOnly HTTP:// and HTTPS:// URL are allowed.'
invalid-filename-length: '&cThe maximum file name is restricted to %filename_length_value% characters.'
filebin-no-audio: '&cNo supported audio file (wav/mp3/flac) found in this Filebin bin.'
filebin-api-error: '&cFailed to access Filebin API. The bin may not exist or is unavailable.'
file-already-exists: '&eA file named &7%filename%&e already exists, saving as &7%new_filename%&e instead.'
subdirectory-not-allowed: '&cSubdirectories are not allowed in musicdata.'
subdirectory-depth-exceeded: '&cOnly one level of subdirectory is allowed in musicdata.'
revert-success: '&aItem successfully reverted.'
revert-not-custom: '&cThe item in your hand is not a custom disc, horn or head.'
reload-success: '&aConfiguration reloaded successfully.'
update-available: '&eA new version of CustomDiscs is available: &6%latest_version% &7(current: %current_version%)'
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
| **1.21.7-9, 1.21.8**               | 5.0 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0)   | (only since 1.21.8) 5.1.1 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0) |
| **1.21.9, 1.21.10**                | 5.1.2 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0) | 5.1.2 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0)                     |
| **1.21.11**                        | 5.1.3 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0) | 5.1.3 – [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0)                     |
| **26.1, 26.1.1, 26.1.2**           | [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0)         | [6.0.0](https://modrinth.com/plugin/customdiscs-plugin/version/6.0.0)                             |

---

**Demo video:**

https://user-images.githubusercontent.com/64107368/178426026-c454ac66-5133-4f3a-9af9-7f674e022423.mp4