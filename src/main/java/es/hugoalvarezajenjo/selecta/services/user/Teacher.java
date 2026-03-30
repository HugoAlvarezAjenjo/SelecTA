package es.hugoalvarezajenjo.selecta.services.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEACHER")
public class Teacher extends User {
    // List of subjects will be implemented in future iterations
    // private List<String> subjects;

    public Teacher() {
        this.setRole(UserRole.TEACHER);
    }
}
