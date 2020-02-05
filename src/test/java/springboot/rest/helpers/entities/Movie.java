package springboot.rest.helpers.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private int yearReleased;

    @ManyToOne
    private Director director;

    @ManyToOne
    private Category category;

    @ManyToMany(mappedBy="movies")
    private List<Actor> actors = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private UUID uuid;

    @ElementCollection
    @CollectionTable(name = "age_ratings")
    @Column(name = "age_rating")
    private Set<String> ageRatings = new HashSet<>();



}
