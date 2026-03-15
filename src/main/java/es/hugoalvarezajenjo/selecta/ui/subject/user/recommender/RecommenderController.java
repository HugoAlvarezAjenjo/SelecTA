package es.hugoalvarezajenjo.selecta.ui.subject.user.recommender;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview.SubjectInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recommender")
@RequiredArgsConstructor
public class RecommenderController {
    
    private final SubjectService subjectService;

    @GetMapping
    public String recommenderForm(Model model) {
        model.addAttribute("recommenderDTO", new SubjectRecommenderDTO());
        model.addAttribute("allSemesters", Semester.values());
        model.addAttribute("allLanguages", Languages.values());
        return "subject/user/recommender";
    }

    @PostMapping
    public String recommendSubjects(@ModelAttribute("recommenderDTO") SubjectRecommenderDTO dto, Model model) {
        List<Subject> results = subjectService.recommendSubjects(dto);
        List<SubjectInfoDTO> recommendedDTOs = results.stream()
                .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                .toList();
        
        model.addAttribute("recommenderDTO", dto);
        model.addAttribute("allSemesters", Semester.values());
        model.addAttribute("allLanguages", Languages.values());
        model.addAttribute("recommendedSubjects", recommendedDTOs);
        
        return "subject/user/recommender";
    }
}
