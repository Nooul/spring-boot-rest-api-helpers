package reactAdmin.rest.helpers.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

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
