package es.hugoalvarezajenjo.selecta.ui.subject.admin;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectAdminMapper {
    @Mapping(target = "teacherIds", ignore = true)
    SubjectAdminDTO toDTO(Subject subject);

    @Mapping(target = "teachers", ignore = true)
    @Mapping(target = "longDescription", source = "longDescription")
    @Mapping(target = "tags", source = "tags")
    Subject toDomain(SubjectAdminDTO dto);
}
