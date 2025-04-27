package es.hugoalvarezajenjo.selecta.mapper;

import es.hugoalvarezajenjo.selecta.dto.SubjectItemDTO;
import es.hugoalvarezajenjo.selecta.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "credits", constant = "0")
    @Mapping(target = "type", constant = "Unknown")
    @Mapping(target = "period", constant = "Unknown")
    @Mapping(target = "numOpinions", constant = "0")
    @Mapping(target = "numResources", constant = "0")
    @Mapping(target = "degree", constant = "Unknown")
    @Mapping(target = "averageGrade", constant = "0.0f")
    SubjectItemDTO subjectToSubjectItemDTO(Subject subject);
    List<SubjectItemDTO> subjectsToSubjectItemDTOs(List<Subject> subjects);
}