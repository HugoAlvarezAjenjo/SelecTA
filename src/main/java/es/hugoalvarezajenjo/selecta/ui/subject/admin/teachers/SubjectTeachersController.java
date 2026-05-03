package es.hugoalvarezajenjo.selecta.ui.subject.admin.teachers;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/subjects/{subjectId}/teachers")
@RequiredArgsConstructor
public class SubjectTeachersController {

    private final SubjectService subjectService;
    private final UserService userService;

    @GetMapping
    public String manageTeachers(@PathVariable final Long subjectId, final Model model) {
        final Optional<Subject> subjectOpt = subjectService.getSubjectById(subjectId);
        if (subjectOpt.isEmpty()) {
            return "redirect:/admin/subjects";
        }

        final Subject subject = subjectOpt.get();
        final Set<Long> assignedTeacherIds = subject.getTeachers().stream()
                .map(Teacher::getId)
                .collect(Collectors.toSet());

        // Get all approved teachers
        final List<User> allTeachers = userService.getApprovedTeachers();

        // Split into assigned and available
        final List<TeacherDTO> assignedTeachers = allTeachers.stream()
                .filter(t -> assignedTeacherIds.contains(t.getId()))
                .map(TeacherDTO::fromUser)
                .toList();

        final List<TeacherDTO> availableTeachers = allTeachers.stream()
                .filter(t -> !assignedTeacherIds.contains(t.getId()))
                .map(TeacherDTO::fromUser)
                .toList();

        model.addAttribute("subject", subject);
        model.addAttribute("assignedTeachers", assignedTeachers);
        model.addAttribute("availableTeachers", availableTeachers);

        return "subject/admin/teachers";
    }

    @PostMapping("/add")
    public String addTeacher(@PathVariable final Long subjectId,
                             @RequestParam final Long teacherId) {
        subjectService.addTeacherToSubject(subjectId, teacherId);
        return "redirect:/admin/subjects/" + subjectId + "/teachers?added";
    }

    @PostMapping("/{teacherId}/remove")
    public String removeTeacher(@PathVariable final Long subjectId,
                                @PathVariable final Long teacherId) {
        subjectService.removeTeacherFromSubject(subjectId, teacherId);
        return "redirect:/admin/subjects/" + subjectId + "/teachers?removed";
    }
}
