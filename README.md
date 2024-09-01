# Description
These [Open Rewrite](https://docs.openrewrite.org) recipes will do the following changes to help you migrate from Jersey's controllers to Spring's Rest Controllers.  

at **class** level;
+ adds `@RestController`
+ removes `@Produces(MediaType.APPLICATION_JSON)`
+ removes `@Consumes(MediaType.APPLICATION_JSON)`
+ replaces`@Path` with `@RequestMapping`

at **method** level;
+ replaces `@GET` and `@Path` with `@GetMapping`
+ replaces `@POST` and `@Path` with `@PostMapping`
+ replaces `@PUT` and `@Path` with `@PutMapping`
+ replaces `@PATCH` and `@Path` with `@PatchMapping`
+ replaces `@DELETE` and `@Path` with `@DeleteMapping`
+ replaces `@QueryParam` with `@RequestParam` in method params
+ replaces `@PathParam` with `@PathVariable` in method params
+ replaces `@BeanParam` with `@ModelAttribute` in method params
+ adds `@RequestBody` to method params
+ refactors `Response` return types with `ResponseEntity`


# Usage
1. Clone this repository to your local. 
1. Execute maven clean install
1. Add this profile to your project pom.xml file
    ```xml
    <profiles>
      <profile>
        <id>openrewrite</id>
        <build>
          <plugins>
            <plugin>
              <groupId>org.openrewrite.maven</groupId>
              <artifactId>rewrite-maven-plugin</artifactId>
              <version>5.39.1</version>
              <configuration>
                <activeRecipes>
                  <recipe>com.github.lkiran.recipes.ReplaceDeleteAndPath</recipe>
                  <recipe>com.github.lkiran.recipes.ReplaceGetAndPath</recipe>
                  <recipe>com.github.lkiran.recipes.ReplacePatchAndPath</recipe>
                  <recipe>com.github.lkiran.recipes.ReplacePostAndPath</recipe>
                  <recipe>com.github.lkiran.recipes.ReplacePutAndPath</recipe>
                  <recipe>com.github.lkiran.recipes.AnnotateWithRestController</recipe>
                  <recipe>com.github.lkiran.recipes.RemoveProducesConsumesJson</recipe>
                  <recipe>com.github.lkiran.recipes.ReplacePathWithRequestMapping</recipe>
                  <recipe>com.github.lkiran.recipes.ReplacePathParamWithPathVariable</recipe>
                  <recipe>com.github.lkiran.recipes.ReplaceQueryParamWithRequestParam</recipe>
                  <recipe>com.github.lkiran.recipes.ReplaceBeanParamWithModelAttribute</recipe>
                  <recipe>com.github.lkiran.recipes.ReplaceHeaderParamWithRequestHeader</recipe>
                  <recipe>com.github.lkiran.recipes.AnnotateWithRequestBody</recipe>
                  <recipe>com.github.lkiran.recipes.ConvertResponseToResponseEntity</recipe>
                  <recipe>com.github.lkiran.recipes.ReturnResponseToResponseEntity</recipe>
                </activeRecipes>
                <failOnDryRunResults>true</failOnDryRunResults>
              </configuration>
              <dependencies>
                <dependency>
                  <groupId>org.openrewrite.recipe</groupId>
                  <artifactId>rewrite-static-analysis</artifactId>
                  <version>1.13.0</version>
                </dependency>
                <dependency>
                  <groupId>com.github.lkiran</groupId>
                  <artifactId>JerseyToRestController</artifactId>
                  <version>1.0-SNAPSHOT</version>
                </dependency>
              </dependencies>
            </plugin>
          </plugins>
        </build>
      </profile>
    </profiles>
    ```
1. Execute maven rewrite run in your project with this profile
1. Review the changes
1. Commit by reformating your project files
