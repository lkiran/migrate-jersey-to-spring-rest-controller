package com.github.lkiran.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import com.github.lkiran.util.ImportHelper;

public class ConvertResponseToResponseEntity extends Recipe {

    @Override
    public @NotNull String getDisplayName() {
        return "Convert Response to ResponseEntity with inferred generic type";
    }

    @Override
    public @NotNull String getDescription() {
        return "This recipe refactors methods that return Response to return ResponseEntity with a generic type. "
               + "The generic type is inferred from the method's return statement, "
               + "ensuring compatibility with Spring's ResponseEntity "
               + "while preserving the original method's intent.";
    }

    @Override
    public @NotNull JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            private static final Set<String> HTTP_METHOD_ANNOTATIONS =
                Set.of("org.springframework.web.bind.annotation.GetMapping",
                    "org.springframework.web.bind.annotation.PostMapping",
                    "org.springframework.web.bind.annotation.PutMapping",
                    "org.springframework.web.bind.annotation.PatchMapping",
                    "org.springframework.web.bind.annotation.DeleteMapping");

            @Override
            public J.@NotNull MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                if (method.getReturnTypeExpression() == null //
                    || method.getReturnTypeExpression().getType() == null //
                    || !"jakarta.ws.rs.core.Response".equals(method.getReturnTypeExpression().getType().toString())
                    || "org.springframework.http.ResponseEntity".equals(
                    method.getReturnTypeExpression().getType().toString()) //
                    || method.getLeadingAnnotations().stream() //
                        .map(J.Annotation::getAnnotationType) //
                        .map(J.Identifier.class::cast) //
                        .map(J.Identifier::getType) //
                        .map(JavaType.Class.class::cast) //
                        .filter(Objects::nonNull) //
                        .map(JavaType.Class::getFullyQualifiedName) //
                        .noneMatch(HTTP_METHOD_ANNOTATIONS::contains)) {
                    return method;
                }

                var returnStatement = method.getBody().getStatements().stream() //
                    .filter(s -> s instanceof J.Return).findFirst();
                if (returnStatement.isEmpty()) {
                    return method;
                }

                Expression returnExp = ((J.Return) returnStatement.get()).getExpression();
                List<Expression> statementChain = new ArrayList<>();

                while (returnExp instanceof J.MethodInvocation curr) {
                    statementChain.add(curr);
                    returnExp = curr.getSelect();
                }
                if (returnExp instanceof J.Identifier identifier) {
                    statementChain.add(identifier);
                }

                var entityInvocation = statementChain.stream() //
                    .filter(s -> s instanceof J.MethodInvocation) //
                    .filter(s -> Set.of("ok", "entity").contains(((J.MethodInvocation) s).getSimpleName())).map(
                        J.MethodInvocation.class::cast) //
                    .findFirst();

                var inferredGenericType = entityInvocation.map(ei -> ei.getArguments().getFirst().getType()).orElse(
                    JavaType.buildType("java.lang.Void"));
                var newReturnType = new JavaType.Parameterized(null,
                    (JavaType.FullyQualified) JavaType.buildType("org.springframework.http.ResponseEntity"),
                    List.of(inferredGenericType));

                method = method //
                    .withReturnTypeExpression(((J.Identifier) method.getReturnTypeExpression()) //
                        .withSimpleName("ResponseEntity<%s>".formatted(
                            ImportHelper.getSimpleName(inferredGenericType.toString()))) //
                        .withType(newReturnType));

                maybeRemoveImport("jakarta.ws.rs.core.Response");
                maybeAddImport("org.springframework.http.ResponseEntity");
                ImportHelper.getImports(inferredGenericType.toString()) //
                    .forEach(i -> maybeAddImport(i, false));

                return method;
            }
        };
    }
}
