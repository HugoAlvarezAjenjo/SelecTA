package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.config.FeatureFlagConfig;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectViewController {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final FeatureFlagConfig featureFlagConfig;

    @GetMapping("/{id}")
    public String subjectView(@PathVariable final Long id, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isEmpty()) {
            return "subject/user/no-subject";
        }
        setFeatureFlags(model);
        model.addAttribute("subject", SubjectViewController.mapToDTO(subject.get()));
        model.addAttribute("resources", this.subjectResourceService.getResourcesFromSubject(id));
        return "subject/user/subject-view";
    }

    private void setFeatureFlags(final Model model) {
        model.addAttribute("subjectResourcesEnabled", this.featureFlagConfig.isSubjectResourceEnabled());
    }

    private static SubjectInfoDTO mapToDTO(final Subject subject) {
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
                attributesList
        );
    }
}
