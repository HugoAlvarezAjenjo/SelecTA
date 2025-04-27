package es.hugoalvarezajenjo.selecta.mapper;

import es.hugoalvarezajenjo.selecta.dto.SubjectDto;
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

    @Mapping(target = "rating", constant = "0.0f")  // Usamos la constante para rating
    @Mapping(target = "numOpinions", constant = "0")  // Usamos la constante para numOpinions
    @Mapping(target = "numCredits", constant = "0")  // Usamos la constante para numCredits
    @Mapping(target = "period", constant = "No definido")  // Usamos la constante para period
    @Mapping(target = "type", constant = "No definido")  // Usamos la constante para type
    SubjectDto subjectToSubjectDto(Subject subject);

    List<SubjectDto> subjectsTSubjectDto(List<Subject> subjects);
}