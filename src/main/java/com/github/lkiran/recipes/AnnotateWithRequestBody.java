package com.github.lkiran.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypedTree;
import org.openrewrite.marker.Markers;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnotateWithRequestBody extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add @RequestBody annotation";
    }

    @Override
    public String getDescription() {
        return "Add @RequestBody annotation to method parameters in methods annotated with "
               + "@PostMapping, @PutMapping, @PatchMapping, and @DeleteMapping.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AnnotateWithRequestBodyVisitor();
    }

    private static class AnnotateWithRequestBodyVisitor extends JavaVisitor<ExecutionContext> {

        private static final Set<String> SKIP_FOR_HAVING_THESE_ANNOTATIONS = Set.of("jakarta.ws.rs.BeanParam", //
            "org.springframework.web.bind.annotation.RequestBody",
            "org.springframework.web.bind.annotation.RequestHeader",
            "org.springframework.web.bind.annotation.ModelAttribute",
            "org.springframework.web.bind.annotation.RequestParam",
            "org.springframework.web.bind.annotation.PathVariable");
        private static final Set<String> HTTP_METHOD_ANNOTATIONS =
            Set.of("org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.PatchMapping",
                "org.springframework.web.bind.annotation.DeleteMapping");

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = (J.MethodDeclaration) super.visitMethodDeclaration(method, ctx);

            if (m.getLeadingAnnotations().stream() //
                .map(J.Annotation::getAnnotationType) //
                .map(TypedTree::getType) //
                .map(Object::toString) //
                .noneMatch(HTTP_METHOD_ANNOTATIONS::contains)) {
                return m;
            }

            var updatedParameters = m.getParameters().stream().map(param -> {
                var v = (J.VariableDeclarations) param;
                if (v.getLeadingAnnotations().stream() //
                    .map(J.Annotation::getAnnotationType) //
                    .map(TypedTree::getType) //
                    .map(Object::toString) //
                    .anyMatch(SKIP_FOR_HAVING_THESE_ANNOTATIONS::contains)) {
                    return param;
                }

                var requestBodyAnnotation = new J.Annotation(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
                    new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "RequestBody",
                        JavaType.buildType("org.springframework.web.bind.annotation.RequestBody"), null), null);

                var updatedAnnotations =  new ArrayList<>(v.getLeadingAnnotations());
                if (CollectionUtils.isEmpty(updatedAnnotations)) {
                    return ((J.VariableDeclarations) param) //
                        .withTypeExpression(v.getTypeExpression().withPrefix(Space.format(" "))) //
                        .withLeadingAnnotations(List.of(requestBodyAnnotation));
                } else {
                    updatedAnnotations.add(requestBodyAnnotation //
                        .withPrefix(Space.format(" ")));
                    return ((J.VariableDeclarations) param) //
                        .withLeadingAnnotations(updatedAnnotations);
                }
            }).toList();

            m = m.withParameters(updatedParameters);

            maybeAddImport("org.springframework.web.bind.annotation.RequestBody");

            return m;
        }
    }
}
