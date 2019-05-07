package reactAdmin.rest.integration;

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
import reactAdmin.rest.helpers.controllers.MovieController;
import reactAdmin.rest.helpers.entities.Actor;
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
    public void testManyToMany_fetch_movie_by_actor() {
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
    public void fetch_by_ids__fetch_movies_by_ids() {
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
    public void fetch_by_id__fetch_movie_by_exact_match_of_name() {
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
    public void full_text_search() {
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
