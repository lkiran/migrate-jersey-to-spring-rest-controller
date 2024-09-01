package com.github.lkiran.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

public class ReturnResponseToResponseEntity extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert Response to ResponseEntity with generic type";
    }

    @Override
    public String getDescription() {
        return "Changes methods returning `Response` to `ResponseEntity` with a generic type, inferred from the service method's return type.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            private static final Set<String> HTTP_METHOD_ANNOTATIONS =
                Set.of("org.springframework.web.bind.annotation.GetMapping",
                    "org.springframework.web.bind.annotation.PostMapping",
                    "org.springframework.web.bind.annotation.PutMapping",
                    "org.springframework.web.bind.annotation.PatchMapping",
                    "org.springframework.web.bind.annotation.DeleteMapping");

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                if (method.getLeadingAnnotations().stream() //
                    .map(J.Annotation::getAnnotationType) //
                    .map(J.Identifier.class::cast) //
                    .map(J.Identifier::getType) //
                    .map(JavaType.Class.class::cast) //
                    .filter(Objects::nonNull) //
                    .map(JavaType.Class::getFullyQualifiedName) //
                    .noneMatch(HTTP_METHOD_ANNOTATIONS::contains)) {
                    return method;
                }

                var body = method.getBody();
                var statements = body.getStatements();
                var returnStatement = statements.stream().filter(s -> s instanceof J.Return).findFirst();
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

                if ("org.springframework.http.ResponseEntity".equals(returnExp.getType().toString())
                    || !"jakarta.ws.rs.core.Response".equals(statementChain.getLast().getType().toString())) {
                    return method;
                }
                var setResponseBodyInvocation = statementChain.stream() //
                    .filter(s -> s instanceof J.MethodInvocation) //
                    .filter(s -> Set.of("ok", "entity").contains(((J.MethodInvocation) s).getSimpleName())).map(
                        J.MethodInvocation.class::cast) //
                    .findFirst();
                if (setResponseBodyInvocation.isPresent() //
                    && setResponseBodyInvocation.get().getArguments().stream().noneMatch(a -> a instanceof J.Empty)) {
                    statementChain.stream() //
                        .filter(s -> s instanceof J.MethodInvocation) //
                        .filter(s -> "build".equals(((J.MethodInvocation) s).getSimpleName())) //
                        .findFirst() //
                        .ifPresent(statementChain::remove);
                }
                var entityInvocation = statementChain.stream() //
                    .filter(s -> s instanceof J.MethodInvocation) //
                    .filter(s -> "entity".equals(((J.MethodInvocation) s).getSimpleName())).map(
                        J.MethodInvocation.class::cast) //
                    .findFirst();
                if (entityInvocation.isPresent()) {
                    var bodyInvocation =
                        entityInvocation.get().withName(entityInvocation.get().getName().withSimpleName("body"));
                    statementChain.set(statementChain.indexOf(entityInvocation.get()), bodyInvocation);
                }
                // Update the last identifier to "ResponseEntity"
                statementChain.set(statementChain.size() - 1,
                    ((J.Identifier) statementChain.get(statementChain.size() - 1)).withSimpleName(
                        "ResponseEntity").withType(JavaType.buildType("org.springframework.http.ResponseEntity")));
                // Rebuild the method invocation chain
                Expression updatedReturnExp = statementChain.get(statementChain.size() - 1);
                for (int i = statementChain.size() - 2; i >= 0; i--) {
                    updatedReturnExp = ((J.MethodInvocation) statementChain.get(i)).withSelect(updatedReturnExp);
                }

                // Create a new return statement with the updated expression
                J.Return updatedReturnStatement = ((J.Return) returnStatement.get()) //
                    .withExpression(updatedReturnExp.withPrefix(Space.SINGLE_SPACE));

                // Replace the old return statement with the new one
                List<Statement> updatedStatements = new ArrayList<>(statements);
                updatedStatements.set(updatedStatements.indexOf(returnStatement.get()), updatedReturnStatement);

                method = method.withBody(body.withStatements(updatedStatements));

                maybeRemoveImport("jakarta.ws.rs.core.Response");
                maybeAddImport("org.springframework.http.ResponseEntity");

                return method;
            }
        };
    }
}
