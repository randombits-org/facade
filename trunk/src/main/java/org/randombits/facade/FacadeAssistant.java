/* 
 * Copyright (c) 2006-2008, randombits.org. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials provided with
 *    the distribution.
 *  * Neither the name of the randombits.org nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.randombits.facade;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

/**
 * This assistant class helps with transporting objects between classloaders.
 * See {@link #prepareObject(Object, ClassLoader)} for details.
 * 
 * @author David Peterson
 */
public class FacadeAssistant {

    class FacadeInfo {
        private boolean returnFacadable;

        private int arrayTypeParameter = -1;

        private boolean[] parameterFacadable;

        public FacadeInfo( Class<?> type, MethodSignature signature ) {
            parameterFacadable = new boolean[signature.getParameterCount()];

            Class<? extends Annotation> facadable = findAnnotationClass( Facadable.class, type );
            Class<? extends Annotation> arrayTypeParam = findAnnotationClass( ArrayTypeParameter.class, type );
            if ( facadable != null ) {
                checkMethod( type, signature, facadable, arrayTypeParam );
            }
        }

        public boolean isReturnFacadable() {
            return returnFacadable;
        }

        public int getArrayTypeParameter() {
            return arrayTypeParameter;
        }

        public boolean isParameterFacadable( int i ) {
            return parameterFacadable[i];
        }

        public int getParameterCount() {
            return parameterFacadable.length;
        }

        private boolean checkMethod( Class<?> type, MethodSignature signature,
                Class<? extends Annotation> facadable, Class<? extends Annotation> arrayTypeParam ) {
            boolean allFacadable = false;

            Method method = signature.findDeclaredMethod( type );

            if ( method != null ) {
                allFacadable = true;

                if ( arrayTypeParameter == -1 ) {
                    arrayTypeParameter = findArrayTypeParameter( method, arrayTypeParam );
                    allFacadable = arrayTypeParameter != -1;
                }

                if ( !returnFacadable ) {
                    returnFacadable = method.isAnnotationPresent( facadable );
                    allFacadable = allFacadable && returnFacadable;
                }

                Annotation[][] pa = method.getParameterAnnotations();
                for ( int i = 0; i < pa.length; i++ ) {
                    // Only process if it's not already facadable
                    if ( !parameterFacadable[i] ) {
                        Annotation[] a = pa[i];
                        int j;
                        for ( j = 0; j < a.length; j++ ) {
                            if ( facadable.equals( a[i] ) ) {
                                parameterFacadable[i] = true;
                                break;
                            }
                        }
                        allFacadable = allFacadable && j < a.length;
                    }
                }
            }

            if ( !allFacadable ) {
                // First, check all declared interfaces
                Class<?>[] interfaces = type.getInterfaces();
                if ( interfaces != null ) {
                    for ( int i = 0; !allFacadable && i < interfaces.length; i++ ) {
                        allFacadable = mergeInfo( findFacadeInfo( interfaces[i], signature ) );
                    }
                }

                // Second, check the superclass hierarchy if necessary
                if ( !allFacadable ) {
                    allFacadable = mergeInfo( findFacadeInfo( type.getSuperclass(), signature ) );
                }
            }

            return allFacadable;
        }

        private int findArrayTypeParameter( Method method, Class<? extends Annotation> arrayTypeParam ) {
            Annotation a = method.getAnnotation( arrayTypeParam );
            if ( a != null ) {
                try {
                    return ( Integer ) a.getClass().getMethod( "value" ).invoke( a );
                } catch ( IllegalArgumentException e ) {
//                    LOG.error( e );
                    e.printStackTrace();
                } catch ( SecurityException e ) {
//                    LOG.error( e );
                    e.printStackTrace();
                } catch ( IllegalAccessException e ) {
//                    LOG.error( e );
                    e.printStackTrace();
                } catch ( InvocationTargetException e ) {
//                    LOG.error( e );
                    e.printStackTrace();
                } catch ( NoSuchMethodException e ) {
//                    LOG.error( e );
                    e.printStackTrace();
                }
            }
            return -1;
        }

