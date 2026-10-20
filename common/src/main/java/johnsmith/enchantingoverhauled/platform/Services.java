package johnsmith.enchantingoverhauled.platform;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.platform.services.NetworkHelper;
import johnsmith.enchantingoverhauled.platform.services.PlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
    public static final NetworkHelper NETWORK = load(NetworkHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}