package com.nooul.apihelpers.springbootrest.helpers.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class UUIDRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, length = 100, updatable = false)
    protected UUID uuid;

    @ManyToOne
    @JoinColumn(name = "uuiduuid", referencedColumnName = "uuid")
    private UUIDEntity uuidEntity;
}
