package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("TEACHER")
@Getter
@Setter
public class Teacher extends User {
    
    @ManyToMany(mappedBy = "teachers", fetch = jakarta.persistence.FetchType.EAGER)
    private java.util.Set<Subject> subjects = new java.util.HashSet<>();

    public Teacher() {
        this.setRole(UserRole.TEACHER);
    }
}
