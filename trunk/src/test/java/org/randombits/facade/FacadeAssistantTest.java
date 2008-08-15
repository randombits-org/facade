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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FacadeAssistantTest {
    private FacadeAssistant facadeAssistant;

    private ClassLoader classLoaderA;

    private ClassLoader classLoaderB;

    private FacadableInterface testA;

    private Class<?> interfaceB;

    private Object testB;

    private String valueB;

    private String valueA;

    private Class<FacadableObject> classA;

    private Class<?> classB;

    @Before
    public void setUp() throws Exception {
        facadeAssistant = new FacadeAssistant();

        // 'A'
        classLoaderA = getClass().getClassLoader();
        classA = FacadableObject.class;
        valueA = "A";
        testA = classA.getConstructor( Object.class ).newInstance( valueA );

        // 'B'
        classLoaderB = new IsolatedClassLoader().include( FacadeAssistant.class.getPackage() );
        interfaceB = Class.forName( FacadableInterface.class.getName(), true, classLoaderB );
        classB = Class.forName( FacadableObject.class.getName(), true, classLoaderB );
        valueB = "B";
        testB = classB.getConstructor( Object.class, int.class ).newInstance( valueB, 3 );
    }

    @After
    public void tearDown() throws Exception {
        facadeAssistant = null;
    }

    @Test
    public void testGetInstance() {
        FacadeAssistant assistant = FacadeAssistant.getInstance();
        assertSame( assistant, FacadeAssistant.getInstance() );
    }

    @Test
    public void testPrepareObject() {
        Object local = "Local";

        // Shared object
        Object prepared = facadeAssistant.prepareObject( local, Object.class );
        assertSame( local, prepared );

        // Local object
        prepared = facadeAssistant.prepareObject( testA, FacadableInterface.class );
        assertSame( testA, prepared );

        // Local made remote
        prepared = facadeAssistant.prepareObject( testA, interfaceB );
        assertNotSame( testA, prepared );
        assertTrue( interfaceB.isInstance( prepared ) );
        assertFalse( FacadableInterface.class.isInstance( prepared ) );
        assertTrue( Proxy.isProxyClass( prepared.getClass() ) );

        // Remote made local
        assertFalse( FacadableInterface.class.isInstance( testB ) );
        prepared = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        assertNotSame( testB, prepared );
        assertTrue( FacadableInterface.class.isInstance( prepared ) );
        assertFalse( interfaceB.isInstance( prepared ) );
        assertTrue( Proxy.isProxyClass( prepared.getClass() ) );
    }

    @Test
    public void testIsLocal() {
        String local = "local";

        assertFalse( facadeAssistant.isLocal( local ) );
        assertTrue( facadeAssistant.isLocal( testA ) );
        assertFalse( facadeAssistant.isLocal( testB ) );
    }

    @Test
    public void testFindClassWithClassObject() {
        Class<?> foundA = facadeAssistant.findClass( interfaceB, testA );
        Class<?> foundB = facadeAssistant.findClass( FacadableInterface.class, testB );

        assertNotSame( foundA, foundB );
        assertSame( FacadableInterface.class, foundA );
        assertNotSame( FacadableInterface.class, foundB );

        assertEquals( FacadableInterface.class.getName(), foundA.getName() );
        assertEquals( FacadableInterface.class.getName(), foundB.getName() );

        assertSame( classLoaderA, foundA.getClassLoader() );
        assertSame( classLoaderB, foundB.getClassLoader() );

    }

    @Test
    public void testFindClassWithClassClassLoader() {
        Class<?> foundA = facadeAssistant.findClass( FacadableInterface.class, classLoaderA );
        Class<?> foundB = facadeAssistant.findClass( interfaceB, classLoaderB );

        assertNotSame( foundA, foundB );
        assertSame( FacadableInterface.class, foundA );
        assertNotSame( FacadableInterface.class, foundB );

        assertEquals( FacadableInterface.class.getName(), foundA.getName() );
        assertEquals( FacadableInterface.class.getName(), foundB.getName() );

        assertSame( classLoaderA, foundA.getClassLoader() );
        assertSame( classLoaderB, foundB.getClassLoader() );
    }

    @Test
    public void testFindClassStringObject() {
        Class<?> foundA = facadeAssistant.findClass( FacadableInterface.class.getName(), testA );
        Class<?> foundB = facadeAssistant.findClass( FacadableInterface.class.getName(), testB );

        assertNotSame( foundA, foundB );
        assertSame( FacadableInterface.class, foundA );
        assertNotSame( FacadableInterface.class, foundB );

        assertEquals( FacadableInterface.class.getName(), foundA.getName() );
        assertEquals( FacadableInterface.class.getName(), foundB.getName() );

        assertSame( classLoaderA, foundA.getClassLoader() );
        assertSame( classLoaderB, foundB.getClassLoader() );
    }

    @Test
    public void testFindClassStringClassLoader() {
        Class<?> foundA = facadeAssistant.findClass( FacadableInterface.class.getName(), classLoaderA );
        Class<?> foundB = facadeAssistant.findClass( FacadableInterface.class.getName(), classLoaderB );

        assertNotSame( foundA, foundB );
        assertSame( FacadableInterface.class, foundA );
        assertNotSame( FacadableInterface.class, foundB );

        assertEquals( FacadableInterface.class.getName(), foundA.getName() );
        assertEquals( FacadableInterface.class.getName(), foundB.getName() );

        assertSame( classLoaderA, foundA.getClassLoader() );
        assertSame( classLoaderB, foundB.getClassLoader() );
    }

    @Test
    public void testIsSharedObjectClassLoader() throws IllegalArgumentException, SecurityException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            ClassNotFoundException {
        Object shared = "shared";

        assertTrue( facadeAssistant.isShared( shared, classLoaderA ) );
        assertTrue( facadeAssistant.isShared( shared, classLoaderB ) );

        assertFalse( facadeAssistant.isShared( testA, classLoaderA ) );
        assertFalse( facadeAssistant.isShared( testA, classLoaderB ) );

        assertFalse( facadeAssistant.isShared( testB, classLoaderA ) );
        assertFalse( facadeAssistant.isShared( testB, classLoaderB ) );

    }

    @Test
    public void testToFacadeClassesOptional() {
        Class<?>[] sourceClasses = {Object.class, FacadableInterface.class};
        try {
            Class<?>[] targetClasses = facadeAssistant.toFacadeClasses( sourceClasses, classLoaderB, false );
            assertEquals( 2, targetClasses.length );
            assertSame( Object.class, targetClasses[0] );
            assertSame( interfaceB, targetClasses[1] );
        } catch ( ClassNotFoundException e ) {
            fail( "Unexpected exception: " + e.getMessage() );
        }
    }

    @Test
    public void testIsLocalFacade() {
        Object object = "Local";
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        Object facadeOfA = facadeAssistant.prepareObject( testA, interfaceB );

        assertFalse( facadeAssistant.isLocalFacade( object ) );
        assertFalse( facadeAssistant.isLocalFacade( testA ) );
        assertFalse( facadeAssistant.isLocalFacade( testB ) );
        assertTrue( facadeAssistant.isLocalFacade( facadeOfB ) );
        assertFalse( facadeAssistant.isLocalFacade( facadeOfA ) );
    }

    @Test
    public void testIsFacade() {
        Object object = "Local";
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        Object facadeOfA = facadeAssistant.prepareObject( testA, interfaceB );

        assertFalse( facadeAssistant.isFacade( object ) );
        assertFalse( facadeAssistant.isFacade( testA ) );
        assertFalse( facadeAssistant.isFacade( testB ) );
        assertTrue( facadeAssistant.isFacade( facadeOfB ) );
        assertTrue( facadeAssistant.isFacade( facadeOfA ) );
    }

    @Test
    public void testGetWrappedObject() {
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        Object facadeOfA = facadeAssistant.prepareObject( testA, interfaceB );

        assertSame( testB, facadeAssistant.getWrapped( facadeOfB ) );
        assertSame( testA, facadeAssistant.getWrapped( facadeOfA ) );
    }

    @Test
    public void testGetWrapped_ObjectClassOfW() {
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        Object facadeOfA = facadeAssistant.prepareObject( testA, interfaceB );

        assertSame( testB, facadeAssistant.getWrapped( facadeOfB, interfaceB ) );
        assertNull( facadeAssistant.getWrapped( facadeOfB, FacadableInterface.class ) );

        assertNull( facadeAssistant.getWrapped( facadeOfA, interfaceB ) );
        assertSame( testA, facadeAssistant.getWrapped( facadeOfA, FacadableInterface.class ) );

        // Check passing in a non-implemented interface.
        assertNull( facadeAssistant.getWrapped( facadeOfA, Runnable.class ) );
    }

    @Test
    public void testPushFacadedObject() {
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        
        assertTrue( FacadeAssistant.getInstance().isLocalFacade( facadeOfB ) );

        FacadableInterface.Result result = facadeOfB.checkInterface( testA );
        assertSame( FacadableInterface.Result.FACADE, result );

        FacadableInterface[] array = facadeOfB.getArray( FacadableInterface.class );
        assertEquals( 3, array.length );
        for ( int i = 0; i < array.length; i++ ) {
            assertEquals( Integer.valueOf( i ), array[i].getValue() );
        }
    }
    
    @Test
    public void testGenericArray() {
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        
        FacadableInterface[] array = facadeOfB.getArray( FacadableInterface.class );
        assertEquals( 3, array.length );
        for ( int i = 0; i < array.length; i++ ) {
            assertEquals( Integer.valueOf( i ), array[i].getValue() );
        }

    }

    @Test
    public void testFacadedAsObject() {
        FacadableInterface facadeOfB = facadeAssistant.prepareObject( testB, FacadableInterface.class );
        Object value = facadeOfB.getFacadableAsObject();
        
        assertTrue( value instanceof FacadableInterface );
    }
    
    @Test
    public void testFacadedSubclass() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        
        Object subclassB = Class.forName( FacadableSubclass.class.getName(), true, classLoaderB ).getConstructor( Object.class, int.class ).newInstance( valueB, 3 );
        assertFalse( facadeAssistant.isLocal( subclassB ) );
        
        FacadableInterface facadeB = facadeAssistant.prepareObject( subclassB, FacadableInterface.class );
        assertTrue( facadeAssistant.isLocalFacade( facadeB ) );
    }
    
}
