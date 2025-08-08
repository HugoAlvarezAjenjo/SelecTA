package es.hugoalvarezajenjo.selecta.ui.subject.admin.form;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubjectCreationDomainDTOMapper {
    SubjectCreationDTO toDTO(Subject subject);
    Subject toDomain(SubjectCreationDTO dto);
}
