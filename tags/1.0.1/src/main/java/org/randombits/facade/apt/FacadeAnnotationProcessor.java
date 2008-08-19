package org.randombits.facade.apt;

import java.util.Collection;
import java.util.Map;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

public class FacadeAnnotationProcessor implements AnnotationProcessor {
    private static final String CLASS_NAME = Class.class.getName();

    private AnnotationProcessorEnvironment environment;

    private TypeDeclaration facade;

    private TypeDeclaration arrayTypeParameter;

    private DeclarationVisitor declarationVisitor;

    private Messager messager;

    public FacadeAnnotationProcessor( AnnotationProcessorEnvironment env ) {
        environment = env;
        messager = environment.getMessager();
        facade = environment.getTypeDeclaration( "org.randombits.facade.Facadable" );
        arrayTypeParameter = environment.getTypeDeclaration( "org.randombits.facade.ArrayTypeParameter" );
        declarationVisitor = new FacadedDeclarationsVisitor();
    }

    public void process() {
        Collection<TypeDeclaration> declarations = environment.getTypeDeclarations();
        DeclarationVisitor scanner = DeclarationVisitors.getSourceOrderDeclarationScanner( declarationVisitor,
                DeclarationVisitors.NO_OP );
        for ( TypeDeclaration declaration : declarations ) {
            // invoke the processing on the scanner.
            declaration.accept( scanner );
        }

    }

    private class FacadedDeclarationsVisitor extends SimpleDeclarationVisitor {

        @Override public void visitMethodDeclaration( MethodDeclaration method ) {
            Collection<AnnotationMirror> annotations = method.getAnnotationMirrors();
            for ( AnnotationMirror annotation : annotations ) {
                // Check the annotation type.
                AnnotationTypeDeclaration annotationTypeDecl = annotation.getAnnotationType().getDeclaration();
                if ( annotationTypeDecl.equals( facade ) ) {
                    // Check Facadable rules
                    checkTypeIsFacadable( method.getReturnType(), annotation.getPosition() );
                } else if ( annotationTypeDecl.equals( arrayTypeParameter ) ) {
                    TypeVariable componentType = null;
                    if ( method.getReturnType() instanceof ArrayType ) {
                        ArrayType arrayType = ( ArrayType ) method.getReturnType();
                        if ( arrayType.getComponentType() instanceof TypeVariable ) {
                            componentType = ( TypeVariable ) arrayType.getComponentType();
                        } else {
                            messager
                                    .printError( annotation.getPosition(),
                                            "@ArrayTypeParameter may only be applied on methods which return a generic array." );
                        }
                    } else {
                        messager
                                .printError( annotation.getPosition(),
                                        "@ArrayTypeParameter may only be applied on methods which return a generic array." );
                    }

                    Integer index = null;
                    ParameterDeclaration[] params = method.getParameters().toArray( new ParameterDeclaration[0] );
                    // Find the parameter index value.
                    for ( Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : annotation
                            .getElementValues().entrySet() ) {
                        AnnotationTypeElementDeclaration annotationMethod = entry.getKey();
                        if ( "value".equals( annotationMethod.getSimpleName() ) ) {
                            AnnotationValue annotationValue = entry.getValue();
                            Integer value = ( Integer ) annotationValue.getValue();

                            if ( value < 0 ) {
                                messager.printError( annotation.getPosition(),
                                        "Array type parameter index must be 0 or greater but was " + value + "." );
                            } else if ( value >= params.length ) {
                                messager.printError( annotation.getPosition(),
                                        "Array type parameter index must be less than " + params.length
                                                + " but was " + value + "." );
                            } else {
                                index = value;
                            }
                        }
                    }

                    if ( componentType != null && index != null ) {
                        // Check the parameter type is compatible with the
                        // return type
                        ParameterDeclaration param = params[index];
                        TypeMirror paramType = param.getType();
                        // May be one of the following types:
                        boolean valid = false;
                        if ( componentType.equals( paramType ) ) {
                            // 1. The actual type variable.
                            valid = true;
                        } else if ( paramType instanceof ArrayType ) {
                            // 2. An array of the type variable.
                            valid = componentType.equals( ( ( ArrayType ) paramType ).getComponentType() );
                        } else if ( paramType instanceof DeclaredType ) {
                            // 3. The Class of the type variable.
                            DeclaredType declared = ( DeclaredType ) paramType;
                            if ( CLASS_NAME.equals( declared.getDeclaration().getQualifiedName() ) ) {
                                Collection<TypeMirror> types = declared.getActualTypeArguments();
                                valid = types.size() == 1 && componentType.equals( types.iterator().next() );
                            }
                        }

                        if ( !valid )
                            messager.printError( param.getPosition(),
                                    "When specified by @ArrayTypeParameter the '" + param.getType() + " "
                                            + param.getSimpleName() + "' parameter must one of the following: "
                                            + componentType + ", Class<" + componentType + ">, or "
                                            + componentType + "[]." );
                    }

                }
            }
        }

        @Override public void visitParameterDeclaration( ParameterDeclaration declaration ) {
            Collection<AnnotationMirror> annotations = declaration.getAnnotationMirrors();
            for ( AnnotationMirror annotation : annotations ) {
                // Check the annotation type.
                if ( annotation.getAnnotationType().getDeclaration().equals( facade ) ) {
                    checkTypeIsFacadable( declaration.getType(), declaration.getPosition() );
                }
            }
        }

        private void checkTypeIsFacadable( TypeMirror type, SourcePosition position ) {
            if ( type.equals( environment.getTypeUtils().getVoidType() ) ) {
                // Annotating with Facadable on void methods is an error.
                messager.printError( position, "Methods returning void cannot be marked as @Facadable" );
            } else if ( type instanceof PrimitiveType ) {
                // Annotating a primitive parameter with Facadable is an error.
                messager.printError( position, "Primitive types cannot be marked as @Facadable" );
            }
        }

    }

}
