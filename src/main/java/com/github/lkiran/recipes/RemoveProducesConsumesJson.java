package com.github.lkiran.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class RemoveProducesConsumesJson extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove @Produces and @Consumes";
    }

    @Override
    public String getDescription() {
        return "Removes @Produces and @Consumes annotations from classes annotated with @RestController if they are set to APPLICATION_JSON.";
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
                var producesAnnotation = c.getLeadingAnnotations().stream().filter(
                    a -> a.getAnnotationType().getType().toString().equals("jakarta.ws.rs.Produces")).findFirst();

                if (producesAnnotation.isPresent() && ("APPLICATION_JSON").equals(
                    ((J.FieldAccess) producesAnnotation.get().getArguments().getFirst()).getName().getSimpleName())) {

                    c.getLeadingAnnotations().remove(producesAnnotation.get());
                    c = c.withLeadingAnnotations(c.getLeadingAnnotations());
                    maybeRemoveImport("jakarta.ws.rs.Produces");
                }

                var consumesAnnotation = c.getLeadingAnnotations().stream().filter(
                    a -> a.getAnnotationType().getType().toString().equals("jakarta.ws.rs.Consumes")).findFirst();

                if (consumesAnnotation.isPresent() && ("APPLICATION_JSON").equals(
                    ((J.FieldAccess) consumesAnnotation.get().getArguments().getFirst()).getName().getSimpleName())) {
                    c.getLeadingAnnotations().remove(consumesAnnotation.get());
                    c = c.withLeadingAnnotations(c.getLeadingAnnotations());
                    maybeRemoveImport("jakarta.ws.rs.Consumes");
                }
                maybeRemoveImport("jakarta.ws.rs.core.MediaType");

                return c;
            }
        };
    }
}
