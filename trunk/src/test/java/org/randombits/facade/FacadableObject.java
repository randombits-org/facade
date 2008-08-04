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

import java.util.Set;

public class FacadableObject implements FacadableInterface {

    private Object value;

    private FacadableInterface[] array;

    public FacadableObject( Object value ) {
        this.value = value;
    }

    public FacadableObject( Object value, int arraySize ) {
        this(value);
        array = new FacadableInterface[arraySize];
        for ( int i = 0; i < arraySize; i++ )
            array[i] = new FacadableObject( i );
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf( value );
    }

    public Result checkInterface( FacadableInterface ti ) {
        if ( this == ti )
            return Result.SAME;

        if ( FacadeAssistant.getInstance().isFacade( ti ) )
            return Result.FACADE;

        return Result.LOCAL;
    }

    public void doSomething() {
        
    }

    public UnfacadableInterface getFacadedUnfacadable() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<FacadableInterface> getSet() {
        // TODO Auto-generated method stub
        return null;
    }

    public UnfacadableInterface getUnfacadable() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFacadedUnfacadable( UnfacadableInterface ui ) {
        // TODO Auto-generated method stub
        
    }

    public void primitiveParam( int param ) {
        // TODO Auto-generated method stub
        
    }

    public Object getFacadableAsObject() {
        return array[0];
    }

    public <F> F[] getArray( Class<F> type ) {
        return ( F[] ) array;
    }

    public FacadableInterface[] getArray( FacadableInterface fi ) {
        return array;
    }

}
