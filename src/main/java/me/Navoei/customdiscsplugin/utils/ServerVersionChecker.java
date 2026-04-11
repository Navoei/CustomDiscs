package me.Navoei.customdiscsplugin.utils;

import me.Navoei.customdiscsplugin.CustomDiscs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerVersionChecker {
    private static final String REQUIRED_PAPER_VERSION = "1.21.7-9"; // Set the PaperMC required version
    private static final String REQUIRED_FOLIA_VERSION = "1.21.8-2"; // Set the Folia required version
    private static final String REQUIRED_PURPUR_VERSION = "1.21.11"; // Set the Purpur required Minecraft version
    private final Logger pluginLogger;
    public static boolean paperAPIcheck;

    public ServerVersionChecker(JavaPlugin plugin) {
        this.pluginLogger = plugin.getLogger();
    }

    public void checkVersion() {
        String serverType = Bukkit.getName();
        String minecraftVersion = Bukkit.getMinecraftVersion();
        String versionMessage = Bukkit.getVersionMessage();
        String buildVersion = extractBuildVersion(versionMessage);

        paperAPIcheck = false;

        if (serverType == null || minecraftVersion == null) {
            pluginLogger.severe("Unable to detect the server version. Is this a supported Paper/Purpur/Folia release?");
            return;
        }

        if(CustomDiscs.isDebugMode()) {
            pluginLogger.info("DEBUG - Detected Server Type: " + serverType);
            pluginLogger.info("DEBUG - Minecraft Version: " + minecraftVersion);
            pluginLogger.info("DEBUG - Server Full Version: " + versionMessage);
            if (buildVersion != null) {
                pluginLogger.info("DEBUG - Build Version: " + buildVersion);
            }
        }

        // Check official server support and version floor.
        if ("paper".equalsIgnoreCase(serverType)) {
            if (isBelowRequiredVersion(minecraftVersion, buildVersion, REQUIRED_PAPER_VERSION)) {
                pluginLogger.severe("This Paper server version is unsupported. Please update to at least Paper " + REQUIRED_PAPER_VERSION);
            }

            paperAPIcheck = true;
        } else if ("folia".equalsIgnoreCase(serverType)) {
            if (isBelowRequiredVersion(minecraftVersion, buildVersion, REQUIRED_FOLIA_VERSION)) {
                pluginLogger.severe("This Folia server version is unsupported. Please update to at least Folia " + REQUIRED_FOLIA_VERSION);
            }

            paperAPIcheck = true;
        } else if ("purpur".equalsIgnoreCase(serverType)) {
            if (compareVersionParts(minecraftVersion, REQUIRED_PURPUR_VERSION) < 0) {
                pluginLogger.severe("This Purpur server version is unsupported. Please update to at least Purpur " + REQUIRED_PURPUR_VERSION);
            }

            paperAPIcheck = true;
        } else {
            // For unsupported server implementations, log a severe message.
            pluginLogger.severe(serverType + " server detected. No support will be provided!");
        }
    }

    /**
     * Cleans up the version string to remove the non usefull part after the build number (like '-main@5661fbb').
     * The result is only the main version part (like '1.21.7-9').
     */
    private static String cleanBuildVersion(String version) {
        String[] versionParts = version.split("-");
        return versionParts.length >= 2 ? versionParts[0] + "-" + versionParts[1] : version;
    }

    private static String extractBuildVersion(String versionMessage) {
        if (versionMessage == null) {
            return null;
        }

        Matcher serverInfoExtracted = Pattern.compile("This server is running \\S+ version (\\S+)").matcher(versionMessage);
        return serverInfoExtracted.find() ? serverInfoExtracted.group(1) : null;
    }

    /**
     * Extract the minecraft version from a full build version.
     * Example: "1.21.11-2454-ver/..." -> "1.21.11"
     */
    private static String cleanMinecraftVersion(String version) {
        int separatorIndex = version.indexOf('-');
        return separatorIndex == -1 ? version : version.substring(0, separatorIndex);
    }

    private static int compareBuildVersions(String runningVersion, String requiredVersionString) {
        // We first start by separating the main version number from the build number
        String[] currentVersion = runningVersion.split("-");
        String[] requiredVersion = requiredVersionString.split("-");

        // Then we compare the base version (sub-function to handle it)
        // If we are in the same main version, we pass to the next check, else we exit (-1 = older release ; 1 = newer release)
        int result = compareVersionParts(currentVersion[0], requiredVersion[0]);
        if (result != 0) return result;

        if (currentVersion.length < 2 || requiredVersion.length < 2) {
            return 0;
        }

        // And finally, we compare the build number (only if we are at the same main base version, to ensure we get the minimal build)
        return Integer.compare(Integer.parseInt(currentVersion[1]), Integer.parseInt(requiredVersion[1]));
    }

    private static boolean isBelowRequiredVersion(String minecraftVersion, String buildVersion, String requiredVersionString) {
        if (buildVersion != null) {
            String cleanVersion = cleanBuildVersion(buildVersion);
            return compareBuildVersions(cleanVersion, requiredVersionString) < 0;
        }

        String requiredMinecraftVersion = cleanMinecraftVersion(requiredVersionString);
        return compareVersionParts(minecraftVersion, requiredMinecraftVersion) < 0;
    }

    private static int compareVersionParts(String currentVersionPart, String requiredVersionPart) {
        // We split each numbers into individual components (major (1), minor (21), and patch versions (7), so we can compare it one by one)
        String[] currentVersionArray = currentVersionPart.split("\\.");
        String[] requiredVersionArray = requiredVersionPart.split("\\.");

        for (int i = 0; i < 3; i++) {
            int currentVersion = i < currentVersionArray.length ? Integer.parseInt(currentVersionArray[i]) : 0;
            int requiredVersion = i < requiredVersionArray.length ? Integer.parseInt(requiredVersionArray[i]) : 0;

            if (currentVersion < requiredVersion) return -1;
            if (currentVersion > requiredVersion) return 1;
        }

        return 0;
    }

    /**
     * Return if it's a Paper API based server (Paper, Purpur or Folia).
     *
     * @return The boolean value of isPaperAPI.
     */
    public static boolean isPaperAPI() { return paperAPIcheck; }

}
