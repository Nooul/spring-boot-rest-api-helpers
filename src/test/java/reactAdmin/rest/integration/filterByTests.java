package reactAdmin.rest.integration;

import net.minidev.json.annotate.JsonIgnore;
import org.assertj.core.util.IterableUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactAdmin.rest.helpers.controllers.ActorController;
import reactAdmin.rest.helpers.controllers.MovieController;
import reactAdmin.rest.helpers.entities.Actor;
import reactAdmin.rest.helpers.entities.Director;
import reactAdmin.rest.helpers.entities.Movie;
import reactAdmin.rest.helpers.repositories.*;

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
    private PartnerRepository partnerRepository;

    @Autowired
    private MovieController movieController;

    @Autowired
    private ActorController actorController;


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
//        Partner lanasPartner = new Partner();
//        lanasPartner.setFirstName("Lana's");
//        lanasPartner.setLastName("Partner");
//        partnerRepository.save(lanasPartner);
//
//        Director lana = new Director();
//        lana.setFirstName("Lana");
//        lana.setLastName("Wachowski");
//        lana.setPartner(lanasPartner);
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
//        Partner andysPartner = new Partner();
//        andysPartner.setFirstName("Andy's");
//        andysPartner.setLastName("Partner");
//        partnerRepository.save(andysPartner);
//
//        Director andy = new Director();
//        andy.setFirstName("Andy");
//        andy.setLastName("Muschietti");
//        andy.setPartner(andysPartner);
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
    @Ignore
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void conjunctive_reference_match__fetch_actors_that_have_played_on_all_queried_movies() {
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


        Iterable<Actor> matrixOrItActors = actorController.filterBy("{movies: [" + matrix.getId()+ ", "+ it.getId() +"]}", null, null);
        Assert.assertEquals(3, IterableUtil.sizeOf(matrixOrItActors));
        Iterable<Actor> matrixAndItActors = actorController.filterBy("{movies: " + matrix.getId()+ ", movies: "+ it.getId() +"}", null, null);
        Assert.assertEquals(0, IterableUtil.sizeOf(matrixAndItActors));
        Iterable<Actor> matrixAndConstantineActors = actorController.filterBy("{movies: " + matrix.getId()+ ", movies: "+ constantine.getId() +"}", null, null);
        Assert.assertEquals(1, IterableUtil.sizeOf(matrixAndConstantineActors));


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
}
