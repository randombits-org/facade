package org.randombits.facade;

/**
 * Provides caching services for the {@link org.randombits.facade.FacadeAssistant}.
 */
public interface FacadeCache {
    /**
     * Returns the facade instance of the specified source object, for the specified target type.
     *
     * @param sourceObject The original sourceObject.
     * @param type The type to retrieve it as.
     * @param <T> The facade type.
     * @return The facade for the sourceObject, or <code>null</code>.
     */
    <T> T get( Object sourceObject, Class<T> type );

    /**
     * Sets the specified facade for the source object, mapped to the provided target type.
     * @param sourceObject The source object.
     * @param facade The facade.
     * @param targetType The target type.
     * @param <T> The target type.
     */
    <T> void set( Object sourceObject, T facade, Class<T> targetType );

    /**
     * Removes any cached facades for the specified object, facaded as the specified target type.
     *
     * @param sourceObject The source object.
     * @param targetType The target type.
     */
    void remove( Object sourceObject, Class<?> targetType );

    /**
     * Clears the cache.
     */
    void clear();
}