        /**
         * Merges the values in the specified facade info into this one. The
         * <code>info</code> object is not modified.
         * 
         * @param info
         *            The info to merge with.
         * @return <code>true</code> if the whole method is facadable.
         */
        private boolean mergeInfo( FacadeInfo info ) {
            if ( info != null ) {
                boolean allFacadable = true;

                if ( parameterFacadable != null ) {
                    if ( info.parameterFacadable == null
                            || parameterFacadable.length != info.parameterFacadable.length )
                        return false;

                    for ( int i = 0; i < parameterFacadable.length; i++ ) {
                        parameterFacadable[i] = parameterFacadable[i] || info.parameterFacadable[i];
                        allFacadable = allFacadable && parameterFacadable[i];
                    }
                } else if ( info.parameterFacadable != null ) {
                    return false;
                }

                returnFacadable = returnFacadable || info.returnFacadable;
                allFacadable = allFacadable && returnFacadable;

                if ( arrayTypeParameter == -1 )
                    arrayTypeParameter = info.arrayTypeParameter;
                allFacadable = allFacadable && arrayTypeParameter != -1;
            }

            return false;
        }
    }

    private static final FacadeAssistant INSTANCE = new FacadeAssistant();

    private Map<ClassLoader, Map<Object, Object>> loaderCache;

    private Map<Class<?>, Boolean> facadableClasses;

    private Map<Class<?>, Map<MethodSignature, FacadeInfo>> facadableMethods;

    FacadeAssistant() {
        facadableClasses = new java.util.HashMap<Class<?>, Boolean>();
        facadableMethods = new java.util.HashMap<Class<?>, Map<MethodSignature, FacadeInfo>>();
    }

    public static FacadeAssistant getInstance() {
        return INSTANCE;
    }

    public Object prepareObject( Object sourceObject, ClassLoader targetClassLoader ) {
        return prepareObject( sourceObject, targetClassLoader, false );
    }

    public Object prepareObject( Object sourceObject, ClassLoader targetClassLoader, boolean facadeShared ) {
        return prepareObject( sourceObject, Object.class, targetClassLoader, facadeShared );
    }

    public <T> T prepareObject( Object sourceObject, Class<T> targetType ) {
        return prepareObject( sourceObject, targetType, targetType.getClassLoader(), false );
    }

    public <T> T prepareObject( Object sourceObject, Class<T> targetType, ClassLoader targetClassLoader ) {
        return prepareObject( sourceObject, targetType, targetClassLoader, false );
    }

    /**
     * <p>
     * This method makes the supplied object ready for use in objects created
     * with the target class loader. The object returned may or may not
     * reference the original object. The rules are these: <p/>
     * <ol>
     * <li>If the target class loader is the same as the object's class loader,
     * return return the original object.</li>
     * <li>If the object's class exists as the exact same class in the target
     * class loader, return the original object.</li>
     * <li>If the object implements the {@link Facadable} interface, a proxy
     * object will be returned which implements all interfaces the object
     * implements, other than {@link Facadable} itself.</li>
     * <li>Otherwise, the object will be serialised and then unserialised using
     * the target class loader, so that all class references are local to that
     * class loader.</li>
     * </ol>
     * <p/>
     * <p>
     * As a result, unless the class implements {@link Facadable}, all objects
     * should be treated as read-only - sometimes a change will effect the
     * original, other times it won't. In the same way, {@link Facadable}
     * objects should treat all parameters passed to it in the same way.
     * </p>
     * 
     * @param <T>
     *            The type of the target object.
     * @param sourceObject
     *            The object.
     * @param targetType
     *            The target type. Must be from the target classloader.
     * @param targetClassLoader
     *            The target classloader.
     * @param facadeShared
     *            If <code>true</code> and the sourceObject is 'shared' (see
     *            {@link #isShared(Object, ClassLoader)), the object will be
     *            facaded even if it doesn't have the {@link Facaded}
     *            annotation.
     * @return An instance of the object prepared for the target class loader.
     * @throws IllegalArgumentException
     *             if the targetType is not from the target classloader
     */
    public <T> T prepareObject( Object sourceObject, Class<T> targetType, ClassLoader targetClassLoader,
            boolean facadeShared ) {
        return prepareObject( sourceObject, targetType, targetClassLoader, facadeShared, null );
    }

