package org.randombits.facade.apt;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.emptyList;
import java.util.Collection;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class Factory implements AnnotationProcessorFactory {
    private static final Collection<String> SUPPORTED_TYPES
 		= unmodifiableList( asList( "org.randombits.facade.Facadable", "org.randombits.facade.ArrayTypeParameter" ) );
    private static final Collection<String> SUPPORTED_OPTIONS = emptyList(); 

    public AnnotationProcessor getProcessorFor( Set<AnnotationTypeDeclaration> declarations,
            AnnotationProcessorEnvironment env ) {
        if ( declarations.isEmpty() )
            return AnnotationProcessors.NO_OP;
        else
            return new FacadeAnnotationProcessor( env );
    }

    public Collection<String> supportedAnnotationTypes() {
        return SUPPORTED_TYPES;
    }

    public Collection<String> supportedOptions() {
        return SUPPORTED_OPTIONS;
    }

}
