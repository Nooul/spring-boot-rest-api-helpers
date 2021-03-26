# spring-boot-rest-api-helpers

A [Spring Boot](https://spring.io/projects/spring-boot) data provider for [React Admin](https://github.com/marmelab/react-admin).

## Spring Boot Installation

For now installation is done through JitPack, or compiling this project as a JAR and adding to your dependencies.

For **JitPack method** add the jitpack repository and the spring-boot-rest-api-helpers dependency to your pom.xml file:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.zifnab87</groupId>
    <artifactId>spring-boot-rest-api-helpers</artifactId>
    <version>LATEST_COMMIT_ID</version> <!-- ex. 3aa51f1  -->
</dependency>
```

For **JAR method** compile this project and install to your local repository:

```
mvn install -DskipTests=true
```

and add the dependency in your pom.xml dependencies:

```
<dependency>
    <groupId>com.nooul.apihelpers</groupId>
    <artifactId>spring-boot-rest-api-helpers</artifactId>
    <version>LATEST_VERSION_ID</version> <!-- ex. 0.7.0.RELEASE  -->
</dependency>
```

**Additional dependency**: if you add the dependecy without any plugin added you should also add openjson to your pom.xml

```
<dependency>
    <groupId>com.github.openjson</groupId>
    <artifactId>openjson</artifactId>
    <version>1.0.12</version>
</dependency>
```

## Sping Boot Usage

- Make a Entity
- Make a JPA repository as an extention of BaseRepository for the Entity
- To enable the filter capabilities you have to set the @RestController adding the FilterService, like below:

```java
@RestController
@RequestMapping("sample")
public class SampleController {

    @Autowired
    private SampleRepository repository;

    @Autowired
    private FilterService<SampleEntity, Integer> filterService;

    @GetMapping
    public Iterable<SampleEntity> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr,
            @RequestParam(required = false, name="sort") String sortStr) {
        QueryParamWrapper wrapper = QueryParamExtractor.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository);
    }
}
```

- Configure appplication.properties to use snake-case or camelCase for properties in API

```
spring-boot-rest-api-helpers.use-snake-case = false
```

## React-Admin Usage

- Write your own data provider accoring to the specificiation or use the raspingboot-dataprovider.js provided in the samplecode folder
- Make Resources accoring to your data provider

## Spring Boot / React-Admin sample

Take a look a the samplecode folder, you will find there a full CRUD Spring Boot generic controller, related Repository and Entity sample files, and a full CRUD data provider for React Admin and sample files

-**raspingboot-dataprovider.js**: React Admin dataprovider with all the feature implemented
- **GenericRAEntityController.java**: A generic Spring Boot controller tha can be used for CRUD operations

## References and filtering capabilities

Inspired by built-in fake REST data provider [react-admin](https://github.com/marmelab/react-admin) (see [documentation](https://marmelab.com/react-admin/DataProviders.html)) that queries like that:

```
GET /movies?filter={id: 1} //get movies by id = 1
GET /movies?filter={id: [1,2]} // get movies by id = 1 or id = 2
GET /actors?filter={movies: 1, firstName: 'John'} = //actors played in movie with id = 1 and their first name is 'John'
GET /actors?filter={birthYearGt: 1960}&sort=[id,DESC]&range=[0,100] // actors born later than 1960
GET /actors?filter={q: '%Keanu Re%'} // full text search on all text fields
GET /actors?sort=[firstName,DESC,birthDate,ASC] //sort by multiple fields in case of ties
```

More Inspiration was drawn from languages like [FIQL/RSQL](https://github.com/jirutka/rsql-parser) it is possible to also do the following (after url-encode of the query part of the url):

```
GET /movies?filter={idNot: 1} //get movies with id not equal to 1
GET /actors?filter={movies: null} = //actors that have played in no movie
GET /actors?filter={moviesNot: null} = //actors that have played to a movie
GET /actors?filter={movies: [1,2]} = //actors played in either movie with id = 1, or movie with id = 2
GET /actors?filter={moviesAnd: [1,2]} = //actors played in both movies with id = 1 and id = 2
GET /actors?filter={moviesNot: [1,2]} = //actors played in neither movie with id = 1, nor movie with id = 2
GET /actors?filter={name: Keanu Ree%} // full text search on specific fields just by the inclusion of one or two '%' n the value
GET /actors?filter={movies: {name: 'Matrix'}} = //actors that have played in movie with name 'Matrix'
GET /actors?filter={movies: {name: 'Matrix%'}} = //actors that have played in movies with name starting with 'Matrix'
GET /movies?filter={actors: {firstName: 'Keanu', lastNameNot: 'Reves'}} = //movies with actors that firstName is 'Keanu' ut lastName is not 'Reves'
GET /actors?filter=[{firstName: 'Keanu'},{firstName: 'John'}] = //actors with firstName  'Keanu' or 'John'
GET /actors?filter={firstName: ['Keanu', 'John']} = //equivalent to the above
GET /documents?filter={uuid: 'f44010c9-4d3c-45b2-bb6b-6cac8572bb78'} // get document with java.util.UUID equal to '44010c9-4d3c-45b2-bb6b-6cac8572bb78'
GET /libraries?filter={documents: {uuid: 'f44010c9-4d3c-45b2-bb6b-6cac8572bb78'}} // get libraries that contain document with uuid equal to 'f44010c9-4d3c-45b2-bb6b-6cac8572bb78'
GET /libraries?filter={documents: 'f44010c9-4d3c-45b2-bb6b-6cac8572bb78'} // same as above
GET /actors?filter={birthDateGt: '1960-01-01'}&sort=[id,DESC]&range=[0,100] // actors born later than 1960-01-01
GET /actors?filter={birthDateGt: '1960-01-01T00:00:00'}&sort=[id,DESC]&range=[0,100] // actors born later than 960-01-01 00:00:00 (database timezone - UTC recommended)

```

Some **keypoints** below:

- The key names are not the ones on the database but the ones exposed by the REST API and are the names of the entity attribute names.
- Keep in mind that key/value pairs that are in { } are combined by default with AND.

```
/actors?filter={firstName:'A',lastName:'B'} => firstName = 'A' and lastName = 'B'
```

- Values or Objects that contain key/values in [] are combined by default with OR unless the key in front of the [] is ending with 'And'.

```
/actors?filter={movies: [1,2]} => actors having acted at movies with ids 1 OR 2
/movies?filter={actors: [{firstName:'A'}, {lastName:'B'}] } => movies having actors with firstName = 'A' OR lastName = 'B'
/actors?filter={moviesAnd: [1,2]} => actors acted at movies with ids 1 AND 2
/movies?filter={actorsAnd: [{firstName:'A'}, {lastName:'B'}] } => movies having actors with firstName = 'A' AND lastName = 'B'
```

- Keep in mind that the object/array that is passed in filter needs to be url encoded for the request to work. Like the example provided below:

```
let filterObj = {movies: [1,2]};
fetch('/actors?filter=' + encodeURIComponent(JSON.stringify(filterObj)));
```

## License

MIT License

Copyright (c) 2017-present Michail Michailidis

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
