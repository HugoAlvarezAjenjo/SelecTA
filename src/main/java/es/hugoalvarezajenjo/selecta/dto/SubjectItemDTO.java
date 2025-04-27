package es.hugoalvarezajenjo.selecta.dto;

import lombok.Builder;
import lombok.Value;

@Value
public class SubjectItemDTO {
    private Long id;
    private String name;
    private String description;
    private int credits;
    private String type;
    private String period;
    private int numOpinions;
    private int numResources;
    private String degree;
    private float averageGrade;
}