    public <T> T prepareObject( Object sourceObject, Class<T> targetType, ClassLoader targetClassLoader,
            boolean facadeShared, Class<?> componentType ) {
        if ( sourceObject == null )
            return null;

        Class<?> sourceType = sourceObject.getClass();
        ClassLoader sourceClassLoader = sourceType.getClassLoader();

        // Check if we need to do any processing at all...
        if ( sourceClassLoader == targetClassLoader ) {
            // The object is from the target class loader
            return ( T ) sourceObject;
        }
        // Check if it's shared.
        boolean isShared = isShared( sourceObject, targetClassLoader );
        if ( ( !facadeShared && isShared ) )
            return ( T ) sourceObject;

        // Handle special class types
        if ( Class.class.equals( sourceType ) ) {
            // It's a class
            return ( T ) findClass( sourceType, targetClassLoader );
        }
        if ( sourceType.isArray() && targetType.isArray() ) {
            // Handle arrays
            if ( componentType == null )
                componentType = targetType.getComponentType();
            return ( T ) toArray( sourceObject, componentType, targetClassLoader, facadeShared );
        } else if ( sourceType.isEnum() && targetType.isEnum() ) {
            // Handle enums
            return toEnum( sourceObject, targetType );
        }

        // Not shared, so see if it's wrapped.
        Object wrapped = getWrapped( sourceObject );
        if ( wrapped != null ) {
            if ( targetType.isInstance( wrapped ) )
                return ( T ) wrapped;
            else
                sourceObject = wrapped;
        }

        T targetObject = null;
        if ( ( Object.class.equals( targetType ) || targetType.isInterface() ) ) {
            if ( isShared || isFacadable( sourceObject, targetType ) ) {
                // If we get this far, we're going to have to convert it.
                // See if we have a cached facade already constructed
                targetObject = findCachedFacade( sourceObject, targetType );

                if ( targetObject == null ) {
                    targetObject = toFacade( sourceObject, targetType, targetClassLoader );

                    if ( targetObject != null ) {
                        cacheFacade( targetObject, sourceObject, targetType );
                    }
                }
            }

        }

        if ( targetObject == null && sourceObject instanceof Serializable ) {
            // If that fails, convert it via Serialization
            targetObject = toSerialized( sourceObject, targetType, targetClassLoader );
        }

        return targetObject;

    }
    
