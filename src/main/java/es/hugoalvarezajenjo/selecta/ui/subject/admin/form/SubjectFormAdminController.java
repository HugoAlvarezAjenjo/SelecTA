package es.hugoalvarezajenjo.selecta.ui.subject.admin.form;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.ui.subject.admin.SubjectAdminDTO;
import es.hugoalvarezajenjo.selecta.ui.subject.admin.SubjectAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/subjects")
@RequiredArgsConstructor
public class SubjectFormAdminController {
    private final SubjectService subjectService;
    private final SubjectAdminMapper mapper;

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("subject", new SubjectAdminDTO());
        model.addAttribute("isEdit", false);
        return "subject/admin/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return this.subjectService.getSubjectById(id)
                .map(this.mapper::toDTO)
                .map(dto -> {
                    model.addAttribute("subject", dto);
                    model.addAttribute("isEdit", true);
                    return "subject/admin/form";
                })
                .orElse("redirect:/admin/subjects");
    }

    @PostMapping("/save")
    public String saveSubject(@ModelAttribute("subject") SubjectAdminDTO subjectDTO) {
        this.subjectService.saveSubject(this.mapper.toDomain(subjectDTO));
        return "redirect:/admin/subjects";
    }
}
