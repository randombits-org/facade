package org.randombits.facade;

import java.util.Map;

/**
 * An implementation of {@link FacadeCache} based on {@link java.util.WeakHashMap}.
 */
public class WeakHashMapCache implements FacadeCache {

    private Map<ClassLoader, Map<Object, Object>> loaderCache;

    public WeakHashMapCache() {}

    @SuppressWarnings( {"unchecked"} )
    public <T> T get( Object sourceObject, Class<T> targetType ) {
        if ( loaderCache != null ) {
            Map<Object, Object> facadeCache = loaderCache.get( targetType.getClassLoader() );
            if ( facadeCache != null )
                return ( T ) facadeCache.get( sourceObject );
        }
        return null;

    }

    public <T> void set( Object sourceObject, T facadeObject, Class<T> targetType ) {
        Map<Object, Object> facadeCache = null;
        if ( loaderCache != null ) {
            facadeCache = loaderCache.get( targetType.getClassLoader() );
        }

        if ( facadeCache == null ) {
            facadeCache = new java.util.WeakHashMap<Object, Object>();

            if ( loaderCache == null )
                loaderCache = new java.util.WeakHashMap<ClassLoader, Map<Object, Object>>();

            loaderCache.put( targetType.getClassLoader(), facadeCache );
        }

        facadeCache.put( sourceObject, facadeObject );
    }

    public void remove( Object sourceObject, Class<?> targetType ) {
        if ( loaderCache != null ) {
            Map<Object, Object> facadeCache = loaderCache.get( targetType.getClassLoader() );
            if ( facadeCache != null )
                facadeCache.remove( sourceObject );
        }
    }

    public void clear() {
        if ( loaderCache != null ) {
            for( Map<Object, Object> facadeCache : loaderCache.values() ) {
                facadeCache.clear();
            }

            loaderCache.clear();
        }
        loaderCache = null;
    }
}
