package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ConvertResponseToResponseEntityTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertResponseToResponseEntity());
    }

    @Test
    void convert_returningServiceResult_shouldSetResponseType() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;
                        
            public class Service {
                public Service(){}
                public String findAll() {
                    return "findAll";
                }
            }
                           
            public class Test {
                @GetMapping
                public Response test() {
                    var service = new Service();
                    return Response.ok(service.findAll()).build();
                }
            }
            """, """
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;
                        
            public class Service {
                public Service(){}
                public String findAll() {
                    return "findAll";
                } 
            } 
                    
            public class Test {
                @GetMapping
                public ResponseEntity<String> test() {
                    var service = new Service();
                    return Response.ok(service.findAll()).build();
                }
            }
            """));
    }

    @Test
    void convert_returningGenericServiceResult_shouldSetResponseType() {
        rewriteRun(java("""
            package com.example;
                        
            import java.util.List;
            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;
                        
            public class Service {
                public Service(){}
                public List<String> findAll() {
                    return List.of("findAll");
                }
            }
                           
            public class Test {
                @GetMapping
                public Response test() {
                    var service = new Service();
                    return Response.ok(service.findAll()).build();
                }
            }
            """, """
            package com.example;
                        
            import java.util.List;
            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;
                        
            public class Service {
                public Service(){}
                public List<String> findAll() {
                    return List.of("findAll");
                } 
            } 
                    
            public class Test {
                @GetMapping
                public ResponseEntity<List<String>> test() {
                    var service = new Service();
                    return Response.ok(service.findAll()).build();
                }
            }
            """));
    }

    @Test
    void convert_shouldFindTypeFromEntityCall() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;
                           
            public class Test {
                
                @GetMapping
                public Response test() {
                    String campaignId = "123";
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(campaignId)
                        .build();
                }
            }""", """
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;
                           
            public class Test {
                
                @GetMapping
                public ResponseEntity<String> test() {
                    String campaignId = "123";
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(campaignId)
                        .build();
                }
            }"""));
    }

    @Test
    void convert_shouldTakeFirstParamType() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;
                           
            public class Test {
                
                @GetMapping
                public Response test() {
                    String campaignId = "123";
                    return Response.ok(campaignId, "OCTET_STREAM")
                        .header("content-disposition", "attachment; filename= export_vehicles.xlsx")
                        .build();
                }
            }""", """
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;
                           
            public class Test {
                
                @GetMapping
                public ResponseEntity<String> test() {
                    String campaignId = "123";
                    return Response.ok(campaignId, "OCTET_STREAM")
                        .header("content-disposition", "attachment; filename= export_vehicles.xlsx")
                        .build();
                }
            }"""));
    }

    @Test
    void convert_noContent_shouldSetReturnTypeVoid() {
        rewriteRun(java("""
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;
                           
            public class Test {
                
                @GetMapping
                public Response test() {
                    return Response.noContent().build();
                }
            }""", """
            package com.example;
                        
            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                
                @GetMapping
                public ResponseEntity<Void> test() {
                    return Response.noContent().build();
                }
            }"""));
    }
}
