package me.Navoei.customdiscsplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerVersionChecker {
    private static final String REQUIRED_PAPER_VERSION = "1.21.7-9"; // Set the PaperMC required version
    private static final String REQUIRED_FOLIA_VERSION = "1.21.8-2"; // Set the Folia required version
    private final Logger pluginLogger;
    private final boolean debugModeResult = CustomDiscs.isDebugMode();

    public ServerVersionChecker(JavaPlugin plugin) {
        this.pluginLogger = plugin.getLogger();
    }

    public void checkVersion() {
        // Get the full server version message output
        String versionMessage = Bukkit.getVersionMessage();

        if (versionMessage == null) {
            pluginLogger.severe("Unable to detect the running server version. Is this a supported PaperMC release?");
            return;
        }

        // Extract server type and version
        Matcher serverInfoExtracted = Pattern.compile("This server is running (\\S+) version (\\S+)").matcher(versionMessage);

        if (serverInfoExtracted.find()) {
            String serverType = serverInfoExtracted.group(1); // Extract the server type (Should be "Paper", but can be forks like "Purpur", "Spigot", ...)
            String buildVersion = serverInfoExtracted.group(2); // Extract the full version info (For example : 1.21.7-9-main@5661fbb)

            if(debugModeResult) {
                pluginLogger.info("DEBUG - Detected Server Type: " + serverType);
                pluginLogger.info("DEBUG - Server Full Version: " + versionMessage);
            }

            // As we only officially support Paper, we look up for it specifically
            if ("paper".equalsIgnoreCase(serverType)) {
                String cleanVersion = cleanBuildVersion(buildVersion);
                if(debugModeResult) {
                    pluginLogger.info("DEBUG - Extracted Version: " + cleanVersion);
                }

                // We then perform a version comparison
                if (compareVersions(cleanVersion, "paper") < 0) {
                    pluginLogger.severe("This Paper server version is unsupported. Please update to at least Paper " + REQUIRED_PAPER_VERSION);
                } else {
                    pluginLogger.info("Paper server version is supported.");
                }
            } else if ("folia".equalsIgnoreCase(serverType)) {
                String cleanVersion = cleanBuildVersion(buildVersion);
                if(debugModeResult) {
                    pluginLogger.info("DEBUG - Extracted Version: " + cleanVersion);
                }

                // We then perform a version comparison
                if (compareVersions(cleanVersion, "folia") < 0) {
                    pluginLogger.severe("This Folia server version is unsupported. Please update to at least Folia " + REQUIRED_FOLIA_VERSION);
                } else {
                    pluginLogger.info("Folia server version is supported.");
                }
            } else {
                // For Paper forks servers (mostly), log a severe message about non-support
                pluginLogger.severe(serverType + " server detected. No support will be made in case of issues!");
            }
        } else {
            pluginLogger.severe("Unable to read the server version. Is this a supported PaperMC release?");
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

    private static int compareVersions(String runningVersion, String serverType) {
        // We first start by separating the main version number from the build number
        String[] currentVersion = runningVersion.split("-");
        String[] requiredVersion;
        if(serverType.equals("folia")) {
            requiredVersion = REQUIRED_FOLIA_VERSION.split("-");
        } else {
            requiredVersion = REQUIRED_PAPER_VERSION.split("-");
        }

        // Then we compare the base version (sub-function to handle it)
        // If we are in the same main version, we pass to the next check, else we exit (-1 = older release ; 1 = newer release)
        int result = compareVersionParts(currentVersion[0], requiredVersion[0]);
        if (result != 0) return result;

        // And finally, we compare the build number (only if we are at the same main base version, to ensure we get the minimal build)
        return Integer.compare(Integer.parseInt(currentVersion[1]), Integer.parseInt(requiredVersion[1]));
    }

    private static int compareVersionParts(String currentVersionPart, String requiredVersionPart) {
        // We split each numbers into individual components (major (1), minor (21), and patch versions (7), so we can compare it one by one)
        String[] currentVersionArray = currentVersionPart.split("\\.");
        String[] requiredVersionArray = requiredVersionPart.split("\\.");

        for (int i = 0; i < 3; i++) {
            int currentVersion = Integer.parseInt(currentVersionArray[i]);
            int requiredVersion = Integer.parseInt(requiredVersionArray[i]);

            if (currentVersion < requiredVersion) return -1;
            if (currentVersion > requiredVersion) return 1;
        }

        return 0;
    }

}