package com.nooul.apihelpers.springbootrest.services;

import com.nooul.apihelpers.springbootrest.AbstractJpaDataTest;
import com.nooul.apihelpers.springbootrest.helpers.controllers.*;
import com.nooul.apihelpers.springbootrest.helpers.entities.*;
import com.nooul.apihelpers.springbootrest.helpers.repositories.*;
import com.nooul.apihelpers.springbootrest.helpers.values.Mobile;
import com.nooul.apihelpers.springbootrest.specifications.CustomSpecifications;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.HashSet;

import static com.nooul.apihelpers.springbootrest.helpers.utils.DateUtils.timeStamp;
import static com.nooul.apihelpers.springbootrest.utils.UrlUtils.encodeURIComponent;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({FilterService.class, CustomSpecifications.class,
        MovieController.class, ActorController.class, UUIDEntityController.class, SenderController.class,
        UUIDRelationshipController.class
})
public class filterByTest extends AbstractJpaDataTest {

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
    private SenderRepository senderRepository;

    @Autowired
    private UUIDRelationshipRepository uuidRelationshipRepository;

    @Autowired
    private MovieController movieController;

    @Autowired
    private ActorController actorController;

    @Autowired
    private UUIDEntityController uuidEntityController;

    @Autowired
    private SenderController senderController;

    @Autowired
    private UUIDRelationshipController uuidRelationshipController;

    @Test
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

