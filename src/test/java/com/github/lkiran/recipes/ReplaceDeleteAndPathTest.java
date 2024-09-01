package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ReplaceDeleteAndPathTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplaceDeleteAndPath());
    }

    @Test
    void replace_withPathAnnotation_shouldRemovePAthAndAddPathMappingAnnotation() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.DELETE;
            import jakarta.ws.rs.Path;
                           
            public class Test {
                
                @DELETE
                @Path("/example")
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.DeleteMapping;
            
            public class Test {
            
               \s
                @DeleteMapping("/example")
                public void example() {}
            }"""));
    }

    @Test
    void replace_withoutPathAnnotation_shouldReplaceWithDeleteMappingAnnotation() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.DELETE;
                           
            public class Test {
                
                @DELETE
                public void example() {}
            }""", """
            package com.example;
            
            import org.springframework.web.bind.annotation.DeleteMapping;
            
            public class Test {
            
                @DeleteMapping
                public void example() {}
            }"""));
    }
}
