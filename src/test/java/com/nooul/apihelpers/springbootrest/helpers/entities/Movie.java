package com.nooul.apihelpers.springbootrest.helpers.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
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

    private Timestamp releaseDate;

    private Instant releaseDateInstant;

    @ManyToOne
    private Director director;

    @ManyToOne
    private Category category;

    @ManyToMany(mappedBy = "movies")
    private List<Actor> actors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "age_ratings")
    @Column(name = "age_rating")
    private Set<String> ageRatings = new HashSet<>();


}
