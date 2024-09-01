package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class AnnotateWithRestControllerTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AnnotateWithRestController());
    }

    @Test
    void annotate() {
        rewriteRun(java("""
            package com.example;
            
            import jakarta.ws.rs.Path;
           
            @Path("/example")
            public class Test {
                public void example() {}
            }""", """
            package com.example;
            
            import jakarta.ws.rs.Path;
            import org.springframework.web.bind.annotation.RestController;
           
            @Path("/example")
            @RestController
            public class Test {
                public void example() {}
            }"""));
    }

    @Test
    void annotate_hasControllerAnnotation_shouldRemove() {
        rewriteRun(java("""
            package com.example;
            
            import org.springframework.stereotype.Controller;
            import jakarta.ws.rs.Path;
            
            @Controller
            @Path("/example")
            public class Test {
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.RestController;
            import jakarta.ws.rs.Path;
            
            
           
            @Path("/example")
            @RestController
            public class Test {
                public void example() {}
            }"""));
    }
}