        Iterable<Movie> moviesWithNullName1 = movieController.filterBy("{name: null}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesWithNullName1));
        Iterable<Movie> moviesWithNullName2 = movieController.filterBy("{name: 'null'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesWithNullName2));
        Iterable<Movie> moviesWithNullName3 = movieController.filterBy("{name: ''}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesWithNullName3));
        Iterable<Movie> moviesWithNullName4 = movieController.filterBy("{name: '   '}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesWithNullName4));
    }

    @Test
    public void timestamp_date_range_queries() {
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
        assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn2005));

        Iterable<Movie> moviesAfter2005 = movieController.filterBy("{releaseDateGt: '2005-01-01'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter2005));

        Iterable<Movie> moviesBeforeOrOn2005 = movieController.filterBy("{releaseDateLte: '2005-01-1'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBeforeOrOn2005));

        Iterable<Movie> moviesBefore2005 = movieController.filterBy("{releaseDateLt: '2005-01-01'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBefore2005));

        Iterable<Movie> moviesAfter1999Before2017 = movieController.filterBy("{releaseDateGt: '1999-01-01', releaseDateLt: '2017-12-01'}", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesAfter1999Before2017));

        Iterable<Movie> moviesAfter2005OrOnBefore2017OrOn = movieController.filterBy("{releaseDateGte: 2005-01-01, releaseDateLte:2017-12-01}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter2005OrOnBefore2017OrOn));
    }

    @Test
    public void instant_date_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDateInstant(timeStamp("1999-01-05").toInstant());
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDateInstant(timeStamp("2005-01-05").toInstant());
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDateInstant(timeStamp("2017-01-05").toInstant());
        movieRepository.save(it);

        Iterable<Movie> moviesAfterOrOn2005 = movieController.filterBy("{releaseDateInstantGte: '2005-01-01'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn2005));

        Iterable<Movie> moviesAfter2005 = movieController.filterBy("{releaseDateInstantGt: '2005-01-01'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter2005));

        Iterable<Movie> moviesBeforeOrOn2005 = movieController.filterBy("{releaseDateInstantLte: '2005-01-1'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBeforeOrOn2005));

        Iterable<Movie> moviesBefore2005 = movieController.filterBy("{releaseDateInstantLt: '2005-01-01'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBefore2005));

        Iterable<Movie> moviesAfter1999Before2017 = movieController.filterBy("{releaseDateInstantGt: '1999-01-01', releaseDateInstantLt: '2017-12-01'}", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesAfter1999Before2017));

        Iterable<Movie> moviesAfter2005OrOnBefore2017OrOn = movieController.filterBy("{releaseDateInstantGte: 2005-01-01, releaseDateInstantLte:2017-12-01}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter2005OrOnBefore2017OrOn));
    }

    @Test
    public void timestamp_date_with_time_range_queries() {
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

        Iterable<Movie> moviesAfterOrOn1 = movieController.filterBy("{releaseDateGte: '1999-01-05T01:00:00'}", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesAfterOrOn1));

        Iterable<Movie> moviesAfter1 = movieController.filterBy("{releaseDateGt: '1999-01-05T01:00:00'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter1));

        Iterable<Movie> moviesAfterOrOn3 = movieController.filterBy("{releaseDateGte: '1999-01-05T03:00:00'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn3));

        Iterable<Movie> moviesAfter3 = movieController.filterBy("{releaseDateGt: '1999-01-05T03:00:00'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesAfter3));

    }

    @Test
    public void instant_date_with_time_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setReleaseDateInstant(timeStamp("1999-01-05T01:00:00").toInstant());
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setReleaseDateInstant(timeStamp("1999-01-05T03:00:00").toInstant());
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setReleaseDateInstant(timeStamp("1999-01-05T04:00:00").toInstant());
        movieRepository.save(it);

        Iterable<Movie> moviesAfterOrOn1 = movieController.filterBy("{releaseDateInstantGte: '1999-01-05T01:00:00'}", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesAfterOrOn1));

        Iterable<Movie> moviesAfter1 = movieController.filterBy("{releaseDateInstantGt: '1999-01-05T01:00:00'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfter1));

        Iterable<Movie> moviesAfterOrOn3 = movieController.filterBy("{releaseDateInstantGte: '1999-01-05T03:00:00'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn3));

        Iterable<Movie> moviesAfter3 = movieController.filterBy("{releaseDateInstantGt: '1999-01-05T03:00:00'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesAfter3));

    }


    @Test
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
        assertEquals(2, IterableUtil.sizeOf(moviesBeforeOrOnSixth));

        Iterable<Movie> moviesBeforeSixth = movieController.filterBy("{releaseDateLt: '1999-01-06'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesBeforeSixth));

        Iterable<Movie> moviesAfterOrOnSixth = movieController.filterBy("{releaseDateGte: '1999-01-06'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOnSixth));

        Iterable<Movie> moviesAfterSixth = movieController.filterBy("{releaseDateGt: '1999-01-06'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesAfterSixth));

        Iterable<Movie> moviesBetweenSixthAndSeventh = movieController.filterBy("{releaseDateGt: '1999-01-06', releaseDateLt: '1999-01-07'}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBetweenSixthAndSeventh));

        Iterable<Movie> moviesBetweenSixthAndSeventhIncluded = movieController.filterBy("{releaseDateGte: '1999-01-06', releaseDateLte: '1999-01-07'}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesBetweenSixthAndSeventhIncluded));

        Iterable<Movie> moviesBetweenFifthAndSeventh = movieController.filterBy("{releaseDateGt: '1999-01-05', releaseDateLt: '1999-01-07'}", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesBetweenFifthAndSeventh));

        Iterable<Movie> moviesBetweenFifthAndSeventhIncluded = movieController.filterBy("{releaseDateGte: '1999-01-05', releaseDateLte: '1999-01-07'}", null, null);
        assertEquals(4, IterableUtil.sizeOf(moviesBetweenFifthAndSeventhIncluded));
    }

    @Test
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


        Iterable<Movie> moviesAfterA = movieController.filterBy("{nameGt: A }", null, null);
        assertEquals(3, IterableUtil.sizeOf(moviesAfterA));

        Iterable<Movie> moviesBeforeD = movieController.filterBy("{nameLt: D }", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesBeforeD));

        Iterable<Movie> moviesAfterDBeforeM = movieController.filterBy("{nameGt: D, nameLt:M }", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesAfterDBeforeM));

    }

    @Test
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
        assertEquals(2, IterableUtil.sizeOf(noDirectorMovies));
    }

    @Test
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

        Iterable<Movie> moviesWithCategoriesThatHaveParentCategoryHorror1 = movieController.filterBy("{category: {parentCategory: {id:" + fiction.getId() + "}}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesWithCategoriesThatHaveParentCategoryHorror1));
        Iterable<Movie> moviesWithCategoriesThatHaveParentCategoryHorror2 = movieController.filterBy("{category: {parentCategory: " + fiction.getId() + "}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesWithCategoriesThatHaveParentCategoryHorror2));
        Iterable<Movie> moviesWithCategoriesThatHaveParentCategoryHorror3 = movieController.filterBy("{category: {parentCategory: " + fiction.getId() + "}, allowDuplicates: true }}", null, null);
        assertEquals(4, IterableUtil.sizeOf(moviesWithCategoriesThatHaveParentCategoryHorror3));
    }

    @Test
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
        assertEquals(1, IterableUtil.sizeOf(directorNotNullMovies));
    }

    @Test
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
        assertEquals(2, IterableUtil.sizeOf(noMovieActors));
    }

    @Test
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
        assertEquals(2, IterableUtil.sizeOf(withMovieActors));
    }

    @Test //fails due to performance issues fix and is not supported
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


        Iterable<Actor> matrixAndConstantineActors = actorController.filterBy("{moviesAnd: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        assertEquals(1, IterableUtil.sizeOf(matrixAndConstantineActors));
    }

    @Test
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
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));
        assertEquals(2, IterableUtil.sizeOf(keanuMovies2));
        assertEquals(2, IterableUtil.sizeOf(keanuMovies3));
    }

    @Test
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


        Iterable<Movie> moviesByActors = movieController.filterBy("{actors: [" + keanu.getId() + ", " + jaeden.getId() + "]}", null, null);

        assertEquals(3, IterableUtil.sizeOf(moviesByActors));
    }

    @Test
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

        Iterable<Movie> moviesById = movieController.filterBy("{ id: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesById));
    }

    @Test
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

        Iterable<Movie> moviesByNotIds = movieController.filterBy("{ idNot: [" + matrix.getId() + "," + constantine.getId() + "]}", null, null);
        assertEquals(1, IterableUtil.sizeOf(moviesByNotIds));
    }

    @Test
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
        assertEquals(1, IterableUtil.sizeOf(movieById));
    }

    @Test
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


        Iterable<Movie> movieById = movieController.filterBy("[{id:" + matrix.getId() + "},{id:" + constantine.getId() + "}]", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieById));
        Iterable<Movie> movieByTwoNames = movieController.filterBy("[{name:" + matrix.getName() + "},{name:" + constantine.getName() + "}]", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByTwoNames));
        Iterable<Movie> movieByTwoNames2 = movieController.filterBy("{name:[" + matrix.getName() + "," + constantine.getName() + "]}", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByTwoNames2));
        Iterable<Movie> movieByTwoNamesOneWrong = movieController.filterBy("[{name:" + matrix.getName() + "},{name:somethingsomething}]", null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByTwoNamesOneWrong));
        Iterable<Movie> movieByTwoNamesTwoWrong = movieController.filterBy("[{name:something},{name:somethingsomething}]", null, null);
        assertEquals(0, IterableUtil.sizeOf(movieByTwoNamesTwoWrong));
        Iterable<Movie> movieByIdOrName = movieController.filterBy("[{id:" + matrix.getId() + "},{name:" + constantine.getName() + "}]", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByIdOrName));
        Iterable<Movie> movieByIdOrName2 = movieController.filterBy("[{id:" + constantine.getId() + "},{name:" + constantine.getName() + "}]", null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByIdOrName2));
    }

    @Test
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
        assertEquals(2, IterableUtil.sizeOf(movieByNotId));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy("{name:" + matrix.getName() + "}", null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByName));
    }


    @Test
    public void exact_match_of_primitive_in_primitive_collection__fetch_movie_by_age_rating() {

        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setAgeRatings(new HashSet<>(Arrays.asList("PG-13")));
        movieRepository.save(matrix);

        Movie matrix2 = new Movie();
        matrix2.setName("The Matrix Reloaded");
        matrix2.setAgeRatings(new HashSet<>(Arrays.asList("R")));
        movieRepository.save(matrix2);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setAgeRatings(new HashSet<>(Arrays.asList("R")));
        movieRepository.save(constantine);

        Iterable<Movie> movieByAgeRating = movieController.filterBy("{ageRatings: R}", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByAgeRating));
    }


    @Test

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
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test

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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {firstName:" + keanu.getFirstName() + ", lastNameNot: Reves}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test

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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id: " + keanu.getId() + ", firstName:" + keanu.getFirstName() + ", lastNameNot: Reves}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));
        Iterable<Movie> constantineMovie = movieController.filterBy("{id: " + constantine.getId() + ", actors: {id: " + keanu.getId() + ", firstName:" + keanu.getFirstName() + ", lastNameNot: Reves}}", null, null);
        assertEquals(1, IterableUtil.sizeOf(constantineMovie));
    }

    @Test

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

        Iterable<Movie> keanuMovies = movieController.filterBy(encodeURIComponent("{actors: {firstName:%ean%, lastName: %eeve%}}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));
    }

    @Test

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

        Iterable<Movie> lanaMovies = movieController.filterBy("{director: {firstName:Lana}}", null, null);
        assertEquals(1, IterableUtil.sizeOf(lanaMovies));
    }


    @Test

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

        Iterable<Actor> actors = actorController.filterBy("{movies: {director: {firstName:Lana}}}", null, null);
        assertEquals(1, IterableUtil.sizeOf(actors));
    }


    @Test

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

        Iterable<Actor> actors = actorController.filterBy("{movies: {director: {firstName:Lan%}}}", null, null);
        assertEquals(1, IterableUtil.sizeOf(actors));
    }


    @Test

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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {firstName:Keanu, lastName: Reeves}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies));

        Iterable<Movie> keanuMovies2 = movieController.filterBy("{actorsAnd: [{firstName:Keanu},{lastName: Reeves}]}", null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies2));
        Iterable<Movie> keanuMovies3 = movieController.filterBy("{actors: [{firstName:SomethingSomething},{lastName: Reeves}]}", null, null);
        assertEquals(2, IterableUtil.sizeOf(keanuMovies3));
        Iterable<Movie> keanuMovies4 = movieController.filterBy("{actorsAnd: [{firstName:SomethingSomething},{lastName: Reeves}]}", null, null);
        assertEquals(0, IterableUtil.sizeOf(keanuMovies4));
    }


    @Test
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


        Iterable<Actor> actors = actorController.filterBy(encodeURIComponent("{movies: [{name:%atr%},{name:%onest%}]}}"), null, null);
        assertEquals(1, IterableUtil.sizeOf(actors));
        Iterable<Actor> actors2 = actorController.filterBy(encodeURIComponent("{moviesAnd: [{name:%atr%},{name:%onst%}]}}"), null, null);
        assertEquals(1, IterableUtil.sizeOf(actors2));
        Iterable<Actor> actors3 = actorController.filterBy(encodeURIComponent("{moviesAnd: [{name:%atr%},{name:%onest%}]}}"), null, null);
        assertEquals(0, IterableUtil.sizeOf(actors3));
    }

    @Test //fails due to performance issues fix and is not supported
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


        Iterable<Movie> movieByName = movieController.filterBy("{director: {firstName:" + lana.getFirstName() + "}}", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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


        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{director: {firstName:%an%}}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{director: {firstName:%an%}}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }


    @Test
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
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: The Matr%}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: %loaded}"), null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: %atri%}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: The Matr%}"), null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    public void filter_by_primary_key_that_is_native_uuid() {
        UUIDEntity entity1 = new UUIDEntity();
        uuidEntityRepository.save(entity1);

        UUIDEntity entity2 = new UUIDEntity();
        uuidEntityRepository.save(entity2);

        Iterable<UUIDEntity> entitiesByUuid = uuidEntityController.filterBy("{uuid: " + entity1.getUuid() + "}", null, null);
        assertEquals(1, IterableUtil.sizeOf(entitiesByUuid));
    }

    @Test
    public void search_by_part_of_a_uuid_field() {
        Sender sender1 = new Sender();
        sender1.setSender("306970011222");
        senderRepository.save(sender1);

        Sender sender2 = new Sender();
        sender2.setSender("306970011333");
        senderRepository.save(sender2);

        String partOfUuid = sender1.getId().toString().substring(10, 20);

        Iterable<Sender> entitiesByUuid = senderController.filterBy("{q: " + partOfUuid + "}", null, null);
        assertEquals(1, IterableUtil.sizeOf(entitiesByUuid));
    }

    @Test
    public void search_by_part_of_a_mobile_field_which_is_integer_as_input_bug_fix() {
        Sender sender1 = new Sender();
        sender1.setSender("306970011222");
        senderRepository.save(sender1);

        Sender sender2 = new Sender();
        sender2.setSender("306970011333");
        senderRepository.save(sender2);

        Iterable<Sender> entitiesByMobile = senderController.filterBy("{q: 69700112}", null, null);
        assertEquals(1, IterableUtil.sizeOf(entitiesByMobile));
    }

    @Test
    public void search_by_part_of_value_object_field() {
        Sender sender1 = new Sender();
        sender1.setSenderValueObject(Mobile.fromString("306970011444"));
        senderRepository.save(sender1);

        Sender sender2 = new Sender();
        sender2.setSenderValueObject(Mobile.fromString("306970011555"));
        senderRepository.save(sender2);

        Iterable<Sender> entitiesByMobile = senderController.filterBy("{q: 306970011555 }", null, null);
        assertEquals(1, IterableUtil.sizeOf(entitiesByMobile));
    }

    @Test
    public void value_object_field_exact_match_null_and_inequality() {
        Sender sender1 = new Sender();
        sender1.setSenderValueObject(Mobile.fromString("306970011123"));
        senderRepository.save(sender1);

        Sender sender2 = new Sender();
        sender2.setSenderValueObject(Mobile.fromString("306970032123"));
        senderRepository.save(sender2);


        Sender sender3 = new Sender();
        senderRepository.save(sender3);

        Sender sender4 = new Sender();
        sender4.setSenderValueObject(Mobile.fromString("3069722222222"));
        senderRepository.save(sender4);

        Iterable<Sender> entitiesByValueObject = senderController.filterBy("{senderValueObject: 306970011123 }", null, null);
        assertEquals(1, IterableUtil.sizeOf(entitiesByValueObject));

        Iterable<Sender> entitiesByNullValueObject = senderController.filterBy("{senderValueObject: null }", null, null);
        assertEquals(0, IterableUtil.sizeOf(entitiesByNullValueObject));

        Iterable<Sender> entitiesByNotNullValueObject = senderController.filterBy("{senderValueObjectNot: null }", null, null);
        assertEquals(3, IterableUtil.sizeOf(entitiesByNotNullValueObject));

        Iterable<Sender> entitiesByNotValueObject = senderController.filterBy("{senderValueObjectNot: 306970011123 }", null, null);
        assertEquals(2, IterableUtil.sizeOf(entitiesByNotValueObject)); // result is not 3 because we need extra check with IS NOT NULL for negations in SQL
    }

    @Test
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

        Iterable<UUIDRelationship> relsByUuid = uuidRelationshipController.filterBy("{uuidEntity:" + entity1.getUuid() + " }", null, null);
        Iterable<UUIDRelationship> relsByUuid2 = uuidRelationshipController.filterBy("{uuidEntity: {uuid: " + entity1.getUuid() + " }}", null, null);
        assertEquals(2, IterableUtil.sizeOf(relsByUuid));
        assertEquals(2, IterableUtil.sizeOf(relsByUuid2));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: %loaded}"), null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: %atri%}"), null, null);
        assertEquals(1, IterableUtil.sizeOf(movieByName));
    }


    @Test
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

        Iterable<Movie> movieByNotName = movieController.filterBy("{nameNot:" + matrix.getName() + "}", null, null);
        assertEquals(2, IterableUtil.sizeOf(movieByNotName));
    }

    @Test
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

        Iterable<Movie> moviesByName = movieController.filterBy("{name: [" + matrix.getName() + "," + constantine.getName() + "]}", null, null);
        assertEquals(2, IterableUtil.sizeOf(moviesByName));
    }


    @Test
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
        assertEquals(1, IterableUtil.sizeOf(movieByFullText));
    }

    @Test
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
        assertEquals(3, IterableUtil.sizeOf(allMovies));
        Iterable<Movie> allMovies2 = movieController.filterBy(null, null, null);
        assertEquals(3, IterableUtil.sizeOf(allMovies2));
    }
}
