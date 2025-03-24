# spring-boot-rest-api-helpers

Java >= 8 (thanks to [davidegironi](https://github.com/davidegironi/))

Inspired by built-in fake REST data provider [react-admin](https://github.com/marmelab/react-admin) (see [documentation](https://marmelab.com/react-admin/DataProviders.html)) that queries like that:
```    
    GET /movies?filter={id: 1} //get movies by id = 1
    GET /movies?filter={id: [1,2]} // get movies by id = 1 or id = 2
    GET /actors?filter={movies: 1, firstName: John} = //actors played in movie with id = 1 and their first  name is John
    GET /actors?filter={birthYearGt: 1960}&sort=[id,DESC]&range=[0,100] // actors born later than 1960
    GET /actors?filter={q: %Keanu Re%} // full text search on all text fields
    GET /actors?sort=[firstName,DESC,birthDate,ASC] //sort by multiple fields in case of ties
```
More Inspiration was drawn from languages like [FIQL/RSQL](https://github.com/jirutka/rsql-parser) so recently more features were added along with in-memory integration tests, support for non-number primary keys,  resulting in a total refactoring of the code and fix of a lot of bugs (there are still some edge cases).

Now it is possible to also do the following (after url-encode of the query part of the url):
```
    GET /movies?filter={idNot: 1} //get movies with id not equal to 1
    GET /actors?filter={movies: null} = //actors that have played in no movie
    GET /actors?filter={moviesNot: null} = //actors that have played to a movie
    GET /actors?filter={movies: [1,2]} = //actors played in either movie with id = 1, or movie with id = 2
    GET /actors?filter={moviesAnd: [1,2]} = //actors played in both movies with id = 1 and id = 2
    GET /actors?filter={moviesNot: [1,2]} = //actors played in neither movie with id = 1, nor movie with id = 2
    GET /actors?filter={name: Keanu Ree%} // full text search on specific fields just by the inclusion of one or two '%' in the value

    GET /actors?filter={movies: {name: Matrix}} = //actors that have played in movie with name Matrix
    GET /actors?filter={movies: {name: Matrix%}} = //actors that have played in movies with name starting with Matrix
    GET /movies?filter={actors: {firstName: Keanu, lastNameNot: Reves}} = //movies with actors that firstName is 'Keanu' but lastName is not 'Reves'

    GET /actors?filter=[{firstName: Keanu},{firstName: John}] = //actors with firstName  'Keanu' or 'John'
    GET /actors?filter={firstName: [Keanu, John]} = //equivalent to the above
    
    GET /documents?filter={uuid: f44010c9-4d3c-45b2-bb6b-6cac8572bb78} // get document with java.util.UUID equal to f44010c9-4d3c-45b2-bb6b-6cac8572bb78
    GET /libraries?filter={documents: {uuid: f44010c9-4d3c-45b2-bb6b-6cac8572bb78}} // get libraries that contain document with uuid equal to f44010c9-4d3c-45b2-bb6b-6cac8572bb78
    GET /libraries?filter={documents: f44010c9-4d3c-45b2-bb6b-6cac8572bb78} // same as above

    GET /actors?filter={birthDateGt: '1960-01-01'}&sort=[id,DESC]&range=[0,100] // actors born later than 1960-01-01
    GET /actors?filter={birthDateGt: '1960-01-01T00:00:00'}&sort=[id,DESC]&range=[0,100] // actors born later than 1960-01-01 00:00:00 (database timezone - UTC recommended)

```
The key names are not the ones on the database but the ones exposed by the REST API and are the names of the entity attribute names. Here `movies` is plural because an Actor has `@ManyToMany` annotation on `List<Movie> movies` attribute. 

* Keep in mind that key/value pairs that are in { } are combined by default with AND.
```
/actors?filter={firstName:'A',lastName:'B'} => firstName = A and lastName = B
```

* Values or Objects that contain key/values in [] are combined by default with OR unless the key in front of the [] is ending with 'And'.
```
/actors?filter={movies: [1,2]} => actors having acted at movies with ids 1 OR 2 
/movies?filter={actors: [{firstName:'A'}, {lastName:'B'}] } => movies having actors with firstName = A OR lastName = B
/actors?filter={moviesAnd: [1,2]} => actors acted at movies with ids 1 AND 2 
/movies?filter={actorsAnd: [{firstName:'A'}, {lastName:'B'}] } => movies having actors with firstName = A AND lastName = B
```

* Disabling distinct search can have some performance boost sometimes - **Warning it will return duplicate entries**
```
/actors?filter={movies: 1, firstName: John}
```
allowDuplicates is not supported after Hibernate 6 since it always passes distinct:true!

https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html#query-sqm-distinct
~~/actors?filter={movies: 1, firstName: John, allowDuplicates: true}~~


**Important**: Keep in mind that the object/array that is passed in filter needs to be url encoded for the request to work. E.g in Javascript someone would use `encodeURIComponent()` like that 
```
let filterObj = {movies: [1,2]};
fetch('/actors?filter=' + encodeURIComponent(JSON.stringify(filterObj)));
```

The above functionality is possible via this simple setup:
```java
@RestController
@RequestMapping("actors")
public class ActorController {

    @Autowired
    private ActorRepository repository;

    @Autowired
    private FilterService<Actor, Long> filterService;

    @GetMapping
    public Iterable<Actor> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, 
            @RequestParam(required = false, name="sort") String sortStr) {

        QueryParamWrapper wrapper = QueryParamExtractor.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("firstName", "lastName"));
    }
}
```


The main important parts include:

- `@ControllerAdvice`s that wrap Collections in objects {content: []) with paging and number of results information along with Status Codes based on Exceptions thrown and returns 404 in case of null returned from endpoints.
- `BaseRepository` interface that needs to be extended by each of resource `Repositories`
- `CustomSpecifications` does all the magic of Criteria API query generation so that filtering and sorting works along with `FilterService` that provides some helper methods to the Controller code and helps provide convert the String query params to `FilterWrapper` so that it can be injected behind the scenes.
- `ObjectMapperProvider` that can be used by the Spring Boot Application in case serialization and deserialization need to work through fields instead of Getters and Setters
- you need to create classes annotated with `@ControllerAdvice` and extend the appropriate classes under package `springboot.rest.controllerAdvices` if needed in your project


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
@SpringBootApplication(scanBasePackages = {"com.myproject", springbootrest});
```
- configure application.properties to use snake-case or camelCase for properties in API
```
spring-boot-rest-api-helpers.use-snake-case = false
```
- for each of the Rest API resources create a class `XYZ` that is annotated with `@Entity`
- for each of the Rest API resources create an interface `XYZRepository` that extends `BaseRepository<XYZ,KeyType>`
- for each of the Rest API resources create a class `XYZController` annotated with `@RestController`
- for each of Value object annotate them with with `com.nooul.apihelpers.springbootrest.annotations.ValueObject`. See `Sender` with `Mobile` and `MobileConverter` in test helpers. They should behave like plain strings. No comparisons are supported with Gte/Lte/Gt/Lt yet

for more examples see/run the integration tests
*Note:* three-level join tests are failing and are not implemented yet - Any help towards an implementation that allows any number of depth for queries would be greatly appreciated :D

## Previous Versions

This repo used to be called `react-admin-java-rest` and it was used to provide the needed building blocks for building a real backend API like that can give responses to the above requests in conjunction with react-admin/admin-on-rest (used here together: https://github.com/zifnab87/admin-on-rest-demo-java-rest). Since the time of their first incarnation, it seemed obvious that those API helpers were useful outside of the react-admin REST API realm, so the name `spring-boot-rest-api-helpers` was given.


## Fully working example (outdated)

For an example of how it can be used along admin-on-rest there is a fork of [admin-on-rest-demo](https://github.com/marmelab/admin-on-rest-demo)
that is fully working and uses [react-admin-java-rest](https://github.com/zifnab87/react-admin-java-rest)

Fully Working Fork of admin-on-rest-demo: [react-admin-demo-java-rest](https://github.com/zifnab87/react-admin-demo-java-rest)

## Release Notes
 - 0.9.0 - Support for Instant fields on Entities for date and date time range comparisons similar to Timestamp querying 
 - 0.10.0 - Support for Value Objects that can be used in search with `q`, exact match and search by null