package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Column(columnDefinition = "TEXT")
    private String longDescription;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Languages> languages = new HashSet<>();
    private int credits;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Semester> semesters = new HashSet<>();
    private boolean discontinued = false;
    @ElementCollection
    private Set<String> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "subject_teachers",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private Set<Teacher> teachers = new HashSet<>();
}
