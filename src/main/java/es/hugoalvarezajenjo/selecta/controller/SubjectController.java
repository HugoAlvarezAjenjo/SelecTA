package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/subject")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(@Autowired final SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public String getSubjects(final Model model) {
        model.addAttribute("subjects", this.subjectService.getAllSubjects());
        return "admin/subject/list";
    }
}
