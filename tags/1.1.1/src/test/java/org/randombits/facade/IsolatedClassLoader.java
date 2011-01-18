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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class simply loads classes that are available to the test environment in
 * a way which is will have the classes from each IsolatedClassLoader being
 * recognised as being from separate class loaders. This is important for
 * testing plugin-related since each plugin is loaded in its own space once
 * uploaded into Confluence, etc.
 *
 * <p>You can block, isolate or inherit specific classes, packages or name patterns.
 *
 * <ul>
 *  <li>blocked: Will not be loaded at all by the class loader.</li>
 *  <li>isolated: Will be loaded separately by this class loader. Class instances will not match those from other class loaders, including the parent class loader.</li>
 *  <li>inherited: Will be loaded by the parent or System class loader, not locally. This can be used to override specific subsets that have been isolated.</li>
 * </ul>
 *
 * @author David Peterson
 */
public class IsolatedClassLoader extends ClassLoader {

    private interface PatternHandler<T> {

        Pattern toPattern( T value );
    }

    private static PatternHandler<Class<?>> CLASS_HANDLER = new PatternHandler<Class<?>>() {

        public Pattern toPattern( Class<?> value ) {
            String name = value.getName().replaceAll( "\\.", "\\\\.");
            return Pattern.compile( name );
        }
    };

    private static PatternHandler<Package> PACKAGE_HANDLER = new PatternHandler<Package>() {

        public Pattern toPattern( Package value ) {
            String name = value.getName().replaceAll( "\\.", "\\\\." );
            return Pattern.compile( name + "\\..+" );

        }
    };

    private static PatternHandler<String> STRING_HANDLER = new PatternHandler<String>() {

        public Pattern toPattern( String value ) {
            return Pattern.compile( value );
        }
    };

    private List<Pattern> inherited = new ArrayList<Pattern>();

    private List<Pattern> isolated = new ArrayList<Pattern>();

    private List<Pattern> blocked = new ArrayList<Pattern>();

    public IsolatedClassLoader() {
    }

    public IsolatedClassLoader( ClassLoader parent ) {
        super( parent );
    }

    private <T> void addPatterns( List<Pattern> list, PatternHandler<T> handler, T... values ) {
        for ( T value : values ) {
            list.add( handler.toPattern( value ) );
        }
    }

    public IsolatedClassLoader inherit( Package... packages ) {
        addPatterns( inherited, PACKAGE_HANDLER, packages );
        return this;
    }

    public IsolatedClassLoader inherit( Class<?>... classes ) {
        addPatterns( inherited, CLASS_HANDLER, classes );
        return this;
    }

    public IsolatedClassLoader inherit( String... patterns ) {
        addPatterns( inherited, STRING_HANDLER, patterns );
        return this;
    }

    public IsolatedClassLoader block( Package... packages ) {
        addPatterns( blocked, PACKAGE_HANDLER, packages );
        return this;
    }

    public IsolatedClassLoader block( Class<?>... classes ) {
        addPatterns( blocked, CLASS_HANDLER, classes );
        return this;
    }

    public IsolatedClassLoader block( String... patterns ) {
        addPatterns( blocked, STRING_HANDLER, patterns );
        return this;
    }

    public IsolatedClassLoader isolate( Package... packages ) {
        addPatterns( isolated, PACKAGE_HANDLER, packages );
        return this;
    }

    public IsolatedClassLoader isolate( Class<?>... classes ) {
        addPatterns( isolated, CLASS_HANDLER, classes );
        return this;
    }

    public IsolatedClassLoader isolate( String... patterns ) {
        addPatterns( isolated, STRING_HANDLER, patterns );
        return this;
    }

    @Override
    public Class<?> loadClass( String name ) throws ClassNotFoundException {
        // Excluded classes are not loaded at all
        for ( Pattern p : blocked ) {
            if ( p.matcher( name ).matches() )
                throw new ClassNotFoundException( name );
        }

        // check if the class is already loaded
        Class<?> loadedClass = findLoadedClass( name );

        if ( loadedClass == null ) {
            loadedClass = findStubClass( name );

            if ( loadedClass == null )
                loadedClass = super.loadClass( name );

            if ( loadedClass == null )
                loadedClass = getSystemClassLoader().loadClass( name );
        }
        return loadedClass;
    }

    protected Class<?> findStubClass( String name ) throws ClassNotFoundException {
        // First, check that we are supposed to load this class ourselves
        // Check explicit inherits first - these get thrown back to the parent/system class loader.
        for ( Pattern p : inherited ) {
            if ( p.matcher( name ).matches() )
                return null;
        }

        // Then check includes.
        boolean found = false;
        for ( Pattern p : isolated ) {
            if ( p.matcher( name ).matches() ) {
                found = true;
                break;
            }
        }
        if ( !found )
            return null;

        // If we're still here, load the class.
        byte[] b = loadClassData( name );
        try {
            return defineClass( name, b, 0, b.length );
        } catch ( SecurityException e ) {
            return null;
        }
    }

    private byte[] loadClassData( String name ) throws ClassNotFoundException {
        String path = "/" + name.replace( '.', '/' ) + ".class";
        InputStream in = getClass().getResourceAsStream( path );
        if ( in == null )
            throw new ClassNotFoundException( name );
        try {
            byte[] bytes = IOUtils.toByteArray( in );
            in.close();
            return bytes;
        } catch ( IOException e ) {
            throw new ClassNotFoundException( name, e );
        }
    }
}
