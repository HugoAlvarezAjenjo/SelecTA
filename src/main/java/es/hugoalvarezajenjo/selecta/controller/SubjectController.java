package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.entity.Subject;
import es.hugoalvarezajenjo.selecta.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

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

    @GetMapping("/new")
    public String newSubjectForm(final Model model) {
        model.addAttribute("subject", new Subject());
        return "admin/subject/form";
    }

    @PostMapping("/new")
    public String saveSubject(final Subject subject) {
        this.subjectService.saveSubject(subject);
        return "redirect:/admin/subject";
    }

    @GetMapping("/edit/{id}")
    public String editSubjectForm(@PathVariable final Long id, Model model) {
        Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isPresent()) {
            model.addAttribute("subject", subject.get());
            return "admin/subject/form";
        } else {
            return "admin/subject/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateSubject(@PathVariable final Long id, final Subject subject) {
        subject.setId(id);
        this.subjectService.saveSubject(subject);
        return "redirect:/admin/subject";
    }

    @GetMapping("/delete/{id}")
    public String deleteSubject(@PathVariable final Long id) {
        this.subjectService.deleteSubjectById(id);
        return "redirect:/admin/subject";
    }

    @GetMapping("/view/{id}")
    public String viewSubject(@PathVariable final Long id, final Model model) {
        Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isPresent()) {
            model.addAttribute("subject", subject.get());
            return "admin/subject/view";
        } else {
            return "admin/subject/list";
        }
    }
}
