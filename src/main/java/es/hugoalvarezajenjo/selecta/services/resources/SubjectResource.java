package es.hugoalvarezajenjo.selecta.services.resources;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class SubjectResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long subjectId;
    private String name;
    private String description;
    private LocalDate creationDate;
    @Enumerated(EnumType.STRING)
    private ResourceType type;
    private String language;
    private String originalName;
}
