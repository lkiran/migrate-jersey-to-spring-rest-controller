package com.github.lkiran.recipes;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

public class ReplacePutAndPath extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace @PUT and @Path with @PutMapping";
    }

    @Override
    public String getDescription() {
        return "Replace @PUT and @Path(\"xxx\") with @PutMapping(\"xxx\") where \"xxx\" can be any value.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ReplacePutAndPathVisitor();
    }

    private static class ReplacePutAndPathVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = (J.MethodDeclaration) super.visitMethodDeclaration(method, ctx);
            var oldMethodAnnotation = m.getLeadingAnnotations().stream().filter(
                a -> a.getAnnotationType().getType().toString().equals("jakarta.ws.rs.PUT")).findFirst();
            if (oldMethodAnnotation.isEmpty()) {
                return m;
            }

            var pathAnnotation = m.getLeadingAnnotations().stream().filter(
                a -> a.getAnnotationType().getType().toString().equals("jakarta.ws.rs.Path")).findFirst();

            return pathAnnotation //
                .map(annotation -> methodWithPath(annotation, m, oldMethodAnnotation.get())) //
                .orElseGet(() -> methodWithoutPath(oldMethodAnnotation.get(), m));
        }

        private J.@NotNull MethodDeclaration methodWithoutPath(J.Annotation oldMethodAnnotation,
            J.MethodDeclaration m) {
            var getMappingAnnotation = oldMethodAnnotation.withAnnotationType(
                new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "PutMapping",
                    JavaType.buildType("org.springframework.web.bind.annotation.PutMapping"), null));
            m.getLeadingAnnotations().remove(oldMethodAnnotation);
            m.getLeadingAnnotations().add(getMappingAnnotation);
            m = m.withLeadingAnnotations(m.getLeadingAnnotations());

            maybeRemoveImport(TypeUtils.asFullyQualified(oldMethodAnnotation.getType()));
            maybeAddImport(TypeUtils.asFullyQualified(getMappingAnnotation.getType()));

            return m;
        }

        private J.@NotNull MethodDeclaration methodWithPath(J.Annotation pathAnnotation, J.MethodDeclaration m,
            J.Annotation oldMethodAnnotation) {
            var getMappingAnnotation = pathAnnotation.withAnnotationType(
                new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "PutMapping",
                    JavaType.buildType("org.springframework.web.bind.annotation.PutMapping"), null));

            m.getLeadingAnnotations().add(getMappingAnnotation);
            m.getLeadingAnnotations().remove(oldMethodAnnotation);
            m.getLeadingAnnotations().remove(pathAnnotation);
            m = m.withLeadingAnnotations(m.getLeadingAnnotations());

            maybeRemoveImport(TypeUtils.asFullyQualified(pathAnnotation.getType()));
            maybeRemoveImport(TypeUtils.asFullyQualified(oldMethodAnnotation.getType()));
            maybeAddImport(TypeUtils.asFullyQualified(getMappingAnnotation.getType()));

            return m;
        }
    }
}
