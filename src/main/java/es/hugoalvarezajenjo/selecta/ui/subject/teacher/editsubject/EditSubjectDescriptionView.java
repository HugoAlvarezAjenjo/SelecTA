package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/edit/subject")
@RequiredArgsConstructor
public class EditSubjectDescriptionView {
    private final SubjectService subjectService;

    @GetMapping("/{id}")
    public String editSubjectDescriptionView(@PathVariable final Long id, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isEmpty()) {
            return "subject/user/no-subject";
        }
        model.addAttribute("subject", EditSubjectDescriptionDTO.createFromDomain(subject.get()));
        return "subject/teacher/edit-subject-description";
    }

    @PostMapping("/{id}")
    public String updateSubjectDescription(
            @PathVariable final Long id,
            @RequestParam String shortDescription,
            @RequestParam String longDescription,
            final RedirectAttributes redirectAttributes) {

        final Optional<Subject> subjectOpt = this.subjectService.getSubjectById(id);
        if (subjectOpt.isEmpty()) {
            return "subject/user/no-subject";
        }

        Subject subject = subjectOpt.get();
        // Update the subject with new descriptions
        subject.setDescription(shortDescription);
        // subject.setLongDescription(longDescription);

        // Save the updated subject
        this.subjectService.saveSubject(subject);

        redirectAttributes.addFlashAttribute("success", "Descripción actualizada correctamente");
        return "redirect:/subject/" + id;
    }
}