package es.hugoalvarezajenjo.selecta.ui.subject.user.enrollmentlist;

import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListItem;
import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for the enrollment priority list feature.
 * Provides page rendering and REST API endpoints for drag-and-drop reordering.
 */
@Slf4j
@Controller
@RequestMapping("/enrollment-list")
@RequiredArgsConstructor
public class EnrollmentListController {

    private final EnrollmentListService enrollmentListService;
    private final UserService userService;

    /**
     * Renders the enrollment list page with main and reserve sections.
     */
    @GetMapping
    public String showEnrollmentList(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<EnrollmentListItem> mainItems = enrollmentListService.getMainList(user.getId());
        List<EnrollmentListItem> reserveItems = enrollmentListService.getReserveList(user.getId());

        model.addAttribute("mainItems", mainItems);
        model.addAttribute("reserveItems", reserveItems);
        model.addAttribute("mainCredits", enrollmentListService.getMainCredits(user.getId()));
        model.addAttribute("reserveCredits", enrollmentListService.getReserveCredits(user.getId()));
        model.addAttribute("mainCount", mainItems.size());
        model.addAttribute("reserveCount", reserveItems.size());

        return "subject/user/enrollment-list";
    }

    /**
     * REST: Add a subject to the enrollment list.
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addSubject(@RequestParam Long subjectId,
                                        @RequestParam(defaultValue = "false") boolean asReserve) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        boolean added = enrollmentListService.addSubject(user, subjectId, asReserve);
        if (!added) {
            return ResponseEntity.badRequest().body(Map.of("error", "La asignatura ya está en tu lista"));
        }

        String listName = asReserve ? "reservas" : "lista de matrícula";
        return ResponseEntity.ok(Map.of("success", true, "message", "Asignatura añadida a " + listName));
    }

    /**
     * REST: Remove a subject from the enrollment list.
     */
    @DeleteMapping("/remove/{subjectId}")
    @ResponseBody
    public ResponseEntity<?> removeSubject(@PathVariable Long subjectId) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        enrollmentListService.removeSubject(user.getId(), subjectId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "mainCredits", enrollmentListService.getMainCredits(user.getId()),
                "reserveCredits", enrollmentListService.getReserveCredits(user.getId())
        ));
    }

    /**
     * REST: Toggle a subject between main list and reserve.
     */
    @PutMapping("/toggle-reserve/{subjectId}")
    @ResponseBody
    public ResponseEntity<?> toggleReserve(@PathVariable Long subjectId) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        enrollmentListService.toggleReserve(user.getId(), subjectId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * REST: Reorder a list (called after drag-and-drop).
     */
    @PutMapping("/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderList(@RequestBody ReorderRequest request) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        boolean isReserve = request.isReserve();
        enrollmentListService.reorder(user.getId(), request.getOrderedSubjectIds(), isReserve);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * REST: Update the note for a specific item.
     */
    @PutMapping("/note/{subjectId}")
    @ResponseBody
    public ResponseEntity<?> updateNote(@PathVariable Long subjectId, @RequestBody NoteRequest request) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        enrollmentListService.updateNote(user.getId(), subjectId, request.getNote());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * REST: Check if a subject is in the user's list.
     */
    @GetMapping("/check/{subjectId}")
    @ResponseBody
    public ResponseEntity<?> checkInList(@PathVariable Long subjectId) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(Map.of("inList", false));
        }

        boolean inList = enrollmentListService.isInList(user.getId(), subjectId);
        return ResponseEntity.ok(Map.of("inList", inList));
    }
}
