# Custom Discs v3.1 for Paper 1.21

A Paper fork of henkelmax's Audio Player.
- Play custom music discs using the Simple Voice Chat API. (The voice chat mod is required on the client and server.)
- Use ```/customdisc``` or ```/cd``` to view available commands.
- Music files should go into ```plugins/CustomDiscs/musicdata/```
- Music files must be in the ```.wav```, ```.flac```, or ```.mp3``` format.

Downloading Files:
- To download a file use the command ```/cd download <url> <filename.extension>```. The link used to download a file must be a direct link (meaning the file must automatically begin downloading when accessing the link). Files must have the correct extension specified. An UnsupportedAudioFileException will be thrown in the server's console if the file extension is not correct (for example when giving a wav file the mp3 extension). Below is an example of how to use the command and a link to get direct downloads from Google Drive.
- Example: ```/cd download https://example.com/mysong mysong.mp3```
- Direct Google Drive links: https://lonedev6.github.io/gddl/

Set the range of a disc:
- To set the active range of a playable disc, just use the command ```/cd range <range>``` where <range> is between 1 and the max value set in the config file (default : 256)
- Example: ```/cd range 100```

Permission Nodes (Required to run the commands. Playing discs does not require a permission.):
- ```customdiscs.create``` to create a disc
- ```customdiscs.download``` to download a file
- ```customdiscs.range``` to set the range of the disc

Dependencies:
- This plugin depends on the latest version of ProtocolLib for 1.21 and SimpleVoiceChatBukkit version 2.5.16. 


https://user-images.githubusercontent.com/64107368/178426026-c454ac66-5133-4f3a-9af9-7f674e022423.mp4

Default Config.yml:
```
# [Music Disc Config]

# The distance from which music discs can be heard in blocks.
music-disc-distance: 16

# The max distance from which music discs can be heard in blocks.
music-disc-max-distance: 256

# The master volume of music discs from 0-1. (You can set values like 0.5 for 50% volume).
music-disc-volume: 1

#The maximum download size in megabytes.
max-download-size: 50

#Custom Discs Help Page
help:
  - "&8-[&6CustomDiscs Help Page&8]-"
  - "&aAuthor&7: &6Navoei"
  - "&aContributors&7: &6alfw / &6Athar42"
  - "&fGit&0Hub&7: &9&ohttps://github.com/Navoei/CustomDiscs"
```

Default Lang.yml:
```
prefix: "&8[&6CustomDiscs&8]&r"
no-permission: "&cYou do not have permission to execute this command."
invalid-filename: "&cThis is an invalid filename!"
no-disc-name-provided: "&cYou must provide a name for your disc."
invalid-format: "&cFile must be in wav, flac, or mp3 format!"
file-not-found: "&cFile not found!"
invalid-arguments: "&cInvalid arguments. &7(&a%command_syntax%&7)"
not-holding-disc: "&cYou must hold a disc in your main hand."
create-filename: "&7Your filename is: &a\"%filename%\"."
create-custom-name: "&7Your custom name is: &a\"%custom_name%\"."
downloading-file: "&7Downloading file..."
file-too-large: "&cThe file is larger than %max_download_size%MB."
successful-download: "&aFile successfully downloaded to &7%file_path%&a."
create-disc: "&aCreate a disc by doing &7/cd create %filename% \"Custom Lore\"&a."
download-error: "&cAn error has occurred while downloading."
now-playing: "&6Now playing: %song_name%"
disc-converted: "&aConverted disc to new format! &fThis is due to changes in newer Minecraft versions which introduced &7JukeboxPlayableComponent&f."
invalid-range: "&cYou need to chose a range between 1 and %range_value%"
create-custom-range: "&7Your range is set to: &a\"%custom_range%\"."
```


