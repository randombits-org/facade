package org.randombits.facade;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link MethodSignature} class.
 */
public class MethodSignatureTest {

    public static class Test1 {
        public void simpleMethod() {
        }

        public void complexMethod( String param1, int param2 ) {
        }
    }

    public static class Test2 {
        public void complexMethod( String param1, int param2 ) {
        }
    }

    public static class Test3 extends Test2 {
        public void simpleMethod() {
        }
    }

    @Test
    public void testMethodConstructor() throws NoSuchMethodException {
        Method simpleMethod = getClass().getMethod( "simpleMethod" );

        MethodSignature signature = new MethodSignature( simpleMethod );
        assertEquals( "simpleMethod", signature.getName() );
        assertEquals( 0, signature.getParameterCount() );

        findSimpleMethods( simpleMethod, signature );
    }

    @Test
    public void testNameConstructor() throws NoSuchMethodException {
        Method simpleMethod = getClass().getMethod( "simpleMethod" );

        MethodSignature signature = new MethodSignature( "simpleMethod" );
        assertEquals( "simpleMethod", signature.getName() );
        assertEquals( 0, signature.getParameterCount() );

        findSimpleMethods( simpleMethod, signature );
    }

    void assertMethodFound( MethodSignature signature, Class<?> type ) {
        assertNotNull( signature.findMethod( type ) );
    }

    void assertMethodNotFound( MethodSignature signature, Class<?> type ) {
        assertNull( signature.findMethod( type ) );
    }

    void assertDeclaredMethodFound( MethodSignature signature, Class<?> type ) {
        assertNotNull( signature.findDeclaredMethod( type ) );
    }

    void assertDeclaredMethodNotFound( MethodSignature signature, Class<?> type ) {
        assertNull( signature.findDeclaredMethod( type ) );
    }

    private void findSimpleMethods( Method simpleMethod, MethodSignature signature ) {
        // Find it in the current class
        assertMethodFound( signature, getClass() );

        // Find it in the Test1 class
        assertMethodFound( signature, Test1.class );

        // Can't find it in the Test2 class
        assertMethodNotFound( signature, Test2.class );

        // Find it in Test3
        assertMethodFound( signature, Test3.class );
    }

    @Test
    public void testGetParameterTypeOutOfBounds() {
        MethodSignature signature = new MethodSignature( "foo" );
        try {
            signature.getParameterType( 0 );
            fail( "Expected an ArrayIndexOutOfBoundsException" );
        } catch ( ArrayIndexOutOfBoundsException e ) {
        }
    }

    @Test
    public void testFindComplexMethod() {
        MethodSignature signature = new MethodSignature( "complexMethod", String.class, int.class );

        assertEquals( 2, signature.getParameterCount() );
        assertEquals( String.class, signature.getParameterType( 0 ) );
        assertEquals( int.class, signature.getParameterType( 1 ) );
        assertArrayEquals( new Class<?>[]{String.class, int.class}, signature.getParameterTypes() );

        assertMethodFound( signature, getClass() );
        assertMethodFound( signature, Test1.class );
        assertMethodFound( signature, Test2.class );
        assertMethodFound( signature, Test3.class );
    }

    @Test
    public void testFindSimpleDeclaredMethod() {
        MethodSignature signature = new MethodSignature( "simpleMethod" );
        assertDeclaredMethodFound( signature, getClass() );
        assertDeclaredMethodFound( signature, Test1.class );
        assertDeclaredMethodNotFound( signature, Test2.class );
        assertDeclaredMethodFound( signature, Test3.class );
    }

    @Test
    public void testFindDeclaredMethod() {
        MethodSignature complexSignature = new MethodSignature( "complexMethod", String.class, int.class );

        assertDeclaredMethodFound( complexSignature, getClass() );
        assertDeclaredMethodFound( complexSignature, Test1.class );
        assertDeclaredMethodFound( complexSignature, Test2.class );
        assertDeclaredMethodNotFound( complexSignature, Test3.class );
    }

    @Test
    public void testEquals() {
        MethodSignature foo = new MethodSignature( "foo", String.class );
        MethodSignature foo2 = new MethodSignature( "foo", String.class );
        MethodSignature foo3 = new MethodSignature( "foo", int.class );
        MethodSignature bar = new MethodSignature( "bar", String.class );

        assertEquals( foo, foo2 );
        assertFalse( foo.equals( foo3 ) );
        assertFalse( foo.equals( bar ) );
    }

    public void simpleMethod() {
    }

    public void complexMethod( String param1, int param2 ) {
    }
}
