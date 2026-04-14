package es.hugoalvarezajenjo.selecta.ui.subject.admin.form;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class SubjectCreationDomainDTOMapper {

    public SubjectCreationDTO toDTO(final Subject subject) {
        if (subject == null) {
            return null;
        }
        final SubjectCreationDTO dto = new SubjectCreationDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setCredits(subject.getCredits());
        dto.setLanguages(subject.getLanguages() != null ? new HashSet<>(subject.getLanguages()) : new HashSet<>());
        dto.setSemesters(subject.getSemesters() != null ? new HashSet<>(subject.getSemesters()) : new HashSet<>());
        dto.setLongDescription(subject.getLongDescription());
        dto.setTags(subject.getTags() != null ? new HashSet<>(subject.getTags()) : new HashSet<>());
        // teacherIds not mapped from domain
        return dto;
    }

    public Subject toDomain(final SubjectCreationDTO dto) {
        if (dto == null) {
            return null;
        }
        final Subject subject = new Subject();
        subject.setId(dto.getId());
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        subject.setCredits(dto.getCredits());
        subject.setLanguages(dto.getLanguages() != null ? new HashSet<>(dto.getLanguages()) : new HashSet<>());
        subject.setSemesters(dto.getSemesters() != null ? new HashSet<>(dto.getSemesters()) : new HashSet<>());
        subject.setLongDescription(dto.getLongDescription());
        subject.setTags(dto.getTags() != null ? new HashSet<>(dto.getTags()) : new HashSet<>());
        // teachers not mapped from DTO
        // discontinued not mapped from creation DTO
        return subject;
    }
}
