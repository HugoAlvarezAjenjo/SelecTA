package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    private String titulation;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "student_favourite_subjects",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> favouriteSubjects = new HashSet<>();

    public Student() {
        this.setRole(UserRole.STUDENT);
    }

    public String getTitulation() {
        return titulation;
    }

    public void setTitulation(String titulation) {
        this.titulation = titulation;
    }

    public Set<Subject> getFavouriteSubjects() {
        return favouriteSubjects;
    }

    public void setFavouriteSubjects(final Set<Subject> favouriteSubjects) {
        this.favouriteSubjects = favouriteSubjects;
    }
}
