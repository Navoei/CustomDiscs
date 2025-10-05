# Custom Discs v5.1.2 for Paper and Folia 1.21.7 / 1.21.8
A Paper fork of henkelmax's Audio Player. Special thanks to Athar42 for maintaining this plugin. 
- Play custom music discs, goat horns and player heads using the Simple Voice Chat API. (The voice chat mod is required on the client and server.)
- Use ```/customdisc``` or ```/cd``` to view available commands.
- Music files should go into ```plugins/CustomDiscs/musicdata/```
- Music files must be in the ```.wav```, ```.flac```, or ```.mp3``` format.

Join the discord for support: https://discord.gg/YJpqruvZ97. You can still use the "[Issues](https://github.com/Navoei/CustomDiscs/issues)" section of GitHub.

Downloading Files:
- To download a file use the command ```/cd download <url> <filename.extension>```.
  - The link used to download a file must be a direct link (meaning the file must automatically begin downloading when accessing the link).
  - Files must have the correct extension specified.
  - An UnsupportedAudioFileException will be thrown in the server's console if the file extension is not correct (for example when giving a wav file the mp3 extension).
  - Below is an example of how to use the command and a link to get direct downloads from Google Drive.
    - Example: ```/cd download "https://example.com/mysong" mysong.mp3```
    - **To note** : Do not forget to use quotes (") for the URL. Without this, you'll get a command error.
- Direct Google Drive links: https://lonedev6.github.io/gddl/

Set the range of a disc:
- To set the active range of a playable disc, just use the command ```/cd range <range>```.
  - The range can be between 1 and the max value set in the config file (default : 256)
- Example: ```/cd range 100```

Permission Nodes (Required to run the commands. Playing discs does not require a permission.):
- ```customdiscs.create``` to create a disc
- ```customdiscs.download``` to download a file
- ```customdiscs.range``` to set the range of the disc
- ```customdiscs.horncooldown``` to set the cooldown (in ticks) for custom goat horns

Dependencies:
- This plugin depends on the latest version of ProtocolLib available for your Paper version and SimpleVoiceChatBukkit (latest is recommended - at least version 2.6.1 required). 


Versions support matrix :

| Minecraft version                          | Server type     | Compatible versions          | Latest compatible<br>version                                       |
|--------------------------------------------|-----------------|------------------------------|--------------------------------------------------------------------|
| **1.19**                                   | Paper           | 1.1.0 - 2.1                  | [2.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.1)     |
| **1.19.1, 1.19.2, 1.19.3**                 | Paper           | 2.2 - 2.2.3                  | [2.2.3](https://github.com/Navoei/CustomDiscs/releases/tag/v2.2.3) |
| **1.19.4**                                 | Paper           | 2.3 - 2.3.2                  | [2.3.2](https://github.com/Navoei/CustomDiscs/releases/tag/v2.3.2) |
| **1.20, 1.20.1**                           | Paper           | 2.4 - 2.4.1                  | [2.4.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.4.1) |
| **1.20.2**                                 | Paper           | 2.5 - 2.5.1                  | [2.5.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.5.1) |
| **1.20.3, 1.20.4, 1.20.5, 1.20.6**         | Paper           | 2.6 - 2.6.1                  | [2.6.1](https://github.com/Navoei/CustomDiscs/releases/tag/v2.6.1) |
| **1.21, 1.21.1**                           | Paper           | 3.0                          | [3.0](https://github.com/Navoei/CustomDiscs/releases/tag/v3.0)     |
| **1.21.2, 1.21.3**                         | Paper           | 4.1                          | [4.1](https://github.com/Navoei/CustomDiscs/releases/tag/v4.1)     |
| **1.21.4**                                 | Paper           | 4.2 - 4.4                    | [4.4](https://github.com/Navoei/CustomDiscs/releases/tag/v4.4)     |
| **1.21.5**                                 | Paper           | 4.4                          | [4.4](https://github.com/Navoei/CustomDiscs/releases/tag/v4.4)     |
| **1.21.6, 1.21.7-8**                       | Paper           | 4.5                          | [4.5](https://github.com/Navoei/CustomDiscs/releases/tag/v4.5)     |
| **1.21.7-9, 1.21.8**<br>**1.21.8**         | Paper<br>Folia  | 5.0 - 5.1.2<br>5.1.1 - 5.1.2 | [5.1.2](https://github.com/Navoei/CustomDiscs/releases/tag/v5.1.2) |
| **1.21.9, 1.21.10**<br>**1.21.9, 1.21.10** | Paper<br>Folia  | 5.1.2<br>5.1.2               | [5.1.2](https://github.com/Navoei/CustomDiscs/releases/tag/v5.1.2) |

https://user-images.githubusercontent.com/64107368/178426026-c454ac66-5133-4f3a-9af9-7f674e022423.mp4

Default Config.yml:
```
# [General CustomDiscs Config]

# The maximum download size in megabytes.
max-download-size: 50

# The maximum length the file name (including the file extension) should be when downloaded. Using a too high value could crash the server.
filename-maximum-length: 100

# The master volume of music discs from 0-1. (You can set values like 0.5 for 50% volume).
music-disc-volume: 1

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

# The default cooldown time for horns in ticks from 1 to the max value of horn-max-cooldown (1 second is 20 ticks).
horn-cooldown: 140

# The default max cooldown time for horns in ticks (1 second is 20 ticks).
horn-max-cooldown: 6000

# [Player Heads Config]

# Enable custom player heads.
custom-head-enable: true

# Enable "Now playing" message for player heads.
custom-head-playing-enable: true

# The distance from which music discs can be heard in blocks.
custom-head-distance: 16

# The max distance from which music discs can be heard in blocks.
custom-head-max-distance: 256
```

Default Lang.yml:
```
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
invalid-filename-length: '&cThe maximum file name is restricted to %filename_length_value% characters'
```
