package com.nooul.apihelpers.springbootrest.testapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class UUIDRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, length = 100, updatable = false)
    @Type(type = "uuid-char")
    protected UUID uuid;

    @ManyToOne
    @JoinColumn(name = "uuiduuid", referencedColumnName = "uuid")
    private UUIDEntity uuidEntity;
}
