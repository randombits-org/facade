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

/**
 * This class contains several bad ways to use the {@link Facadable} and
 * {@link ArrayTypeParameter} annotations.
 */
@Facadable public interface BadInterface<T> {
    // Return types

    /**
     * <b>BAD:</b> This is illegal because {@link Facadable} on methods applies
     * to the return value.
     */
    @Facadable void voidReturnMethod();

    /**
     * <b>BAD:</b> This is illegal because primitives can't be facaded.
     * 
     * @return an integer.
     */
    @Facadable int primitiveReturnMethod();

    // Arrays

    /**
     * <b>BAD:</b> This is illegal because generic type information is lost at
     * runtime. As such, all methods returning a generic array should be
     * annotated with the {@link ArrayTypeParameter}.
     * 
     * @param <A>
     *            The type.
     * @param type
     *            The type class.
     * @return the typed array.
     */
    <A> A[] genericArrayMethod( Class<A> type );

    /**
     * <b>BAD:</b> Because arrays are fixed to their type, and this is not a
     * generic type, there is no point trying to determine an alternate array
     * type.
     * 
     * @param type
     *            The type to cast to.
     * @return The Object array.
     */
    @ArrayTypeParameter(0) Object[] typedArrayMethod( Class<Object> type );

    /**
     * <b>BAD:</b> This is illegal because the parameter type (<code>B</code>)
     * does not match the returned array's component type (<code>A</code>).
     * 
     * @param <A>
     *            The A type.
     * @param <B>
     *            The B type.
     * @param type
     *            The B type class.
     * @return The A array.
     */
    @ArrayTypeParameter(0) <A, B> A[] mistypedArrayMethod( Class<B> type );

    /**
     * <b>BAD:</b> This is illegal because the parameter index is less than 0.
     * 
     * @param <A>
     *            The A type.
     * @param type
     *            The type class.
     * @return The typed array.
     */
    @ArrayTypeParameter(-1) <A> A[] badParameterIndexMethod( Class<A> type );

    /**
     * <b>BAD:</b> This is illegal because the parameter index is less than
     * greater than the number of parameter items.
     * 
     * @param <A>
     *            The A type.
     * @param type
     *            The type class.
     * @return The typed array.
     */
    @ArrayTypeParameter(2) <A> A[] badParameterIndexMethod( Class<A> type, int number );

}
