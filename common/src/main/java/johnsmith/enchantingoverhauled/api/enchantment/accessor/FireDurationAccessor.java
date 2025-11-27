package johnsmith.enchantingoverhauled.api.enchantment.accessor;

/**
 * Accessor interface for classes that can store and retrieve a custom fire duration value,
 * specifically entities that are set on fire by enchanted projectiles (like arrows with the Flame enchantment).
 * <p>
 * This interface is implemented via Mixin on the target entity class (e.g., {@code AbstractArrowMixin})
 * to allow the {@link johnsmith.enchantingoverhauled.api.enchantment.effect.SetFireDurationEffect}
 * to correctly configure the fire duration before the entity is ignited.
 */
public interface FireDurationAccessor {

    /**
     * Sets the custom duration (in seconds) for which the entity should burn.
     * <p>
     * A value greater than {@code -1} indicates that a custom duration should be used,
     * overriding the default vanilla fire duration.
     *
     * @param seconds The desired duration of fire in seconds.
     */
    void enchanting_overhauled$setFireDuration(float seconds);

    /**
     * Retrieves the custom fire duration previously set on the entity.
     *
     * @return The custom fire duration in seconds, or a negative value (typically {@code -1})
     * if no custom duration has been set.
     */
    float enchanting_overhauled$getFireDuration();
}