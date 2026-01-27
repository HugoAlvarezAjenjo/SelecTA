package es.hugoalvarezajenjo.selecta.ui.subject.admin;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.Data;

import java.util.Set;

@Data
public class SubjectAdminDTO {
    private Long id;
    private String name;
    private String description;
    private int credits;
    private Set<Languages> languages;
    private Set<Semester> semesters;
    private boolean discontinued;
}
