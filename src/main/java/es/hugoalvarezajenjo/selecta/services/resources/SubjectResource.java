package es.hugoalvarezajenjo.selecta.services.resources;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private ResourceType type;
    private String language;
    private String url;
}
