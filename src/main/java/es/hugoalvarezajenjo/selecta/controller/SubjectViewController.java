package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.dto.SubjectItemDTO;
import es.hugoalvarezajenjo.selecta.entity.Subject;
import es.hugoalvarezajenjo.selecta.mapper.SubjectMapper;
import es.hugoalvarezajenjo.selecta.service.SubjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/subjects")
public class SubjectViewController {
    private final SubjectMapper subjectMapper;
    private final SubjectService subjectService;

    public SubjectViewController(final SubjectMapper subjectMapper, final SubjectService subjectService) {
        this.subjectMapper = subjectMapper;
        this.subjectService = subjectService;
    }

    @GetMapping()
    public String subjectView(Model model) {
        List<SubjectItemDTO> subjectDTOs = subjectMapper.subjectsToSubjectItemDTOs(subjectService.getAllSubjects());
        model.addAttribute("subjects", subjectDTOs);
        return "public/subject/list";
    }

    @GetMapping("/{id}")
    public String subjectView(final Model model, @PathVariable final long id) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isPresent()) {
            model.addAttribute("subject", this.subjectMapper.subjectToSubjectDto(subject.get()));
            return "public/subject/view-modern";
        }
        return "public/subject/list";
    }

}
