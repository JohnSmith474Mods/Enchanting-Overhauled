package johnsmith.enchantingoverhauled.platform.services;

/**
 * Defines the contract for retrieving platform-specific runtime environment data.
 */
public interface PlatformHelper {

    /**
     * Gets the name of the current mod loader.
     *
     * @return "Fabric" or "NeoForge".
     */
    String getPlatformName();

    /**
     * Checks if another mod with the given ID is currently loaded.
     *
     * @param modId The mod ID to check.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Checks if the game is running in a development environment (IDE).
     *
     * @return True if in development, false if in production.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the environment name as a string for logging purposes.
     *
     * @return "development" or "production".
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}