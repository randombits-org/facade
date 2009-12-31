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

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Defines the name and parameter types for a method.
 */
class MethodSignature {
    
    private String name;

    private Class<?>[] parameterTypes;
    
    public MethodSignature( Method method ) {
        this.name = method.getName();
        this.parameterTypes = method.getParameterTypes();
    }

    public MethodSignature( String name, Class<?>... parameterTypes ) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the type at the specified index.
     * 
     * @param i
     *            The index.
     * @return The type.
     * @throws ArrayIndexOutOfBoundsException
     *             if <code>i</code> is negative or greater than the parameter
     *             count.
     */
    public Class<?> getParameterType( int i ) {
        if ( parameterTypes == null )
            throw new ArrayIndexOutOfBoundsException( i );

        return parameterTypes[i];
    }

    /**
     * Returns a copy of the parameter types array. Modifying this array will
     * not affect the method signature.
     * 
     * @return The parameter type array.
     */
    public Class<?>[] getParameterTypes() {
        Class<?>[] copy = new Class<?>[parameterTypes.length];
        System.arraycopy( parameterTypes, 0, copy, 0, parameterTypes.length );
        return copy;
    }

    /**
     * Returns the number of parameters in this method.
     * 
     * @return The number of parameters.
     */
    public int getParameterCount() {
        return parameterTypes == null ? 0 : parameterTypes.length;
    }

    /**
     * Finds the declared method that matches this method signature in the
     * specified type, or <code>null</code> if it could not be found.
     * 
     * @param type
     *            The type.
     * @return The method, if present.
     */
    public Method findDeclaredMethod( Class<?> type ) {
        try {
            return type.getDeclaredMethod( name, (Class<?>[])parameterTypes );
        } catch ( SecurityException e ) {
            // Do nothing.
        } catch ( NoSuchMethodException e ) {
            // Do nothing.
        }
        return null;
    }

    /**
     * Returns the method which matches this method signature in the specified
     * type, or <code>null</code> if it could not be found.
     * 
     * @param type
     *            The type
     * @return The method, if present.
     */
    public Method findMethod( Class<?> type ) {
        try {
            return type.getMethod( name, (Class<?>[])parameterTypes );
        } catch ( SecurityException e ) {
            // Do nothing.
        } catch ( NoSuchMethodException e ) {
            // Do nothing
        }
        return null;
    }

    @Override public boolean equals( Object obj ) {
        if ( obj instanceof MethodSignature ) {
            MethodSignature ms = ( MethodSignature ) obj;
            return name.equals( ms.name ) && Arrays.equals( parameterTypes, ms.parameterTypes );
        }
        return false;
    }

    @Override public int hashCode() {
        return name.hashCode() + Arrays.hashCode( parameterTypes );
    }

    @Override public String toString() {
        return "[name: " + name + "; parameterTypes: " + Arrays.toString( parameterTypes ) + "]";
    }
}