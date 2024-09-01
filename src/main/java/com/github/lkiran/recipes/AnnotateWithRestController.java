package com.github.lkiran.recipes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.FindAnnotations;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class AnnotateWithRestController extends Recipe {

    @Override
    public String getDisplayName() {
        return "Annotate classes with @RestController where @Path exists";
    }

    @Override
    public String getDescription() {
        return "Adds @RestController to classes annotated with @Path and removes @Controller if it exists.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration c, ExecutionContext context) {
                // Check if the class has @Path annotation
                var pathAnnotation = FindAnnotations.find(c, "@jakarta.ws.rs.Path").stream().findFirst();

                if (pathAnnotation.isEmpty()) {
                    return c;
                }
                Set<J.Annotation> restControllerAnnotations =
                    FindAnnotations.find(c, "@org.springframework.web.bind.annotation.RestController");

                if (restControllerAnnotations.isEmpty()) {
                    var restControllerAnnotation = pathAnnotation.get() //
                        .withPrefix(Space.format("\n")) //
                        .withMarkers(Markers.EMPTY) //
                        .withArguments(List.of()) //
                        .withAnnotationType(
                            new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "RestController",
                                JavaType.buildType("org.springframework.web.bind.annotation.RestController"), null));
                    c.getLeadingAnnotations().add(restControllerAnnotation);
                    c = c.withLeadingAnnotations(c.getLeadingAnnotations());
                    maybeAddImport("org.springframework.web.bind.annotation.RestController");
                }

                var controllerAnnotation = c.getLeadingAnnotations().stream().filter(
                    a -> a.getAnnotationType().getType().toString().equals(
                        "org.springframework.stereotype.Controller")).findFirst();
                if (controllerAnnotation.isEmpty()) {
                    return c;
                }
                c.getLeadingAnnotations().remove(controllerAnnotation.get());
                c = c.withLeadingAnnotations(c.getLeadingAnnotations());
                maybeRemoveImport("org.springframework.stereotype.Controller");

                return c;
            }
        };
    }
}