    private <T> T toSerialized( Object sourceObject, Class<T> targetType, ClassLoader targetClassLoader ) {
        try {
            // Freeze
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( baos );
            out.writeObject( sourceObject );
            out.close();
            
            // Thaw
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            CustomObjectInputStream in = new CustomObjectInputStream( bais, targetClassLoader );
            Object targetObject = in.readObject();
            in.close();
            
            if ( targetType.isInstance( targetObject ) )
                return ( T ) targetObject;
            
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Object toArray( Object sourceObject, Class<?> componentType, ClassLoader targetClassLoader,
            boolean requireFacade ) {
        // Localise the component type.
        componentType = findClass( componentType, targetClassLoader );

        int length = Array.getLength( sourceObject );
        Object targetArray = Array.newInstance( componentType, length );
        for ( int i = 0; i < length; i++ ) {
            Object sourceItem = Array.get( sourceObject, i );
            Object targetItem = prepareObject( sourceItem, componentType, targetClassLoader, requireFacade );
            Array.set( targetArray, i, targetItem );
        }
        return targetArray;
    }

    /**
     * Converts the specified object to an enum of the specified type. The
     * object must be an instance of the same enum, either in the same or an
     * alternate ClassLoader.
     * 
     * @param <T>
     *            The enum class type.
     * @param sourceObject
     *            The source object.
     * @param enumType
     *            The enum type in the target classloader.
     * @return
     */
    private <T> T toEnum( Object sourceObject, Class<T> enumType ) {
        try {
            return ( T ) enumType.getMethod( "valueOf", String.class ).invoke( null,
                    ( ( Enum<?> ) sourceObject ).name() );
        } catch ( IllegalArgumentException e ) {
            throw new FacadeException( "Unexpected exception: " + e.getMessage(), e );
        } catch ( SecurityException e ) {
            throw new FacadeException( "Unexpected exception: " + e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            throw new FacadeException( "Unexpected exception: " + e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            throw new FacadeException( "Unexpected exception: " + e.getMessage(), e );
        } catch ( NoSuchMethodException e ) {
            throw new FacadeException( "Unexpected exception: " + e.getMessage(), e );
        }
    }

    /**
     * Checks if the object is local to this classloader. If the object is
     * <code>null</code>, <code>false</code> is returned since it cannot be
     * determined conclusively.
     * 
     * @param object
     *            The object to test.
     * @return <code>true</code> if the object is not-null and is created by
     *         the current classloader.
     */
    public boolean isLocal( Object object ) {
        return object != null && getClass().getClassLoader().equals( object.getClass().getClassLoader() );
    }

    /**
     * Checks if the object can be facaded to the specified target type. For an
     * object to be facadable, it must implement the Facadable interface, and
     * implement the equivalent Class of the <code>targetType</code> in its
     * own ClassLoader.
     * 
     * @param sourceObject
     *            The object to check.
     * @param targetType
     *            The interface type to try facading to.
     * 
     * @return <code>true</code> if the object implements the
     *         {@link Facadable} interface.
     */
    private boolean isFacadable( Object sourceObject, Class<?> targetType ) {
        if ( sourceObject == null )
            return false;

        // First, check if it implements Facadable. If not, bail.
        Class<? extends Annotation> facadable = findAnnotationClass( Facadable.class, sourceObject );
        if ( facadable == null )
            return false;

        // Next, check that the source object even extends/implements the
        // specified targetType.
        if ( Object.class.equals( targetType ) || ( targetType != null && targetType.isInterface() ) ) {
            Class<?> sourceType = findClass( targetType, sourceObject );
            if ( sourceType == null || !sourceType.isInstance( sourceObject ) )
                return false;
        }

        // Lastly, check if the source object is facadable.
        return isFacadable( sourceObject.getClass(), facadable );
    }

    private boolean isFacadable( Class<?> type, Class<? extends Annotation> facadable ) {
        Boolean isFacadable = facadableClasses.get( type );
        if ( isFacadable == null ) {
            isFacadable = type.isAnnotationPresent( facadable );
            if ( !isFacadable ) {
                // Try its interfaces
                Class<?>[] interfaces = type.getInterfaces();
                for ( int i = 0; !isFacadable && i < interfaces.length; i++ ) {
                    isFacadable = isFacadable( interfaces[i], facadable );
                }
            }
            facadableClasses.put( type, isFacadable );
        }
        return isFacadable;
    }

    private Class<? extends Annotation> findAnnotationClass( Class<? extends Annotation> type, Object object ) {
        return findAnnotationClass( type, object.getClass() );
    }

    private Class<? extends Annotation> findAnnotationClass( Class<? extends Annotation> type, Class<?> sourceType ) {
        return findAnnotationClass( type, sourceType.getClassLoader() );
    }

    private Class<? extends Annotation> findAnnotationClass( Class<? extends Annotation> type,
            ClassLoader classLoader ) {
        return ( Class<? extends Annotation> ) findClass( type, classLoader );
    }

    /**
     * Finds the equivalent class for the provided <code>type</code> in the
     * class loader of the specified <code>target</code>. If none exists,
     * <code>null</code> is returned.
     * 
     * @param sourceType
     *            The source type
     * @param target
     *            The target object.
     * @return the target type.
     */
    public Class<?> findClass( Class<?> sourceType, Object target ) {
        return findClass( sourceType, target != null ? target.getClass().getClassLoader() : null );
    }

    /**
     * Finds the equivalent class for the provided <code>type</code> in the
     * specified target class loader. If none exists, <code>null</code> is
     * returned.
     * 
     * @param sourceType
     *            The source type.
     * @param targetClassLoader
     *            The target class loader.
     * @return The target type.
     */
    public Class<?> findClass( Class<?> sourceType, ClassLoader targetClassLoader ) {
        if ( sourceType.getClassLoader() == targetClassLoader )
            return sourceType;
        // NOTE: Added to work around an apparent bug in JUnit. Not sure this is
        // a good idea...
        if ( sourceType.isPrimitive() )
            return sourceType;

        return findClass( sourceType.getName(), targetClassLoader );
    }

    /**
     * Finds the class for the provided <code>classname</code> in the class
     * loader of the specified <code>targetObject</code>. If none can be
     * found, <code>null</code> is returned.
     * 
     * @param classname
     *            The classname to find.
     * @param targetObject
     *            The target object.
     * @return The target type.
     */
    public Class<?> findClass( String classname, Object targetObject ) {
        return findClass( classname, targetObject != null ? targetObject.getClass().getClassLoader() : null );
    }

    /**
     * Finds the class for the provided <code>classname</code> in the
     * specified <code>targetClassLoader</code>. If none can be found,
     * <code>null</code> is returned.
     * 
     * @param classname
     *            The class name.
     * @param targetClassLoader
     *            The target class loader.
     * @return the target type.
     */
    public Class<?> findClass( String classname, ClassLoader targetClassLoader ) {
        try {
            return Class.forName( classname, true, targetClassLoader );
        } catch ( ClassNotFoundException e ) {
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the object's class has a different class
     * loader than the specified <code>targetClassLoader</code>, but is still
     * able accessed directly in the target class loader. This indicates that
     * the object class is provided by another class loader which is an ancestor
     * to both the object class's loader and the <code>targetClassLoader</code>.
     * 
     * @param object
     *            The object to check.
     * @param targetClassLoader
     *            The class loader the check against.
     * 
     * @return <code>true</code> if the object is transportable.
     */
    public boolean isShared( Object object, ClassLoader targetClassLoader ) {
        if ( object == null )
            return true;

        Class<?> sourceClass = object.getClass();
        Class<?> targetClass = findClass( object.getClass(), targetClassLoader );
        return sourceClass != null && sourceClass == targetClass
                && sourceClass.getClassLoader() != targetClassLoader;
    }

    /**
     * Creates a facade of the specified object, if it is facadable. If not,
     * <code>null</code> is returned.
     * 
     * @param <T>
     *            The target type.
     * @param facadable
     *            The object being facaded.
     * @param targetType
     *            The target class.
     * @return The facaded object, or <code>null</code>.
     */
    private <T> T toFacade( Object facadable, Class<T> targetType, ClassLoader targetClassLoader ) {
        if ( !isFacadable( facadable, targetType ) )
            return null;

        Class<?>[] interfaces = getAllInterfaces( facadable );
        try {
            interfaces = toFacadeClasses( interfaces, targetClassLoader, false );
        } catch ( ClassNotFoundException e ) {
            // This shouldn't happen...
            e.printStackTrace();
        }

        if ( interfaces != null && interfaces.length > 0 ) {
            InvocationHandler invocationHandler = createInvocationHandler( targetClassLoader, facadable );
            if ( invocationHandler != null )
                return ( T ) Proxy.newProxyInstance( targetClassLoader, interfaces, invocationHandler );
        }
        return null;
    }

    private InvocationHandler createInvocationHandler( ClassLoader targetClassLoader, Object facadable ) {
        try {
            Class<? extends InvocationHandler> handlerClass = ( Class<? extends InvocationHandler> ) Class
                    .forName( FacadeInvocationHandler.class.getName(), true, targetClassLoader );
            Constructor<? extends InvocationHandler> cnst = handlerClass.getConstructor( Object.class );
            return cnst.newInstance( facadable );
        } catch ( ClassNotFoundException e ) {
            return null;
        } catch ( SecurityException e ) {
            // LOG.warn( "Incompatible version of Facade: " + e.getMessage(), e
            // );
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            // LOG.warn( "Incompatible version of Facade: " + e.getMessage(), e
            // );
            e.printStackTrace();
        } catch ( IllegalArgumentException e ) {
            // LOG.warn( "Incompatible version of Facade: " + e.getMessage(), e
            // );
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            // LOG.warn( "Error while creating Facade: " + e.getMessage(), e );
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            // LOG.warn( "Incompatible version of Facade: " + e.getMessage(), e
            // );
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            // LOG.warn( "Incompatible version of Facade: " + e.getMessage(), e
            // );
            e.printStackTrace();
        }
        return null;
    }

    private Class<?>[] getAllInterfaces( Object object ) {
        Set<Class<?>> interfaces = new java.util.HashSet<Class<?>>();
        addAllInterfaces( interfaces, object.getClass() );
        return ( Class<?>[] ) interfaces.toArray( new Class<?>[interfaces.size()] );
    }

    private void addAllInterfaces( Set<Class<?>> interfaces, Class<?> clazz ) {
        Class<?>[] classInterfaces = clazz.getInterfaces();

        // Add direct interfaces...
        for ( int i = 0; i < classInterfaces.length; i++ ) {
            if ( !interfaces.contains( classInterfaces[i] ) ) {
                interfaces.add( classInterfaces[i] );
                addAllInterfaces( interfaces, classInterfaces[i] );
            }
        }

        // Add superclass interfaces...
        if ( clazz.getSuperclass() != null )
            addAllInterfaces( interfaces, clazz.getSuperclass() );
    }

    /**
     * Converts the provided array of classes into the equivalent list of
     * classes local to the <code>targetClassLoader</code>.
     * 
     * @param sourceClasses
     *            The array of classes.
     * @param targetClassLoader
     *            The class loader the returned classes will be from.
     * @param requireClass
     *            if all classes must have an equivalent in the target
     *            classloader.
     * @return
     * @throws ClassNotFoundException
     *             if an equivalent class cannot be found and
     *             <code>requireClass</code> is <code>true</code>.
     */
    Class<?>[] toFacadeClasses( Class<?>[] sourceClasses, ClassLoader targetClassLoader, boolean requireClass )
            throws ClassNotFoundException {
        Class<?>[] targetInterfaces = new Class[sourceClasses.length];
        int targetIndex = 0;
        for ( int i = 0; i < sourceClasses.length; i++ ) {
            try {
                targetInterfaces[targetIndex] = Class.forName( sourceClasses[i].getName(), false,
                        targetClassLoader );
                targetIndex++;
            } catch ( ClassNotFoundException e ) {
                if ( requireClass )
                    throw e;
            }
        }

        sourceClasses = new Class[targetIndex];
        System.arraycopy( targetInterfaces, 0, sourceClasses, 0, targetIndex );
        return sourceClasses;
    }

    /**
     * Finds the cached facade for the object, if it already exists.
     * 
     * @param object
     *            The object being facaded.
     * @param type
     *            The target class.
     * @return The facade, if it exists.
     */
    private <T> T findCachedFacade( Object object, Class<T> type ) {
        if ( loaderCache != null ) {
            Map<Object, Object> facadeCache = loaderCache.get( type.getClassLoader() );
            if ( facadeCache != null )
                return ( T ) facadeCache.get( object );
        }
        return null;
    }

    private void cacheFacade( Object facade, Object sourceObject, Class<?> targetType ) {
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

        facadeCache.put( sourceObject, facade );
    }

    /**
     * Tests if the provided object is a locally-created facade of another
     * object.
     * 
     * @param object
     *            The object to test.
     * @return <code>true</code> if the object is a local facade.
     */
    public boolean isLocalFacade( Object object ) {
        return getInvocationHandler( object, FacadeInvocationHandler.class ) != null;
    }

    /**
     * Tests if the provided object is a facade of another object, from any
     * classloader.
     * 
     * @param object
     *            The object to test.
     * @return <code>true</code> if the object is a facade from any
     *         classloader.
     */
    public boolean isFacade( Object object ) {
        InvocationHandler handler = getInvocationHandler( object );
        Class<?> facadeHandler = findClass( FacadeInvocationHandler.class, object );
        return facadeHandler != null && facadeHandler.isInstance( handler );
    }

    private InvocationHandler getInvocationHandler( Object object ) {
        return getInvocationHandler( object, InvocationHandler.class );
    }

    /**
     * Returns the {@link InvocationHandler} for the specified object, if it is
     * a proxy object. It will also test if the handler implements the provided
     * type and only return it if it does, casting to the specified type.
     * 
     * @param <T>
     *            The InvocationHandler type.
     * @param object
     *            The object to search.
     * @param type
     *            The type of handler class.
     * @return The handler, or <code>null</code> if not found.
     */
    private <T extends InvocationHandler> T getInvocationHandler( Object object, Class<T> type ) {
        if ( object != null && Proxy.isProxyClass( object.getClass() ) ) {
            InvocationHandler handler = Proxy.getInvocationHandler( object );
            if ( type.isInstance( handler ) )
                return type.cast( handler );
        }
        return null;
    }

    /**
     * Returns the wrapped object if this is a facade, or <code>null</code> if
     * not.
     * 
     * @param facade
     *            The possible facade object.
     * @return The wrapped object.
     */
    public Object getWrapped( Object facade ) {
        return getWrapped( facade, Object.class );
    }

    /**
     * Returns the wrapped object if it is a facade and the class matches the
     * <code>wrappedClass</code>.
     * 
     * @param <W>
     *            The Type the wrapped object must implement/extend.
     * @param facade
     *            The facade object.
     * @param wrappedClass
     *            The class the wrapped object must implement.
     * @return The wrapped object, or <code>null</code> if it was not a
     *         facade.
     */
    public <W> W getWrapped( Object facade, Class<W> wrappedClass ) {
        Object wrapped = null;

        InvocationHandler handler = getInvocationHandler( facade, InvocationHandler.class );
        if ( handler instanceof FacadeInvocationHandler ) {
            // Local facade
            wrapped = ( ( FacadeInvocationHandler ) handler ).wrapped;
        } else if ( handler != null ) {
            Class<?> facadeClass = findClass( FacadeInvocationHandler.class, handler );
            if ( facadeClass != null && facadeClass.isInstance( handler ) ) {
                try {
                    wrapped = facadeClass.getDeclaredMethod( "getWrapped" ).invoke( handler );
                } catch ( SecurityException e ) {
                    throw new FacadeException( "Unexpected security exception: " + e.getMessage(), e );
                } catch ( IllegalArgumentException e ) {
                    throw new FacadeException( "Unexpected illegal access exception: " + e.getMessage(), e );
                } catch ( IllegalAccessException e ) {
                    throw new FacadeException( "Unexpected illegal access exception: " + e.getMessage(), e );
                } catch ( NoSuchMethodException e ) {
                    throw new FacadeException( "Unexpected no such method exception: " + e.getMessage(), e );
                } catch ( InvocationTargetException e ) {
                    throw new FacadeException( "Unexpected invocation target exception: " + e.getMessage(), e );
                }
            }
        }

        if ( wrappedClass.isInstance( wrapped ) )
            return ( W ) wrapped;

        return null;
    }

    /**
     * Finds the {@link FacadeInfo} for the specified type/signature combo. This
     * method will cache results for subsequent calls.
     * 
     * @param type
     *            The type.
     * @param signature
     *            The method signature.
     * @return The facade info.
     */
    FacadeInfo findFacadeInfo( Class<?> type, MethodSignature signature ) {
        if ( type == null || signature.findMethod( type ) == null )
            return null;

        Map<MethodSignature, FacadeInfo> signatureMap = facadableMethods.get( type );
        FacadeInfo info = null;
        if ( signatureMap == null ) {
            signatureMap = new java.util.HashMap<MethodSignature, FacadeInfo>();
            facadableMethods.put( type, signatureMap );
        } else {
            info = signatureMap.get( signature );
            // Check if the signature has already been found.
            if ( info != null || signatureMap.containsKey( signature ) )
                return info;
        }

        info = new FacadeInfo( type, signature );
        signatureMap.put( signature, info );

        return info;
    }
}
