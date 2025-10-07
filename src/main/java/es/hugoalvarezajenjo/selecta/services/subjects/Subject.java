package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Languages> languages = new HashSet<>();
    private int credits;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Semester> semesters = new HashSet<>();
}
