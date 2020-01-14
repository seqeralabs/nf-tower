/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.nustaq.serialization.annotations.Version;

/**
 * This AST xform is needed to add `org.nustaq.serialization.annotations.Version` to a field
 * to a Groovy class indirectly.
 *
 * This is needed because the `Version` annotation requires a `byte` field which must be expressed
 * as an inline literal which is not supported by groovy FUCK!!
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("io.seqera.annotation.VerFST$FSTVerImpl")
public @interface VerFST {
    // annotation parameters
    int value();

    // --== annotation implementation ==--
    @GroovyASTTransformation(phase= CompilePhase.SEMANTIC_ANALYSIS)
    class FSTVerImpl implements ASTTransformation {

        @Override
        public void visit(ASTNode nodes[], SourceUnit source) {
            // the AST declaration itself
            AnnotationNode node = (AnnotationNode) nodes[0];
            // the target field to which add the `Version` annotation
            FieldNode target = (FieldNode) nodes[1];
            Expression expr = node.getMember("value");
            // add annotation
            AnnotationNode version = new AnnotationNode(new ClassNode(Version.class));
            Byte val = Byte.valueOf(expr.getText());
            version.setMember("value", new ConstantExpression(val,true));
            target.addAnnotation( version );
        }

    }
}
