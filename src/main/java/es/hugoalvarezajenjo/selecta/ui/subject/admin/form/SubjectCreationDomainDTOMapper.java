package es.hugoalvarezajenjo.selecta.ui.subject.admin.form;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectCreationDomainDTOMapper {
    @Mapping(target = "teacherIds", ignore = true)
    SubjectCreationDTO toDTO(Subject subject);

    @Mapping(target = "teachers", ignore = true)
    @Mapping(target = "longDescription", source = "longDescription")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "discontinued", ignore = true)
    Subject toDomain(SubjectCreationDTO dto);
}
