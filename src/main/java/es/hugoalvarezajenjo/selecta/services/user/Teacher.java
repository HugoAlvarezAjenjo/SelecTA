package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("TEACHER")
@Getter
@Setter
public class Teacher extends User {
    
    @ManyToMany(mappedBy = "teachers", fetch = FetchType.EAGER)
    private Set<Subject> subjects = new HashSet<>();

    public Teacher() {
        this.setRole(UserRole.TEACHER);
    }
}
