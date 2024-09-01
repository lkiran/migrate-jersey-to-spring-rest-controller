package com.github.lkiran.recipes;

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
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypedTree;
import org.openrewrite.marker.Markers;

public class ReplaceQueryParamWithRequestParam extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace @QueryParam with @RequestParam";
    }

    @Override
    public String getDescription() {
        return "Replaces @QueryParam annotations with @RequestParam annotations for methods in annotated with "
               + "@GetMapping, @PostMapping, @PutMapping, @PatchMapping, and @DeleteMapping. "
               + "Removes the value attribute if it's same as the method parameter name."
               + "Skips if @RequestParam annotation already exists.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ReplaceQueryParamWithRequestParamVisitor();
    }

    private static class ReplaceQueryParamWithRequestParamVisitor extends JavaVisitor<ExecutionContext> {

        private static final Set<String> HTTP_METHOD_ANNOTATIONS =
            Set.of("org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.PatchMapping",
                "org.springframework.web.bind.annotation.DeleteMapping");

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = (J.MethodDeclaration) super.visitMethodDeclaration(method, ctx);

            if (m.getLeadingAnnotations().stream().map(J.Annotation::getAnnotationType).map(TypedTree::getType).map(
                Object::toString).noneMatch(HTTP_METHOD_ANNOTATIONS::contains)) {
                return m;
            }

            if (m.getParameters().stream().filter(param -> param instanceof J.VariableDeclarations).map(
                param -> (J.VariableDeclarations) param).map(J.VariableDeclarations::getLeadingAnnotations).map(
                annotations -> annotations.stream() //
                    .anyMatch(ann -> "jakarta.ws.rs.QueryParam".equals(
                        ann.getAnnotationType().getType().toString()))).toList().stream().noneMatch(
                Boolean.TRUE::equals)) {
                return m;
            }

            var updatedParameters = m.getParameters().stream().map(param -> {
                var v = (J.VariableDeclarations) param;
                var updatedAnnotations = v.getLeadingAnnotations().stream().map(ann -> {
                    if (!"jakarta.ws.rs.QueryParam".equals(ann.getAnnotationType().getType().toString())) {
                        return ann;
                    }
                    if (ann.getAnnotationType().getType().equals(
                        "org.springframework.web.bind.annotation.RequestParam")) {
                        return ann;
                    }
                    var vp = new J.Identifier(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, List.of(), "RequestParam",
                        JavaType.buildType("org.springframework.web.bind.annotation.RequestParam"), null);
                    var isSameNaming = ann.getArguments().getFirst().toString().equals(
                        v.getVariables().getFirst().getName().getSimpleName());
                    ann = ann.withAnnotationType(vp).withArguments(isSameNaming ? List.of() : ann.getArguments());
                    return ann;
                }).toList();

                return (Statement) ((J.VariableDeclarations) param).withLeadingAnnotations(updatedAnnotations);
            }).toList();

            m = m.withParameters(updatedParameters);

            maybeAddImport("org.springframework.web.bind.annotation.RequestParam");
            maybeRemoveImport("jakarta.ws.rs.QueryParam");

            return m;
        }
    }
}
