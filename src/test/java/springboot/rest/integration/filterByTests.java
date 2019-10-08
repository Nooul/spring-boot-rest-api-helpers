package springboot.rest.integration;

import org.assertj.core.util.IterableUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import springboot.rest.helpers.controllers.*;
import springboot.rest.helpers.entities.*;
import springboot.rest.helpers.repositories.*;

import static springboot.rest.utils.UrlUtils.encodeURIComponent;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class filterByTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private UuidRepository uuidRepository;

    @Autowired
    private UUIDEntityRepository uuidEntityRepository;

    @Autowired
    private UUIDRelationshipRepository uuidRelationshipRepository;

    @Autowired
    private MovieController movieController;

    @Autowired
    private ActorController actorController;

    @Autowired
    private UuidController uuidController;

    @Autowired
    private UUIDEntityController uuidEntityController;

    @Autowired
    private UUIDRelationshipController uuidRelationshipController;



//    @Before
//    public void before() {
//        Category action = new Category();
//        action.setName("action");
//        categoryRepository.save(action);
//
//        Category horror = new Category();
//        horror.setName("horror");
//        categoryRepository.save(horror);
//
//
//
//        Director lana = new Director();
//        lana.setFirstName("Lana");
//        lana.setLastName("Wachowski");
//        directorRepository.save(lana);
//
//
//        Movie matrix = new Movie();
//        matrix.setName("The Matrix");
//        matrix.setCategory(action);
//        matrix.setDirector(lana);
//        movieRepository.save(matrix);
//
//        Director francis = new Director();
//        francis.setFirstName("Francis");
//        francis.setLastName("Lawrence");
//        directorRepository.save(francis);
//
//        Movie constantine = new Movie();
//        constantine.setName("Constantine");
//        constantine.setCategory(horror);
//        constantine.setDirector(francis);
//        movieRepository.save(constantine);
//
//        Actor keanu = new Actor();
//        keanu.setFirstName("Keanu");
//        keanu.setLastName("Reeves");
//        keanu.setMovies(Arrays.asList(matrix, constantine));
//        actorRepository.save(keanu);
//
//
//
//        Director andy = new Director();
//        andy.setFirstName("Andy");
//        andy.setLastName("Muschietti");
//        directorRepository.save(andy);
//
//        Movie it = new Movie();
//        it.setName("IT");
//        it.setCategory(horror);
//        it.setDirector(andy);
//        movieRepository.save(it);
//    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void find_null_primitive_should_return() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setYearReleased(1999);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName(null);
        constantine.setYearReleased(2005);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setYearReleased(2017);
        movieRepository.save(it);

        Iterable<Movie> moviesWithNullName1 = movieController.filterBy("{name: null}", null, null);
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
    public void integer_range_queries() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setYearReleased(1999);
        movieRepository.save(matrix);

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setYearReleased(2005);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setYearReleased(2017);
        movieRepository.save(it);

        Iterable<Movie> moviesAfterOrOn2005 = movieController.filterBy("{yearReleasedGte: 2005}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfterOrOn2005));

        Iterable<Movie> moviesAfter2005 = movieController.filterBy("{yearReleasedGt: 2005}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesAfter2005));

        Iterable<Movie> moviesBeforeOrOn2005 = movieController.filterBy("{yearReleasedLte: 2005}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesBeforeOrOn2005));

        Iterable<Movie> moviesBefore2005 = movieController.filterBy("{yearReleasedLt: 2005}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBefore2005));

        Iterable<Movie> moviesAfter1999Before2017 = movieController.filterBy("{yearReleasedGt: 1999, yearReleasedLt:2017}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesAfter1999Before2017));

        Iterable<Movie> moviesAfter2005OrOnBefore2017OrOn = movieController.filterBy("{yearReleasedGte: 2005, yearReleasedLte:2017}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(moviesAfter2005OrOnBefore2017OrOn));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void string_range_queries() {

        Movie constantine = new Movie();
        constantine.setName("Constantine");
        constantine.setYearReleased(2005);
        movieRepository.save(constantine);

        Movie it = new Movie();
        it.setName("IT");
        it.setYearReleased(2017);
        movieRepository.save(it);


        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setYearReleased(1999);
        movieRepository.save(matrix);


        Iterable<Movie> moviesAfterA = movieController.filterBy("{nameGt: A }", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(moviesAfterA));

        Iterable<Movie> moviesBeforeD = movieController.filterBy("{nameLt: D }", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(moviesBeforeD));

        Iterable<Movie> moviesAfterDBeforeM = movieController.filterBy("{nameGt: D, nameLt:M }", null, null);
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


        Iterable<Actor> matrixAndConstantineActors = actorController.filterBy("{moviesAnd: ["+matrix.getId()+","+constantine.getId()+"]}", null, null);
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


        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id: " + keanu.getId()+ "}}", null, null);
        Iterable<Movie> keanuMovies2 = movieController.filterBy("{actors: "+ keanu.getId()+ "}", null, null);
        Iterable<Movie> keanuMovies3 = movieController.filterBy("{actors: [{id: "+ keanu.getId()+ "}]}", null, null);
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


        Iterable<Movie> moviesByActors = movieController.filterBy("{actors: [" + keanu.getId()+ ", "+ jaeden.getId() +"]}", null, null);

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

        Iterable<Movie> moviesById = movieController.filterBy("{ id: ["+matrix.getId()+","+constantine.getId()+"]}", null, null);
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

        Iterable<Movie> moviesByNotIds = movieController.filterBy("{ idNot: ["+matrix.getId()+","+constantine.getId()+"]}", null, null);
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


        Iterable<Movie> movieById = movieController.filterBy("{id:"+matrix.getId()+"}", null, null);
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


        Iterable<Movie> movieById = movieController.filterBy("[{id:"+matrix.getId()+"},{id:"+constantine.getId()+"}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieById));
        Iterable<Movie> movieByTwoNames = movieController.filterBy("[{name:"+matrix.getName()+"},{name:"+constantine.getName()+"}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByTwoNames));
        Iterable<Movie> movieByTwoNames2 = movieController.filterBy("{name:["+matrix.getName()+","+constantine.getName()+"]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByTwoNames2));
        Iterable<Movie> movieByTwoNamesOneWrong = movieController.filterBy("[{name:"+matrix.getName()+"},{name:somethingsomething}]", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByTwoNamesOneWrong));
        Iterable<Movie> movieByTwoNamesTwoWrong = movieController.filterBy("[{name:something},{name:somethingsomething}]", null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(movieByTwoNamesTwoWrong));
        Iterable<Movie> movieByIdOrName = movieController.filterBy("[{id:"+matrix.getId()+"},{name:"+constantine.getName()+"}]", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByIdOrName));
        Iterable<Movie> movieByIdOrName2 = movieController.filterBy("[{id:"+constantine.getId()+"},{name:"+constantine.getName()+"}]", null, null);
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


        Iterable<Movie> movieByNotId = movieController.filterBy("{idNot:"+matrix.getId()+"}", null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy("{name:"+matrix.getName()+"}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
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



        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id:"+keanu.getId()+"}}", null, null);
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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {firstName:"+keanu.getFirstName()+", lastNameNot: Reves}}", null, null);
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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {id: "  + keanu.getId() +", firstName:"+keanu.getFirstName()+", lastNameNot: Reves}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));
        Iterable<Movie> constantineMovie = movieController.filterBy("{id: " + constantine.getId() +", actors: {id: "  + keanu.getId() +", firstName:"+keanu.getFirstName()+", lastNameNot: Reves}}", null, null);
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

        Iterable<Movie> keanuMovies = movieController.filterBy(encodeURIComponent("{actors: {firstName:%ean%, lastName: %eeve%}}"), null, null);
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

        Iterable<Movie> lanaMovies = movieController.filterBy("{director: {firstName:Lana}}", null, null);
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

        Iterable<Actor> actors = actorController.filterBy("movies: {director: {firstName:Lana}}}", null, null);
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

        Iterable<Actor> actors = actorController.filterBy("movies: {director: {firstName:Lan%}}}", null, null);
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

        Iterable<Movie> keanuMovies = movieController.filterBy("{actors: {firstName:Keanu, lastName: Reeves}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies));

        Iterable<Movie> keanuMovies2 = movieController.filterBy("{actorsAnd: [{firstName:Keanu},{lastName: Reeves}]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies2));
        Iterable<Movie> keanuMovies3 = movieController.filterBy("{actors: [{firstName:SomethingSomething},{lastName: Reeves}]}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(keanuMovies3));
        Iterable<Movie> keanuMovies4 = movieController.filterBy("{actorsAnd: [{firstName:SomethingSomething},{lastName: Reeves}]}", null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(keanuMovies4));
    }



    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
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
        Assert.assertEquals(1, IterableUtil.sizeOf(actors));
        Iterable<Actor> actors2 = actorController.filterBy(encodeURIComponent("{moviesAnd: [{name:%atr%},{name:%onst%}]}}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(actors2));
        Iterable<Actor> actors3 = actorController.filterBy(encodeURIComponent("{moviesAnd: [{name:%atr%},{name:%onest%}]}}"), null, null);
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


        Iterable<Movie> movieByName = movieController.filterBy("{director: {firstName:"+lana.getFirstName()+"}}", null, null);
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void  two_level_full_text_search__fetch_movie_like_director_name() {

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
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void  two_level__fetch_movie_like_director_name() {

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
        Assert.assertEquals(2, IterableUtil.sizeOf(movieByName));
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void  two_level__fetch_movie_with_director_id() {

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

        Iterable<Movie> movieByName = movieController.filterBy("{director: {id:"+lana.getId()+"}}", null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: The Matr%}"), null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: %loaded}"), null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{name: %atri%}"), null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: The Matr%}"), null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByName));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_primary_key_that_is_not_called_id() {
        UUID uuid = new UUID("ad2qewqdscasd2e123");
        uuidRepository.save(uuid);

        Iterable<UUID> uuidsByUuid = uuidController.filterBy("{uuid: ad2qewqdscasd2e123}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(uuidsByUuid));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_foreign_key_that_is_not_called_id() {
        Movie matrix = new Movie();
        matrix.setName("The Matrix");
        matrix.setUuid(new UUID("ad2qewqdscasd2e123"));
        movieRepository.save(matrix);

        Iterable<Movie> movieByUuid = movieController.filterBy("{uuid: ad2qewqdscasd2e123}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(movieByUuid));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_primary_key_that_is_native_uuid() {
        UUIDEntity entity = new UUIDEntity();
        uuidEntityRepository.save(entity);

        Iterable<UUIDEntity> uuidsByUuid = uuidEntityController.filterBy("{uuid: "+entity.getUuid().toString()+"}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(uuidsByUuid));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void filter_by_foreign_key_that_is_native_uuid() {
        UUIDEntity entity = new UUIDEntity();
        uuidEntityRepository.save(entity);

        UUIDRelationship relationship = new UUIDRelationship();
        relationship.setUuidEntity(entity);
        uuidRelationshipRepository.save(relationship);

        Iterable<UUIDRelationship> uuidsByUuid = uuidRelationshipController.filterBy("{uuidEntity: "+entity.getUuid().toString()+"}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(uuidsByUuid));
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: %loaded}"), null, null);
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

        Iterable<Movie> movieByName = movieController.filterBy(encodeURIComponent("{nameNot: %atri%}"), null, null);
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

        Iterable<Movie> movieByNotName = movieController.filterBy("{nameNot:"+matrix.getName()+"}", null, null);
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

        Iterable<Movie> moviesByName = movieController.filterBy("{name: ["+matrix.getName()+","+ constantine.getName()+"]}", null, null);
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
