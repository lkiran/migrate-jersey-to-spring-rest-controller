package com.github.lkiran.recipes;

import java.util.List;
import java.util.UUID;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class ReplacePathWithRequestMapping extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace @Path with @RequestMapping";
    }

    @Override
    public String getDescription() {
        return "Replaces @Path with @RequestMapping by keeping attributes as is.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration c, ExecutionContext context) {
                var restControllerAnnotation = c.getLeadingAnnotations().stream().filter(
                    a -> a.getAnnotationType().getType().toString().equals(
                        "org.springframework.web.bind.annotation.RestController")).findFirst();
                if (restControllerAnnotation.isEmpty()) {
                    return c;
                }
                var pathAnnotation = c.getLeadingAnnotations().stream().filter(
                    a -> a.getAnnotationType().getType().toString().equals("jakarta.ws.rs.Path")).findFirst();

                if (pathAnnotation.isEmpty()) {
                    return c;
                }

                var requestMappingAnnotation = pathAnnotation.get().withAnnotationType(
                    new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "RequestMapping",
                        JavaType.buildType("org.springframework.web.bind.annotation.RequestMapping"), null));

                c.getLeadingAnnotations().remove(pathAnnotation.get());
                c.getLeadingAnnotations().add(requestMappingAnnotation);
                c = c.withLeadingAnnotations(c.getLeadingAnnotations());
                maybeRemoveImport("jakarta.ws.rs.Path");
                maybeAddImport("org.springframework.web.bind.annotation.RequestMapping");

                return c;
            }
        };
    }
}
