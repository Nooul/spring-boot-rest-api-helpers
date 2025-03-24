package com.nooul.apihelpers.springbootrest.helpers.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UUID {
    @Id
    private String uuid;

    public UUID(String uuid) {
        this.uuid = uuid;
    }
}
