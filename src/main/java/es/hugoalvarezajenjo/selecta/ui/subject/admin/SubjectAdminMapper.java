package es.hugoalvarezajenjo.selecta.ui.subject.admin;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubjectAdminMapper {
    SubjectAdminDTO toDTO(Subject subject);

    Subject toDomain(SubjectAdminDTO dto);
}
