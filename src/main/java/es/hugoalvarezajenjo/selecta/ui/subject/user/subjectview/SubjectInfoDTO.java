package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class SubjectInfoDTO {
    private Long id;
    private String name;
    private String description;
    private String longDescriptionHtml;
    private Iterable<String> attributes;

    public static SubjectInfoDTO createFromDomain(final Subject subject, final String longDescriptionHtml) {
        final List<String> attributesList = new ArrayList<>();
        attributesList.add(subject.getCredits() + " ects");
        for (final Semester semester : subject.getSemesters()) {
            attributesList.add(semester.toString() + " semester");
        }
        for (final Languages language : subject.getLanguages()) {
            attributesList.add(language.toString());
        }
        return new SubjectInfoDTO(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                longDescriptionHtml,
                attributesList);
    }
}
