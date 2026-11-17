package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

/**
 * The base abstract class for all custom entries in the configuration GUI list.
 * <p>
 * This class extends the generic Minecraft {@link ContainerObjectSelectionList.Entry}
 * and specifies that it works with other custom {@code Entry} objects, providing
 * a common type boundary for all configuration list elements (headers, options).
 */
public abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {}