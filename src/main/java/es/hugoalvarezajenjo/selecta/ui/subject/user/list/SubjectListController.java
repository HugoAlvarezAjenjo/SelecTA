package es.hugoalvarezajenjo.selecta.ui.subject.user.list;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectListController {
    final SubjectService subjectService;

    @GetMapping
    private String subjectListView(final Model model) {
        model.addAttribute(
                "subjects",
                this.subjectService.getAllSubjects().stream().map(SubjectListController::mapToDTO).toList()
        );
        return "subject/user/list";
    }

    private static SubjectListItemDTO mapToDTO(final Subject subject) {
        final List<String> attributesList = new ArrayList<>();
        attributesList.add(subject.getCredits() + " ects");
        for (final Semester semester : subject.getSemesters()) {
            attributesList.add(semester.toString() + " semester");
        }
        for (final Languages language : subject.getLanguages()) {
            attributesList.add(language.toString());
        }
        return new SubjectListItemDTO(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                attributesList
        );
    }
}
