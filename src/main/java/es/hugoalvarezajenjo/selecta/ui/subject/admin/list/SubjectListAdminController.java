package es.hugoalvarezajenjo.selecta.ui.subject.admin.list;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.ui.subject.admin.SubjectAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/subjects")
@RequiredArgsConstructor
public class SubjectListAdminController {
    private final SubjectService subjectService;
    private final SubjectAdminMapper mapper;

    @GetMapping
    public String listSubjects(Model model) {
        model.addAttribute("subjects", this.subjectService.getAllSubjects().stream()
                .map(this.mapper::toDTO)
                .collect(Collectors.toList()));
        return "subject/admin/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable Long id) {
        this.subjectService.deleteSubjectById(id);
        return "redirect:/admin/subjects";
    }
}
