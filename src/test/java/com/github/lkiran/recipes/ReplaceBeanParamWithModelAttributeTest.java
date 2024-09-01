package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ReplaceBeanParamWithModelAttributeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplaceBeanParamWithModelAttribute());
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
                        
            import org.springframework.web.bind.annotation.PostMapping;
            import jakarta.ws.rs.BeanParam;

            public class Test {
                @PostMapping("/example/{id}")
                public void example(@BeanParam Object bean) {}
            }""", """
            package com.example;
                       
            import org.springframework.web.bind.annotation.ModelAttribute;
            import org.springframework.web.bind.annotation.PostMapping;
                        
            public class Test {
                @PostMapping("/example/{id}")
                public void example(@ModelAttribute Object bean) {}
            }"""));
    }
}
