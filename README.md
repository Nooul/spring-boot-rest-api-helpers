# spring-boot-rest-api-helpers

Inspired by built-in fake REST data provider [react-admin](https://github.com/marmelab/react-admin) (see [documentation](https://marmelab.com/react-admin/DataProviders.html)) that queries like that:
```    
    GET /api/v1/movies?filter={id: 1} //get by id = 1
    GET /api/v1/movies?filter={id: [1,2]} // get by id = 1 or id = 2
    GET /api/v1/actors?filter={movies: 1, firstName: John} = //actors played in movie with id = 1 and their first  name is John
    GET /api/v1/actors?filter={birthDateGt: 1960}&sort=[id,DESC]&range=[0,100] // actors born later than 1960
    GET /api/v1/actors?filter={q: Keanu Reeves%} // full text search on all text fields
```
More Inspiration was drawn from languages like [FIQL/RSQL](https://github.com/jirutka/rsql-parser) so recently more features were added along with in-memory integration tests, support for non-number primary keys,  resulting in a total refactoring of the code and fix of a lot of bugs (there are still some edge cases).

Now it is possible to also do the following (after url-encode of the query part of the url):
```
    GET /movies?filter={idNot: 1} //get by id != 1
    GET /actors?filter={movies: null} = //actors that have played in no movie
    GET /actors?filter={moviesNot: null} = //actors that have played to a movie
    GET /actors?filter={movies: [1,2]} = //actors played in either movie with id = 1, or movie with id = 2
    GET /actors?filter={moviesAnd: [1,2]} = //actors played in both movies with id = 1 and id = 2
    GET /actors?filter={moviesNot: [1,2]} = //actors played in neither movie with id = 1, nor movie with id = 2
    GET /actors?filter={name: Keanu Reeves%} // full text search on specific fields just by the inclusion of one or two '%' in the value
```
The key names are not the ones on the database but the ones exposed by the REST API and are the names of the entity attribute names. Here `movies` is plural because an Actor has `@ManyToMany` annotation on `List<Movie> movies` attribute.  
    
The above functionality is possible via this simple setup:
```java
@RestController
@RequestMapping("actors")
public class ActorController {

    @Autowired
    private ActorRepository repository;

    @Autowired
    FilterService<Actor, Long> filterService;

    @GetMapping
    public Iterable<Actor> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        FilterWrapper wrapper = filterService.extractFilterWrapper(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("firstName", "lastName"));
    }
}
```


The main important parts include:

- `@ControllerAdvices` that wrap Collections in objects {content: []) with paging and number of results information along with Status Codes based on Exceptions thrown.
- `BaseRepository` interface that needs to be extended by each of resource `Repositories`
- `CustomSpecifications` does all the magic of Criteria API query generation so that filtering and sorting works along with `FilterService` that provides some helper methods to the Controller code and helps provide convert the String query params to `FilterWrapper` so that it can be injected behind the scenes.
- `ObjectMapperProvider` that can be used by the Spring Boot Application in case serialization and deserialization need to work through fields instead of Getters and Setters

## Installation

For now installation is done through jitpack:

Add this in your pom.xml repositories:

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
        ...
    </repositories>

and add this as a dependency in your pom.xml dependencies:

        <dependency>
            <groupId>com.github.zifnab87</groupId>
            <artifactId>spring-boot-rest-api-helpers</artifactId>
            <version>edb1770</version> <!-- or latest short commit id -->
        </dependency>
        
## Usage

- Add springboot.rest package in the scanBasePackages at the top of your Spring Boot Application class
```java
@SpringBootApplication(scanBasePackages = {"com.myproject", "springboot.rest"})
```

- inject and expose as `@Bean` the provided `ObjectMapperProvider`

```java
    @Autowired
    private ObjectMapperProvider objMapperProvider;

    @Bean
    public ObjectMapper objectMapper() {
        return objMapperProvider.getObjectMapper();
    }
```
- configure appplication.properties to use snake-case or camelCase for properties in API
```
spring-boot-rest-api-helpers.use-snake-case = false
```
- for each of the Rest API resources create a class `XYZ` that is annotated with `@Entity`
- for each of the Rest API resources create an interface `XYZRepository` that extends `BaseRepository<XYZ,KeyType>`
- for each of the Rest API resources create a class `XYZController` annotated with `@RestController`

for more examples see/run the integration tests

## Previous Versions

This repo used to be called `react-admin-java-rest` and it was used to provide the needed building blocks for building a real backend API like that can give responses to the above requests in conjuction with react-admin/admin-on-rest (used here together: https://github.com/zifnab87/admin-on-rest-demo-java-rest). Since the time of their first incarnation, it seemed obvious that those API helpers were useful outside of the react-admin REST API realm, so the name `spring-boot-rest-api-helpers` was given.


## Fully working example (outdated)

For an example of how it can be used along admin-on-rest there is a fork of [admin-on-rest-demo](https://github.com/marmelab/admin-on-rest-demo)
that is fully working and uses [react-admin-java-rest](https://github.com/zifnab87/react-admin-java-rest)

Fully Working Fork of admin-on-rest-demo: [react-admin-demo-java-rest](https://github.com/zifnab87/react-admin-demo-java-rest)
