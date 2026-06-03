package es.hugoalvarezajenjo.selecta.ui.subject.user.list;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectListController {
    private final SubjectService subjectService;

    private static final int PAGE_SIZE = 9;

    @GetMapping
    private String subjectListView(
            @RequestParam(value = "search", required = false) final String searchQuery,
            @RequestParam(value = "semester", required = false) final Integer semester,
            @RequestParam(value = "language", required = false) final String language,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            final Model model) {

        final Page<Subject> subjectPage = this.subjectService.findActiveBySearchQuery(
                searchQuery, semester, language, PageRequest.of(Math.max(0, page), PAGE_SIZE));

        model.addAttribute("subjects", subjectPage.getContent().stream()
                .map(SubjectListController::mapToDTO).toList());
        model.addAttribute("currentPage", subjectPage.getNumber());
        model.addAttribute("totalPages", subjectPage.getTotalPages());
        model.addAttribute("totalElements", subjectPage.getTotalElements());
        model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("selectedLanguage", language != null ? language : "");
        model.addAttribute("semesters", Semester.values());
        model.addAttribute("languages", Languages.values());

        return "subject/user/list";
    }

    private static SubjectListItemDTO mapToDTO(final Subject subject) {
        final List<String> attributesList = new ArrayList<>();
        attributesList.add(subject.getCredits() + " ECTS");
        for (final Semester semester : subject.getSemesters()) {
            attributesList.add("Semestre " + semester.toString());
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