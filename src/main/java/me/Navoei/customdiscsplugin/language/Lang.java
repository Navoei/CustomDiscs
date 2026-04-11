package me.Navoei.customdiscsplugin.language;

import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    PREFIX("prefix", "&8[&6CustomDiscs&8]&r"),
    INVALID_FILENAME("invalid-filename", "&cThis is an invalid filename!"),
    INVALID_FORMAT("invalid-format", "&cFile must be in wav, flac, or mp3 format!"),
    FILE_NOT_FOUND("file-not-found", "&cFile not found!"),
    NOT_HOLDING_CORRECT_ITEM("not-holding-correct-item", "&cYou must either hold a disc, goat horn or player head in your main hand."),
    CREATE_FILENAME("create-filename", "&7Your filename is: &a\"%filename%\"."),
    CREATE_CUSTOM_NAME("create-custom-name", "&7Your custom name is: &a\"%custom_name%\"."),
    DOWNLOADING_FILE("downloading-file", "&7Downloading file..."),
    FILE_TOO_LARGE("file-too-large", "&cThe file is larger than %max_download_size%MB."),
    SUCCESSFUL_DOWNLOAD("successful-download", "&aFile successfully downloaded to &7%file_path%&a."),
    CREATE_DISC("create-disc", "&aCreate a disc by doing &7/cd create %filename% \"Custom Lore\"&a."),
    DOWNLOAD_ERROR("download-error", "&cAn error has occurred while downloading."),
    DOWNLOAD_FILENAME_REQUIRED("download-filename-required", "&cYou must provide a filename for non-YouTube URLs."),
    YOUTUBE_DOWNLOAD_DISABLED("youtube-download-disabled", "&7YouTube download is disabled in the configuration."),
    YOUTUBE_TOOLS_MISSING("youtube-tools-missing", "&cYouTube download requires yt-dlp (%yt_dlp_path%) and ffmpeg (%ffmpeg_path%)."),
    YOUTUBE_TOOLS_INSTALLING("youtube-tools-installing", "&7Installing yt-dlp and ffmpeg... this may take a moment."),
    YOUTUBE_TOOLS_INSTALLED("youtube-tools-installed", "&aInstalled yt-dlp and ffmpeg: &7%yt_dlp_path% &a| &7%ffmpeg_path%"),
    YOUTUBE_TOOLS_INSTALL_FAILED("youtube-tools-install-failed", "&cFailed to auto-install yt-dlp/ffmpeg. Check console logs and network access."),
    YOUTUBE_TOOLS_UNSUPPORTED_PLATFORM("youtube-tools-unsupported-platform", "&cAuto-install is only supported on Linux/Windows x64. Please configure yt-dlp-path and ffmpeg-path manually."),
    NOW_PLAYING("now-playing","&6Now playing: %song_name%"),
    DISC_CONVERTED("disc-converted", "&aConverted disc to new format! &fThis is due to changes in newer Minecraft versions which introduced &7ToolTipDisplay&f."),
    INVALID_RANGE("invalid-range","&cYou need to chose a range between 1 and %range_value%"),
    CREATE_CUSTOM_RANGE("create-custom-range", "&7Your range is set to: &a\"%custom_range%\"."),
    NOT_HOLDING_CUSTOM_GOAT_HORN("not-holding-custom-goathorn", "&cYou must hold a custom goat horn in your main hand."),
    INVALID_COOLDOWN("invalid-cooldown","&cYou need to chose a cooldown between 1 and %cooldown_value% (in ticks)."),
    CREATE_CUSTOM_GOAT_COOLDOWN("create-custom-goat-cooldown", "&7Your goat horn cooldown is set to: &a\"%custom_goat_cooldown%\" ticks."),
    CUSTOM_MUSIC_DISABLED("custom-music-disabled", "&7Custom music discs are disabled in the configuration."),
    CUSTOM_HEAD_DISABLED("custom-head-disabled", "&7Custom player heads are disabled in the configuration."),
    CUSTOM_HORN_DISABLED("custom-horn-disabled", "&7Custom goat horns are disabled in the configuration."),
    INVALID_PROTOCOL("invalid-protocol", "&cOnly HTTP:// and HTTPS:// URL are allowed."),
    INVALID_FILENAME_LENGTH("invalid-filename-length", "&cThe maximum file name is restricted to %filename_length_value% characters."),
    FILEBIN_NO_AUDIO("filebin-no-audio", "&cNo supported audio file (wav/mp3/flac) found in this Filebin bin."),
    FILEBIN_API_ERROR("filebin-api-error", "&cFailed to access Filebin API. The bin may not exist or is unavailable."),
    FILE_ALREADY_EXISTS("file-already-exists", "&eA file named &7%filename%&e already exists, saving as &7%new_filename%&e instead."),
    SUBDIRECTORY_NOT_ALLOWED("subdirectory-not-allowed", "&cSubdirectories are not allowed in musicdata."),
    SUBDIRECTORY_DEPTH_EXCEEDED("subdirectory-depth-exceeded", "&cOnly one level of subdirectory is allowed in musicdata."),
    REVERT_SUCCESS("revert-success", "&aItem successfully reverted."),
    REVERT_NOT_CUSTOM("revert-not-custom", "&cThe item in your hand is not a custom disc, horn or head."),
    RELOAD_SUCCESS("reload-success", "&aConfiguration reloaded successfully."),
    UPDATE_AVAILABLE("update-available", "&eA new version of CustomDiscs is available: &6%latest_version% &7(current: %current_version%)");

    private final String path;
    private final String def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * @param path The string path.
     * @param start The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }

    @Override
    public String toString() {
        if (this == PREFIX)
            return LANG.getString(this.path, def) + " ";
        return LANG.getString(this.path, def);
    }

    /**
     * Get the default value of the path.
     * @return The default value of the path.
     */
    public String getDefault() {
        return this.def;
    }

    /**
     * Get the path to the string.
     * @return The path to the string.
     */
    public String getPath() {
        return this.path;
    }

}
