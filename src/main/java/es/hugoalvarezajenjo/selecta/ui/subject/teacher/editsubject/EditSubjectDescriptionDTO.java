package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor
public class EditSubjectDescriptionDTO {
    private Long id;
    private String subjectName;
    private String description;
    private String subjectInfo;
    private Iterable<String> attributes;

    public static EditSubjectDescriptionDTO createFromDomain(final Subject subject) {
        final List<String> attributesList = new ArrayList<>();
        attributesList.add(subject.getCredits() + " ects");
        for (final Semester semester : subject.getSemesters()) {
            attributesList.add(semester.toString() + " semester");
        }
        for (final Languages language : subject.getLanguages()) {
            attributesList.add(language.toString());
        }
        return new EditSubjectDescriptionDTO(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getLongDescription() != null ? subject.getLongDescription() : "",
                attributesList);
    }
}
