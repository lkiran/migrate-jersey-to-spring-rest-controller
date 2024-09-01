package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ReplacePathParamWithPathVariableTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplacePathParamWithPathVariable());
    }

    @Test
    void replace_noMethodParam_shouldNotChange() {
        rewriteRun(java("""
            package com.example;
                        
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping("/example")
                public void example() {}
            }"""));
    }

    @Test
    void replace_methodHasGetMappingAnnotation_shouldReplace() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.PathParam;
            import org.springframework.web.bind.annotation.GetMapping;
            import jakarta.validation.constraints.NotNull;

            public class Test {
                @GetMapping("/example/{id}")
                public void example(@PathParam("id") @NotNull String id) {}
            }""", """
            package com.example;
                       
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PathVariable;
            import jakarta.validation.constraints.NotNull;
                        
            public class Test {
                @GetMapping("/example/{id}")
                public void example(@PathVariable @NotNull String id) {}
            }"""));
    }

    @Test
    void replace_varibleNamingIsDifferent_shouldKeepParameter() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.PathParam;
            import org.springframework.web.bind.annotation.GetMapping;
            import jakarta.validation.constraints.NotNull;

            public class Test {
                @GetMapping("/example/{id}")
                public void example(@PathParam("id") @NotNull String uuid) {}
            }""", """
            package com.example;
                       
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PathVariable;
            import jakarta.validation.constraints.NotNull;
                        
            public class Test {
                @GetMapping("/example/{id}")
                public void example(@PathVariable("id") @NotNull String uuid) {}
            }"""));
    }
}
