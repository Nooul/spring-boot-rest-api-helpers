package com.nooul.apihelpers.springbootrest;

import static com.nooul.apihelpers.springbootrest.testapp.utils.DateUtils.timeStamp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.nooul.apihelpers.springbootrest.testapp.TestApp;
import com.nooul.apihelpers.springbootrest.testapp.controllers.ActorController;
import com.nooul.apihelpers.springbootrest.testapp.controllers.MovieController;
import com.nooul.apihelpers.springbootrest.testapp.controllers.UUIDEntityController;
import com.nooul.apihelpers.springbootrest.testapp.controllers.UUIDRelationshipController;
import com.nooul.apihelpers.springbootrest.testapp.entities.Actor;
import com.nooul.apihelpers.springbootrest.testapp.entities.Category;
import com.nooul.apihelpers.springbootrest.testapp.entities.Director;
import com.nooul.apihelpers.springbootrest.testapp.entities.Movie;
import com.nooul.apihelpers.springbootrest.testapp.entities.UUIDEntity;
import com.nooul.apihelpers.springbootrest.testapp.entities.UUIDRelationship;
import com.nooul.apihelpers.springbootrest.testapp.repositories.ActorRepository;
import com.nooul.apihelpers.springbootrest.testapp.repositories.CategoryRepository;
import com.nooul.apihelpers.springbootrest.testapp.repositories.DirectorRepository;
import com.nooul.apihelpers.springbootrest.testapp.repositories.MovieRepository;
import com.nooul.apihelpers.springbootrest.testapp.repositories.UUIDEntityRepository;
import com.nooul.apihelpers.springbootrest.testapp.repositories.UUIDRelationshipRepository;
import com.nooul.apihelpers.springbootrest.utils.UrlUtils;

