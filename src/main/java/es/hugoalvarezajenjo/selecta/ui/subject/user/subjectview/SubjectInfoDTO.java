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
    private Iterable<String> tags;
    private Iterable<TeacherInfo> teachers;

    @Value
    public static class TeacherInfo {
        String name;
        String email;
    }

    public static SubjectInfoDTO createFromDomain(final Subject subject, final String longDescriptionHtml) {
        final List<String> attributesList = new ArrayList<>();
        attributesList.add(subject.getCredits() + " ects");
        for (final Semester semester : subject.getSemesters()) {
            attributesList.add(semester.toString() + " semester");
        }
        for (final Languages language : subject.getLanguages()) {
            attributesList.add(language.toString());
        }

        final List<TeacherInfo> teachersList = new ArrayList<>();
        if (subject.getTeachers() != null) {
            for (final var teacher : subject.getTeachers()) {
                teachersList.add(new TeacherInfo(teacher.getUsername(), teacher.getEmail()));
            }
        }

        return new SubjectInfoDTO(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                longDescriptionHtml,
                attributesList,
                subject.getTags(),
                teachersList);
    }
}
