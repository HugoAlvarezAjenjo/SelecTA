package es.hugoalvarezajenjo.selecta.services.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    public Admin() {
        this.setRole(UserRole.ADMIN);
    }
}
