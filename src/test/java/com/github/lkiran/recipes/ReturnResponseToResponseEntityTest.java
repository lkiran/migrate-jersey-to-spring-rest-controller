package com.github.lkiran.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;


class ReturnResponseToResponseEntityTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReturnResponseToResponseEntity());
    }

    @Test
    void convert_returnsConstant_shouldChangeResponseType() {
        rewriteRun(java("""
            package com.example;

            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping
                public Response test() {
                    String campaignId = "123";
                    return Response.ok(campaignId).build();
                }
            }""", """
            package com.example;

            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping
                public Response test() {
                    String campaignId = "123";
                    return ResponseEntity.ok(campaignId);
                }
            }"""));
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
                public Response test() {
                    var service = new Service();
                    return ResponseEntity.ok(service.findAll());
                }
            }
            """));
    }

    @Test
    void convert_setsStatus_shouldReplaceWithBody() {
        rewriteRun(java("""
            package com.example;

            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping
                public Response test() {
                    String campaignId = "123";
                    return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
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
                public Response test() {
                    String campaignId = "123";
                    return ResponseEntity
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .body(campaignId);
                }
            }"""));
    }

//    @Test
//    void convert_4() {
//        rewriteRun(java("""
//            package com.example;
//
//            import jakarta.ws.rs.core.Response;
//
//            public class Test {
//
//                public Response test() {
//                    String campaignId = "123";
//                    return Response.ok(campaignId, "OCTET_STREAM")
//                        .header("content-disposition", "attachment; filename= export_vehicles.xlsx")
//                        .build();
//                }
//            }""", """
//            package com.example;
//
//            import jakarta.ws.rs.core.Response;
//            import org.springframework.http.ResponseEntity;
//
//            public class Test {
//
//                public Response test() {
//                    String campaignId = "123";
//                    return ResponseEntity.ok()
//                        .header("content-disposition", "attachment; filename= export_vehicles.xlsx")
//                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                        .body(campaignId);
//                }
//            }"""));
//    }

    @Test
    void convert_5() {
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
                public Response test() {
                    return ResponseEntity.noContent().build();
                }
            }"""));
    }

    @Test
    void convert_6() {
        rewriteRun(java("""
            package com.example;

            import jakarta.ws.rs.core.Response;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping
                public Response test() {
                    return Response.ok().build();
                }
            }""", """
            package com.example;

            import jakarta.ws.rs.core.Response;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.GetMapping;

            public class Test {
                @GetMapping
                public Response test() {
                    return ResponseEntity.ok().build();
                }
            }"""));
    }


    @Test
    void convert_7() {
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
                    return service.findAll();
                }
            }
            """));
    }

}
