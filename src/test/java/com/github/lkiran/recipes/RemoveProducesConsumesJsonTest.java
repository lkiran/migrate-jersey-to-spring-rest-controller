package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class RemoveProducesConsumesJsonTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemoveProducesConsumesJson());
    }
    @Test
    void removeProduces() {
        rewriteRun(java("""
            package com.example;
           
            import jakarta.ws.rs.Produces;
            import jakarta.ws.rs.core.MediaType;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            @Produces(MediaType.APPLICATION_JSON)
            public class Test {
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.RestController;
            
            @RestController
            public class Test {
                public void example() {}
            }"""));
    }

    @Test
    void removeConsumes() {
        rewriteRun(java("""
            package com.example;
           
            import jakarta.ws.rs.Consumes;
            import jakarta.ws.rs.core.MediaType;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            @Consumes(MediaType.APPLICATION_JSON)
            public class Test {
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.RestController;
            
            @RestController
            public class Test {
                public void example() {}
            }"""));
    }
}
