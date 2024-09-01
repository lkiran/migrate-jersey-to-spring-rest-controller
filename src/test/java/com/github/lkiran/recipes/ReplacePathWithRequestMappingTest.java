package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ReplacePathWithRequestMappingTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplacePathWithRequestMapping());
    }



    @Test
    void annotate_hasControllerAnnotation_shouldRemove() {
        rewriteRun(java("""
            package com.example;
            
            import org.springframework.web.bind.annotation.RestController;
            import jakarta.ws.rs.Path;
            
            @RestController
            @Path("/example")
            public class Test {
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            @RequestMapping("/example")
            public class Test {
                public void example() {}
            }"""));
    }
}
