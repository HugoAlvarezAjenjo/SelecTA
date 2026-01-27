package es.hugoalvarezajenjo.selecta.ui.subject.user.list;

import es.hugoalvarezajenjo.selecta.config.FeatureFlagConfig;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectListController {
    final SubjectService subjectService;
    private final FeatureFlagConfig featureFlagConfig;

    @GetMapping
    private String subjectListView(
            @RequestParam(value = "search", required = false) final String searchQuery,
            final Model model) {

        List<Subject> filteredSubjects;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            filteredSubjects = this.subjectService.findActiveBySearchQuery(searchQuery);
        } else {
            filteredSubjects = this.subjectService.getActiveSubjects();
        }

        model.addAttribute("subjects", filteredSubjects.stream()
                .map(SubjectListController::mapToDTO).toList());
        model.addAttribute("filterMenuEnabled", this.featureFlagConfig.isFilterListEnabled());
        model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");

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
                attributesList);
    }
}