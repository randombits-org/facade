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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * This class simply loads classes that are available to the test environment in
 * a way which is will have the classes from each IsolatedClassLoader being
 * recognised as being from separate class loaders. This is important for
 * testing plugin-related since each plugin is loaded in its own space once
 * uploaded into Confluence, etc.
 * 
 * @author David Peterson
 * 
 */
public class IsolatedClassLoader extends ClassLoader {

    private List<Pattern> includes;

    private List<Pattern> excludes;

    public IsolatedClassLoader() {
    }

    public IsolatedClassLoader( ClassLoader parent ) {
        super( parent );
    }

    public IsolatedClassLoader exclude( Package... packages ) {
        for ( Package p : packages ) {
            String name = getPattern( p );
            exclude( name );
        }
        return this;
    }

    public IsolatedClassLoader exclude( Class<?>... classes ) {
        for ( Class<?> c : classes ) {
            String name = getPattern( c );
            exclude( name );
        }
        return this;
    }

    public IsolatedClassLoader exclude( String... patterns ) {
        if ( excludes == null )
            excludes = new java.util.ArrayList<Pattern>();

        for ( String pattern : patterns ) {
            excludes.add( Pattern.compile( pattern ) );
        }

        return this;
    }

    public IsolatedClassLoader include( Package... packages ) {
        for ( Package p : packages ) {
            String name = getPattern( p );
            include( name );
        }
        return this;
    }

    public IsolatedClassLoader include( Class<?>... classes ) {
        for ( Class<?> c : classes ) {
            String name = getPattern( c );
            include( name );
        }
        return this;
    }

    public IsolatedClassLoader include( String... patterns ) {
        if ( includes == null )
            includes = new java.util.ArrayList<Pattern>();
        for ( String pattern : patterns ) {
            includes.add( Pattern.compile( pattern ) );
        }
        return this;
    }

    private String getPattern( Class<?> c ) {
        String name = c.getName();
        name = name.replaceAll( ".", "\\." );
        name = name.replaceAll( "$", "\\$" );
        return name;
    }

    private String getPattern( Package p ) {
        String name = p.getName();
        name = name.replaceAll( ".", "\\." ) + "\\..*";
        return name;
    }

    @Override
    public Class<?> loadClass( String name ) throws ClassNotFoundException {
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
        // First, check that we are supposed to load this class ourself
        // Check excludes first
        if ( excludes != null ) {
            for ( Pattern p : excludes ) {
                if ( p.matcher( name ).matches() )
                    return null;
            }
        }

        // Then check includes.
        if ( includes != null ) {
            boolean found = false;
            for ( Pattern p : includes ) {
                if ( p.matcher( name ).matches() ) {
                    found = true;
                    break;
                }
            }
            if ( !found )
                return null;
        }

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
