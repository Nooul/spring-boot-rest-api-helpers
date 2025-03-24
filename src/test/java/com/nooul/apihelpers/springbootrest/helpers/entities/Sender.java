package com.nooul.apihelpers.springbootrest.helpers.entities;

import com.nooul.apihelpers.springbootrest.helpers.values.Mobile;
import com.nooul.apihelpers.springbootrest.helpers.values.MobileConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sender {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    private String sender;

    @Convert(converter = MobileConverter.class)
    private Mobile senderValueObject;
}
