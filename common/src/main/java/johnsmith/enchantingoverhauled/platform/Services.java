package johnsmith.enchantingoverhauled.platform;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

/**
 * Service loader orchestrator for the multi-loader architecture.
 * <p>
 * This class is responsible for locating and loading the platform-specific implementation
 * of the {@link IPlatformHelper} interface (e.g., FabricPlatformHelper or NeoForgePlatformHelper)
 * at runtime. This allows the common module to interact with loader-specific features
 * without depending directly on them.
 */
public class Services {

    /**
     * The singleton instance of the platform helper.
     * Loaded automatically when the class is initialized.
     */
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    /**
     * Loads a service implementation for the given interface.
     * <p>
     * It searches the {@code META-INF/services/} directory for a text file named matching
     * the fully qualified class name of the interface. That file should contain the
     * fully qualified name of the implementation class.
     *
     * @param clazz The interface class to load an implementation for.
     * @param <T>   The type of the service.
     * @return The loaded service implementation.
     * @throws NullPointerException If no service implementation is found.
     */
    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}