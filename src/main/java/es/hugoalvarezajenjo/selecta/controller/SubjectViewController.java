package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.entity.Subject;
import es.hugoalvarezajenjo.selecta.service.SubjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/subjects")
public class SubjectViewController {
    private final SubjectService subjectService;

    public SubjectViewController(final SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public String subjectsView(final Model model) {
        model.addAttribute("subjects", this.subjectService.getAllSubjects());
        return "public/subject/list";
    }

    @GetMapping("/{id}")
    public String subjectView(final Model model, @PathVariable final long id) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isPresent()) {
            model.addAttribute("subject", subject.get());
            return "public/subject/view";
        }
        return "public/subject/list";
    }
}
