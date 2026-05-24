package es.hugoalvarezajenjo.selecta.ui.subject.user.recommender;

import es.hugoalvarezajenjo.selecta.services.recommendation.RecommendationEngine;
import es.hugoalvarezajenjo.selecta.services.recommendation.SubjectScoreDTO;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRecommendationCriteria;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recommender")
@RequiredArgsConstructor
public class RecommenderController {

    private static final int MAX_RECOMMENDATIONS = 20;

    private final SubjectService subjectService;
    private final RecommendationEngine recommendationEngine;
    private final UserService userService;

    @GetMapping
    public String recommenderForm(Model model) {
        model.addAttribute("recommenderDTO", new SubjectRecommenderDTO());
        populateModelAttributes(model);
        return "subject/user/recommender";
    }

    @PostMapping
    public String recommendSubjects(@ModelAttribute("recommenderDTO") SubjectRecommenderDTO dto, Model model) {
        User currentUser = userService.getCurrentUser();

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder()
                .semesterTypes(dto.getSemesterTypes())
                .language(dto.getLanguage())
                .maxCredits(dto.getMaxCredits())
                .searchKeywords(dto.getSearchKeywords())
                .selectedTags(dto.getSelectedTags())
                .build();

        List<SubjectScoreDTO> results = recommendationEngine.recommend(currentUser, criteria, MAX_RECOMMENDATIONS);

        model.addAttribute("recommenderDTO", dto);
        model.addAttribute("recommendations", results);
        model.addAttribute("isLoggedIn", currentUser != null);
        populateModelAttributes(model);

        return "subject/user/recommender";
    }

    /**
     * Populates shared model attributes (languages, available tags).
     */
    private void populateModelAttributes(Model model) {
        model.addAttribute("allLanguages", Languages.values());
        model.addAttribute("availableTags", getAvailableTags());
    }

    /**
     * Collects all unique tags from active subjects for the tag chips UI.
     */
    private Set<String> getAvailableTags() {
        return subjectService.getActiveSubjects().stream()
                .filter(s -> s.getTags() != null)
                .flatMap(s -> s.getTags().stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
