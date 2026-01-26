package es.hugoalvarezajenjo.selecta.services.resources;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    private boolean isPrivate = false;
}
