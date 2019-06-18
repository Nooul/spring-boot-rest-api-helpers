package springboot.rest.helpers.entities;

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
        // this is for validation
        this.uuid = new UUID(uuid).toString();
    }

    public UUID toUUID() {
        return new UUID(uuid);
    }

    @Override
    public String toString() {
        return uuid;
    }
}
