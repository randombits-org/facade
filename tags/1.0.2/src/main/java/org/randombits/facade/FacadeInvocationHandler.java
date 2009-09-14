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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.randombits.facade.FacadeAssistant.FacadeInfo;

public class FacadeInvocationHandler implements InvocationHandler {
    Object wrapped;

    private ClassLoader wrapperLoader;

    private ClassLoader wrappedLoader;

    /**
     * Constructs a new handler.
     * 
     * @param facaded
     *            The object to facade.
     */
    public FacadeInvocationHandler( Object facaded ) {
        init( facaded );
    }

    private void init( Object facaded ) {
        if ( wrapped == null ) {
            this.wrapped = facaded;

            wrapperLoader = getClass().getClassLoader();
            wrappedLoader = facaded.getClass().getClassLoader();
        }
    }

    public Object getWrapped() {
        return wrapped;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Exception {
        try {
            Method iMethod = findWrappedMethod( method );
            MethodSignature signature = new MethodSignature( iMethod );
            FacadeInfo info = FacadeAssistant.getInstance().findFacadeInfo( wrapped.getClass(), signature );
            Class<?> arrayType = findArrayType( info, args );

            args = toWrapped( args, method.getParameterTypes(), info );
            Class<?> returnType = FacadeAssistant.getInstance().findClass( iMethod.getReturnType(), wrapperLoader );
            Object returnValue = iMethod.invoke( wrapped, args );
            return FacadeAssistant.getInstance().prepareObject( returnValue, returnType, wrapperLoader,
                    info.isReturnFacadable(), arrayType );
        } catch ( NoSuchMethodException e ) {
            throw new FacadeException( e );
        } catch ( ClassNotFoundException e ) {
            throw new FacadeException( e );
        } catch ( IllegalAccessException e ) {
            throw new FacadeException( e );
        } catch ( InvocationTargetException e ) {
            if ( e.getTargetException() instanceof Exception ) {
                Exception targetException = ( Exception ) e.getTargetException();
                Class<? extends Exception> wrapperClass = ( Class<? extends Exception> ) FacadeAssistant
                        .getInstance().findClass( targetException.getClass(), wrapperLoader );
                Exception preparedException = FacadeAssistant.getInstance().prepareObject( targetException,
                        wrapperClass, wrapperLoader, false );
                if ( preparedException != null )
                    throw preparedException;
            }
            throw new FacadeException( e );
        }
    }

    private Class<?> findArrayType( FacadeInfo info, Object[] args ) {
        int arrayTypeParam = info.getArrayTypeParameter();
        if ( arrayTypeParam >= 0 ) {
            if ( args == null || arrayTypeParam >= args.length )
                throw new FacadeException( "Illegal array type parameter index: " + arrayTypeParam );

            Object arg = args[arrayTypeParam];

            if ( arg == null )
                return null;

            else if ( arg instanceof Class )
                return ( Class<?> ) arg;

            else {
                Class<?> argClass = arg.getClass();
                return argClass.isArray() ? argClass.getComponentType() : argClass;
            }
        }
        return null;
    }

    private Object[] toWrapped( Object[] objs, Class<?>[] types, FacadeInfo info ) {
        if ( objs == null )
            return null;

        Object wrappedObjs[] = new Object[objs.length];
        for ( int i = 0; i < types.length; i++ ) {
            if ( Class.class.isInstance( objs[i] ) ) {
                wrappedObjs[i] = FacadeAssistant.getInstance().findClass( ( Class<?> ) objs[i], wrappedLoader );
            } else {
                Class<?> wrappedClass = FacadeAssistant.getInstance().findClass( types[i], wrappedLoader );
                wrappedObjs[i] = FacadeAssistant.getInstance().prepareObject( objs[i], wrappedClass,
                        wrappedLoader, info.isParameterFacadable( i ) );
            }
        }

        return wrappedObjs;
    }

    private Method findWrappedMethod( Method method ) throws NoSuchMethodException, ClassNotFoundException {
        Class<?>[] paramTypes = FacadeAssistant.getInstance().toFacadeClasses( method.getParameterTypes(),
                wrappedLoader, true );
        Method wrappedMethod = findHighestMethod( wrapped.getClass(), method.getName(), paramTypes );
        if ( wrappedMethod == null )
            throw new NoSuchMethodException( method.getName() );
        return wrappedMethod;
    }

    // recurse up hierarchy, looking for highest method
    private Method findHighestMethod( Class<?> cls, String method, Class<?>... paramTypes )
            throws SecurityException {
        Class<?>[] interfaces = cls.getInterfaces();
        for ( int i = 0; i < interfaces.length; i++ ) {
            Method ifaceMethod = findHighestMethod( interfaces[i], method, paramTypes );
            if ( ifaceMethod != null )
                return ifaceMethod;
        }
        if ( cls.getSuperclass() != null ) {
            Method parentMethod = findHighestMethod( cls.getSuperclass(), method, paramTypes );
            if ( parentMethod != null )
                return parentMethod;
        }

        try {
            return cls.getMethod( method, paramTypes );
        } catch ( NoSuchMethodException e ) {
            return null;
        }
    }

}