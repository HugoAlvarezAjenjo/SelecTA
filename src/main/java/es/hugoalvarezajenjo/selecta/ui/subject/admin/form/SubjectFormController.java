package es.hugoalvarezajenjo.selecta.ui.subject.admin.form;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/path")
@RequiredArgsConstructor
public class SubjectFormController {
    final SubjectCreationDomainDTOMapper mapper;
    final SubjectService subjectService;

    @GetMapping
    public String subjectForm(Model model) {
        final SubjectCreationDTO subject = new SubjectCreationDTO();
        model.addAttribute("subject", subject);
        return "/subject/admin/form";
    }

    @PostMapping
    public String submitSubjectForm(@ModelAttribute("subject") SubjectCreationDTO subjectDTO, Model model) {
        this.subjectService.saveSubject(
                this.mapper.toDomain(subjectDTO)
        );
        return "redirect:/test/path"; // Or redirect somewhere: return "redirect:/somewhere";
    }

}
