package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class AnnotateWithRequestBodyTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AnnotateWithRequestBody());
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
            import org.springframework.web.bind.annotation.RequestParam;

            public class Test {
                @PostMapping("/example/{id}")
                public void example(@RequestParam String id, String body) {}
            }""", """
            package com.example;
                       
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestParam;
                        
            public class Test {
                @PostMapping("/example/{id}")
                public void example(@RequestParam String id, @RequestBody String body) {}
            }"""));
    }

    @Test
    void replace_methodHasNoBodyParameter_shouldNotAdd() {
        rewriteRun(java("""
            package com.example;
                        
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestParam;

            public class Test {
                @PostMapping("/example/{id}")
                public void example(@RequestParam String id) {}
            }"""));
    }

    @Test
    void replace_alreadyHaveAnnotation_shouldReplace() {
        rewriteRun(java("""
            package com.example;
                        
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestParam;
            import jakarta.validation.Valid;                          

            public class Test {
                @PostMapping("/example/{id}")
                public void example(@RequestParam String id, @Valid String body) {}
            }""", """
            package com.example;
                       
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestParam;
            import jakarta.validation.Valid;                         
                        
            public class Test {
                @PostMapping("/example/{id}")
                public void example(@RequestParam String id, @Valid @RequestBody String body) {}
            }"""));
    }
}
