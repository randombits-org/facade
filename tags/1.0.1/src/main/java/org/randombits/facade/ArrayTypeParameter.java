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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows the you to specify which input parameter determines
 * the type of the returned array. This is particularly critical in cases where
 * generics are used to automatically cast returned arrays to a specific type.
 * For example:
 * 
 * <pre>
 * public &lt;T&gt; T[] getArray( Class&lt;T&gt; type );
 * </pre>
 * 
 * <p>
 * Because generic type information is lost at runtime, <code>T</code> will
 * simply be <code>Object</code>, which means that the array type will be an
 * <code>{@link Object}[]</code>. Unfortunately, you can't just cast arrays
 * of one type to those of another type, so you will get a
 * {@link ClassCastException} at runtime when accessing a facaded object.
 * 
 * <p>
 * The workaround is to use this attribute to decorate the method and so that
 * the Facade system can determine what class of array to create. This would be
 * done something like this:
 * 
 * <pre>
 * &#064;ArrayTypeParameter(0) public &lt;T&gt; T[] getArray( Class&lt;T&gt; type );
 * </pre>
 * 
 * <p>
 * This tells the Facade system to use the '0' parameter ('type') to determine
 * the array return type. It will use parameters in the following ways:
 * 
 * <ol>
 * <li>If the parameter is a {@link Class}, use that class as the array type.</li>
 * <li>If the parameter is an array object, as determined by
 * {@link Class#isArray()}, the {@link Class#getComponentType()} of the object
 * will be the new array type.</li>
 * <li>If the parameter is any other object, the class of that object is the
 * new array type.</li>
 * </ol>
 * 
 * <p>
 * This will cover the following conditions:
 * 
 * <pre>
 * &#064;ArrayTypeParameter(0) public &lt;T extends Foo&gt; T[] findFoo( Class&lt;T&gt; type );
 * 
 * &#064;ArrayTypeParameter(1) public &lt;T&gt; T[] filteredArray( Filter&lt;T&gt; filter, T[] array );
 * 
 * &#064;ArrayTypeParameter(0) public &lt;T&gt; T[] getChildren( T parent );
 * </pre>
 * 
 * <p>
 * The allowed parameter types are restricted to these because they are the only
 * way to guarantee that the required class type is discoverable at runtime.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface ArrayTypeParameter {
    /**
     * The index number of the parameter that determines the returned array type. Must be between
     * 0 and 1 less than the number of parameters.
     * 
     * @return The parameter index.
     */
    int value();
}