import org.assertj.core.util.IterableUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = TestApp.class)
public class FilterServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private UUIDEntityRepository uuidEntityRepository;

    @Autowired
    private UUIDRelationshipRepository uuidRelationshipRepository;

    @Autowired
    private MovieController movieController;

    @Autowired
    private ActorController actorController;

    @Autowired
    private UUIDEntityController uuidEntityController;

    @Autowired
    private UUIDRelationshipController uuidRelationshipController;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void find_null_primitive_should_return() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDate(timeStamp("1999-01-05"));
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName(null);
        constantine.setReleaseDate(timeStamp("2005-01-05"));
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDate(timeStamp("2017-01-05"));
        movieRepository.save(it);

        Iterable<Movie> moviesWithNullName1 = movieController.filterBy("{name: 'null'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesWithNullName1));
        Iterable<Movie> moviesWithNullName2 = movieController.filterBy("{name: 'null'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesWithNullName2));
        Iterable<Movie> moviesWithNullName3 = movieController.filterBy("{name: ''}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesWithNullName3));
        Iterable<Movie> moviesWithNullName4 = movieController.filterBy("{name: '   '}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesWithNullName4));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void date_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDate(timeStamp("1999-01-05"));
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDate(timeStamp("2005-01-05"));
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDate(timeStamp("2017-01-05"));
        movieRepository.save(it);

        Iterable<Movie> moviesAfterOrOn2005 = movieController.filterBy("{releaseDateGte: '2005-01-01'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn2005));

        Iterable<Movie> moviesAfter2005 = movieController.filterBy("{releaseDateGt: '2005-01-01'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfter2005));

        Iterable<Movie> moviesBeforeOrOn2005 = movieController.filterBy("{releaseDateLte: '2005-01-1'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBeforeOrOn2005));

        Iterable<Movie> moviesBefore2005 = movieController.filterBy("{releaseDateLt: '2005-01-01'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBefore2005));

        Iterable<Movie> moviesAfter1999Before2017 = movieController
                .filterBy("{releaseDateGt: '1999-01-01', releaseDateLt: '2017-12-01'}", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(moviesAfter1999Before2017));

        Iterable<Movie> moviesAfter2005OrOnBefore2017OrOn = movieController
                .filterBy("{releaseDateGte: 2005-01-01, releaseDateLte:2017-12-01}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfter2005OrOnBefore2017OrOn));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void date_with_time_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDate(timeStamp("1999-01-05T01:00:00"));
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDate(timeStamp("1999-01-05T03:00:00"));
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDate(timeStamp("1999-01-05T04:00:00"));
        movieRepository.save(it);

        Iterable<Movie> moviesAfterOrOn1 = movieController.filterBy("{releaseDateGte: '1999-01-05T01:00:00'}", null,
                null);
        Assert.assertEquals(3, IterableUtil.sizeOf(moviesAfterOrOn1));

        Iterable<Movie> moviesAfter1 = movieController.filterBy("{releaseDateGt: '1999-01-05T01:00:00'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfter1));

        Iterable<Movie> moviesAfterOrOn3 = movieController.filterBy("{releaseDateGte: '1999-01-05T03:00:00'}", null,
                null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn3));

        Iterable<Movie> moviesAfter3 = movieController.filterBy("{releaseDateGt: '1999-01-05T03:00:00'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesAfter3));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void date_with_implied_zero_time_in_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDate(timeStamp("1999-01-05T01:00:00"));
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDate(timeStamp("1999-01-05T03:00:00"));
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDate(timeStamp("1999-01-06T01:00:00"));
        movieRepository.save(it);

        Movie it2 = new Movie();
        it2.setName("IT2");
        it2.setReleaseDate(timeStamp("1999-01-07T00:00:00"));
        movieRepository.save(it2);

        Iterable<Movie> moviesBeforeOrOnSixth = movieController.filterBy("{releaseDateLte: '1999-01-06'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesBeforeOrOnSixth));

        Iterable<Movie> moviesBeforeSixth = movieController.filterBy("{releaseDateLt: '1999-01-06'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesBeforeSixth));

        Iterable<Movie> moviesAfterOrOnSixth = movieController.filterBy("{releaseDateGte: '1999-01-06'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOnSixth));

        Iterable<Movie> moviesAfterSixth = movieController.filterBy("{releaseDateGt: '1999-01-06'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfterSixth));

        Iterable<Movie> moviesBetweenSixthAndSeventh = movieController
                .filterBy("{releaseDateGt: '1999-01-06', releaseDateLt: '1999-01-07'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBetweenSixthAndSeventh));

        Iterable<Movie> moviesBetweenSixthAndSeventhIncluded = movieController
                .filterBy("{releaseDateGte: '1999-01-06', releaseDateLte: '1999-01-07'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesBetweenSixthAndSeventhIncluded));

        Iterable<Movie> moviesBetweenFifthAndSeventh = movieController
                .filterBy("{releaseDateGt: '1999-01-05', releaseDateLt: '1999-01-07'}", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(moviesBetweenFifthAndSeventh));

        Iterable<Movie> moviesBetweenFifthAndSeventhIncluded = movieController
                .filterBy("{releaseDateGte: '1999-01-05', releaseDateLte: '1999-01-07'}", null, null);
        Assert.assertEquals(4, IterableUtil.sizeOf(moviesBetweenFifthAndSeventhIncluded));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void string_range_queries() {

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDate(timeStamp("2005-01-05"));
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDate(timeStamp("2017-01-05"));
        movieRepository.save(it);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDate(timeStamp("1999-01-05"));
        movieRepository.save(matrix);

        Iterable<Movie> moviesAfterA = movieController.filterBy("{nameGt: 'A '}", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(moviesAfterA));

        Iterable<Movie> moviesBeforeD = movieController.filterBy("{nameLt: 'D' }", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBeforeD));

        Iterable<Movie> moviesAfterDBeforeM = movieController.filterBy("{nameGt: 'D', nameLt: 'M' }", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesAfterDBeforeM));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_many_to_one_null__fetch_movies_with_no_director() {
        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> noDirectorMovies = movieController.filterBy("{director: null}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(noDirectorMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_many_to_one_null__fetch_movies_with_category_having_parent_category() {

        Category fiction = new Category();
        fiction.setName("fiction");
        categoryRepository.save(fiction);

        Category horror = new Category();
        horror.setName("horror");
        horror.setParentCategory(fiction);
        categoryRepository.save(horror);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setCategory(fiction);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setCategory(horror);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setCategory(horror);
        movieRepository.save(it);

        Iterable<Movie> moviesWithCategoriesThatHaveParentCategoryHorror1 = movieController
                .filterBy("{category: {parentCategory: {id:" + fiction.getId() + "}}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesWithCategoriesThatHaveParentCategoryHorror1));
        Iterable<Movie> moviesWithCategoriesThatHaveParentCategoryHorror2 = movieController
                .filterBy("{category: {parentCategory: " + fiction.getId() + "}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesWithCategoriesThatHaveParentCategoryHorror2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_many_to_one_null__fetch_movies_with_director_not_null() {
        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> directorNotNullMovies = movieController.filterBy("{directorNot: null}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(directorNotNullMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_many_to_many_null__fetch_actors_with_no_movies() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor noMovieActor = new Actor();
        noMovieActor.setFirstName("No Movie");
        noMovieActor.setLastName("Whatsoever");
        actorRepository.save(noMovieActor);

        Actor noMovieActor2 = new Actor();
        noMovieActor2.setFirstName("No Movie");
        noMovieActor2.setLastName("Whatsoever 2");
        actorRepository.save(noMovieActor2);

        Iterable<Actor> noMovieActors = actorController.filterBy("{movies: null}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(noMovieActors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_many_to_many_not_null__fetch_actors_with_at_least_a_movie() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor carrie = new Actor();
        carrie.setFirstName("Carrie-Anne");
        carrie.setLastName("Moss");
        carrie.setMovies(Arrays.asList(matrix));
        actorRepository.save(carrie);

        Actor noMovieActor = new Actor();
        noMovieActor.setFirstName("No Movie");
        noMovieActor.setLastName("Whatsoever");
        actorRepository.save(noMovieActor);

        Actor noMovieActor2 = new Actor();
        noMovieActor2.setFirstName("No Movie");
        noMovieActor2.setLastName("Whatsoever 2");
        actorRepository.save(noMovieActor2);

        Iterable<Actor> withMovieActors = actorController.filterBy("{moviesNot: null}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(withMovieActors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Ignore
    public void reference_test_conjunctive_equality_in_list__fetch_actors_that_have_played_in_all_movies_of_query() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor carrie = new Actor();
        carrie.setFirstName("Carrie-Anne");
        carrie.setLastName("Moss");
        carrie.setMovies(Arrays.asList(matrix));
        actorRepository.save(carrie);

        Actor noMovieActor = new Actor();
        noMovieActor.setFirstName("No Movie");
        noMovieActor.setLastName("Whatsoever");
        actorRepository.save(noMovieActor);

        Actor noMovieActor2 = new Actor();
        noMovieActor2.setFirstName("No Movie");
        noMovieActor2.setLastName("Whatsoever 2");
        actorRepository.save(noMovieActor2);

        Iterable<Actor> matrixAndConstantineActors = actorController
                .filterBy("{moviesAnd: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(matrixAndConstantineActors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void reference_match__fetch_movie_by_actor_id() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id: " + keanu.getId() + "}}", null, null);
        Iterable<Movie> keanuMovies2 = movieController.filterBy("{actors: " + keanu.getId() + "}", null, null);
        Iterable<Movie> keanuMovies3 = movieController.filterBy("{actors: [{id: " + keanu.getId() + "}]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies2));
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies3));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void disjunctive_reference_match__fetch_movie_by_multiple_actor_ids() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor jaeden = new Actor();
        jaeden.setFirstName("Jaeden");
        jaeden.setLastName("Martell");
        jaeden.setMovies(Arrays.asList(it));
        actorRepository.save(jaeden);

        Iterable<Movie> moviesByActors = movieController
                .filterBy("{actors: [" + keanu.getId() + ", " + jaeden.getId() + "]}", null, null);

        Assert.assertEquals(3, IterableUtil.sizeOf(moviesByActors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void fetch_by_multiple_ids__fetch_movies_by_ids() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> moviesById = movieController
                .filterBy("{ id: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesById));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void fetch_by_multiple_ids__fetch_movies_by_not_including_ids() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> moviesByNotIds = movieController
                .filterBy("{ idNot: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesByNotIds));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void fetch_by_id__fetch_movie_by_id() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieById = movieController.filterBy("{id:" + matrix.getId() + "}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieById));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void fetch_by_id__fetch_movie_by_id_and_name_with_or_on_first_level() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieById = movieController
                .filterBy("[{id:" + matrix.getId() + "},{id:" + constantine.getId() + "}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieById));
        Iterable<Movie> movieByTwoNames = movieController
                .filterBy("[{name:'" + matrix.getName() + "'},{name:'" + constantine.getName() + "'}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByTwoNames));
        Iterable<Movie> movieByTwoNames2 = movieController
                .filterBy("{name:['" + matrix.getName() + "','" + constantine.getName() + "']}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByTwoNames2));
        Iterable<Movie> movieByTwoNamesOneWrong = movieController
                .filterBy("[{name:'" + matrix.getName() + "'},{name:'somethingsomething'}]", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByTwoNamesOneWrong));
        Iterable<Movie> movieByTwoNamesTwoWrong = movieController
                .filterBy("[{name:'something'},{name:'somethingsomething'}]", null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(movieByTwoNamesTwoWrong));
        Iterable<Movie> movieByIdOrName = movieController
                .filterBy("[{id:" + matrix.getId() + "},{name:'" + constantine.getName() + "'}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByIdOrName));
        Iterable<Movie> movieByIdOrName2 = movieController
                .filterBy("[{id:" + constantine.getId() + "},{name:'" + constantine.getName() + "'}]", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByIdOrName2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void fetch_by_id__fetch_movie_by_not_id() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByNotId = movieController.filterBy("{idNot:" + matrix.getId() + "}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByNotId));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void exact_match_of_primitive__fetch_movie_by_name() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy("{name:'" + matrix.getName() + "'}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void exact_match_of_primitive_in_primitive_collection__fetch_movie_by_age_rating() {
        Set<String> ageRatings = new HashSet<String>();

        ageRatings = new HashSet<String>();
        ageRatings.add("PG-13");

        Movie matrix = new Movie();
        ageRatings = new HashSet<String>();
        ageRatings.add("PG-13");
        matrix.setAgeRatings(ageRatings);
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        ageRatings = new HashSet<String>();
        ageRatings.add("R");
        matrix2.setAgeRatings(ageRatings);
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        ageRatings = new HashSet<String>();
        ageRatings.add("R");
        constantine.setAgeRatings(ageRatings);
        movieRepository.save(constantine);

        Iterable<Movie> movieByAgeRating = movieController.filterBy("{ageRatings: 'R'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByAgeRating));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_many_fetch_movies_with_actor_id() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id:" + keanu.getId() + "}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_many_fetch_movies_with_actor_first_name() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController
                .filterBy("{actors: {firstName:'" + keanu.getFirstName() + "', lastNameNot: 'Reves'}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_many_fetch_movies_with_actor_first_name_and_id_overrides() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController.filterBy(
                "{actors: {id: " + keanu.getId() + ", firstName:'" + keanu.getFirstName() + "', lastNameNot: 'Reves'}}",
                null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
        Iterable<Movie> constantineMovie = movieController.filterBy("{id: " + constantine.getId() + ", actors: {id: "
                + keanu.getId() + ", firstName:'" + keanu.getFirstName() + "', lastNameNot: 'Reves'}}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(constantineMovie));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_many_fetch_movies_with_actor_first_name_like() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController
                .filterBy(UrlUtils.encodeURI("{actors: {firstName:'%ean%', lastName: '%eeve%'}}"), null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_one_fetch_movies_with_director_first_name_exact() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Director other = new Director();
        other.setFirstName("other");
        other.setLastName("Other");
        directorRepository.save(other);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setDirector(other);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setDirector(other);
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> lanaMovies = movieController.filterBy("{director: {firstName:'Lana'}}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(lanaMovies));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void three_level_fetch_actors_of_movies_with_director_first_name_exact() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Director other = new Director();
        other.setFirstName("other");
        other.setLastName("Other");
        directorRepository.save(other);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setDirector(other);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setDirector(other);
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor otherActor = new Actor();
        otherActor.setFirstName("other");
        otherActor.setLastName("other");
        actorRepository.save(otherActor);

        Iterable<Actor> actors = actorController.filterBy("{movies: {director: {firstName:'Lana'}}}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(actors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void three_level_fetch_actors_of_movies_with_director_first_name_like() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Director other = new Director();
        other.setFirstName("other");
        other.setLastName("Other");
        directorRepository.save(other);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setDirector(other);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setDirector(other);
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor otherActor = new Actor();
        otherActor.setFirstName("other");
        otherActor.setLastName("other");
        actorRepository.save(otherActor);

        Iterable<Actor> actors = actorController.filterBy("{movies: {director: {firstName:'Lan%'}}}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(actors));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_many_to_many_fetch_movies_with_actor_having_firstName_and_last_name_in_three_equivalent_ways() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {firstName:'Keanu', lastName: 'Reeves'}}",
                null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));

        Iterable<Movie> keanuMovies2 = movieController
                .filterBy("{actorsAnd: [{firstName:'Keanu'},{lastName: 'Reeves'}]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies2));
        Iterable<Movie> keanuMovies3 = movieController
                .filterBy("{actors: [{firstName:'SomethingSomething'},{lastName: 'Reeves'}]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies3));
        Iterable<Movie> keanuMovies4 = movieController
                .filterBy("{actorsAnd: [{firstName:'SomethingSomething'},{lastName: 'Reeves'}]}", null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(keanuMovies4));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Ignore
    public void two_level_many_to_many_fetch_actors_with_movies_starting_with_matr_or_const() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Actor keanu = new Actor();
        keanu.setFirstName("Keanu");
        keanu.setLastName("Reeves");
        keanu.setMovies(Arrays.asList(matrix, constantine));
        actorRepository.save(keanu);

        Actor noMovieActor = new Actor();
        noMovieActor.setFirstName("No Movie");
        noMovieActor.setLastName("Whatsoever");
        actorRepository.save(noMovieActor);

        Actor noMovieActor2 = new Actor();
        noMovieActor2.setFirstName("No Movie 2");
        noMovieActor2.setLastName("Whatsoever 2");
        actorRepository.save(noMovieActor2);

        Iterable<Actor> actors = actorController
                .filterBy(UrlUtils.encodeURI("{movies: [{name:'%atr%'},{name:'%onest%'}]}}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(actors));
        Iterable<Actor> actors2 = actorController
                .filterBy(UrlUtils.encodeURI("{moviesAnd: [{name:'%atr%'},{name:'%onst%'}]}}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(actors2));
        Iterable<Actor> actors3 = actorController
                .filterBy(UrlUtils.encodeURI("{moviesAnd: [{name:'%atr%'},{name:'%onest%'}]}}"), null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(actors3));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_exact_match_of_primitive__fetch_movie_by_director_name() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        matrix2.setDirector(lana);
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Iterable<Movie> movieByName = movieController.filterBy("{director: {firstName:'" + lana.getFirstName() + "'}}",
                null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level_full_text_search__fetch_movie_like_director_name() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        matrix2.setDirector(lana);
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{director: {firstName:'%an%'}}"),
                null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level__fetch_movie_like_director_name() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        matrix2.setDirector(lana);
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{director: {firstName:'%an%'}}"),
                null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void two_level__fetch_movie_with_director_id() {

        Director lana = new Director();
        lana.setFirstName("Lana");
        lana.setLastName("Wachowski");
        directorRepository.save(lana);

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setDirector(lana);
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        matrix2.setDirector(lana);
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Iterable<Movie> movieByName = movieController.filterBy("{director: {id:" + lana.getId() + "}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_name_prefix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{name: 'The Matr%'}"), null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_name_postfix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{name: '%loaded'}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_name_infix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{name: '%atri%'}"), null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_not_containing_name_prefix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{nameNot: 'The Matr%'}"), null,
                null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_primary_key_that_is_native_uuid() {
        UUIDEntity entity1 = new UUIDEntity();
        uuidEntityRepository.save(entity1);

        UUIDEntity entity2 = new UUIDEntity();
        uuidEntityRepository.save(entity2);

        Iterable<UUIDEntity> entitiesByUuid = uuidEntityController.filterBy("{uuid: '" + entity1.getUuid() + "'}", null,
                null);
        Assert.assertEquals(1, IterableUtil.sizeOf(entitiesByUuid));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_foreign_key_that_is_native_uuid() {
        UUIDEntity entity1 = new UUIDEntity();
        uuidEntityRepository.save(entity1);

        UUIDEntity entity2 = new UUIDEntity();
        uuidEntityRepository.save(entity2);

        UUIDRelationship relationship1 = new UUIDRelationship();
        relationship1.setUuidEntity(entity1);
        uuidRelationshipRepository.save(relationship1);

        UUIDRelationship relationship2 = new UUIDRelationship();
        relationship2.setUuidEntity(entity1);
        uuidRelationshipRepository.save(relationship2);

        UUIDRelationship relationship3 = new UUIDRelationship();
        relationship3.setUuidEntity(entity2);
        uuidRelationshipRepository.save(relationship3);

        Iterable<UUIDRelationship> relsByUuid = uuidRelationshipController
                .filterBy("{uuidEntity:'" + entity1.getUuid() + "' }", null, null);
        Iterable<UUIDRelationship> relsByUuid2 = uuidRelationshipController
                .filterBy("{uuidEntity: {uuid: '" + entity1.getUuid() + "' }}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(relsByUuid));
        Assert.assertEquals(2, IterableUtil.sizeOf(relsByUuid2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_not_containing_name_postfix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{nameNot: '%loaded'}"), null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void search_text_on_primitive__fetch_movie_by_not_oontaining_name_infix() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("The Matrix: Reloaded");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByName = movieController.filterBy(UrlUtils.encodeURI("{nameNot: '%atri%'}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void exact_match_of_primitive__fetch_movie_by_not_name() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByNotName = movieController.filterBy("{nameNot:'" + matrix.getName() + "'}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByNotName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void disjunctive_exact_match_of_primitives__fetch_movie_by_list_of_names() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> moviesByName = movieController
                .filterBy("{name: ['" + matrix.getName() + "','" + constantine.getName() + "']}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void full_text_search_in_all_fields() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> movieByFullText = movieController.filterBy("{q:atr}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByFullText));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void find_all_works() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        movieRepository.save(it);

        Iterable<Movie> allMovies = movieController.filterBy("{}", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(allMovies));
        Iterable<Movie> allMovies2 = movieController.filterBy(null, null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(allMovies2));
    }
}
