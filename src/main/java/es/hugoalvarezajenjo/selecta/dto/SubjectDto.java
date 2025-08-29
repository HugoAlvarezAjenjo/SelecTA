package es.hugoalvarezajenjo.selecta.dto;

import lombok.Value;

@Value
public class SubjectDto {
    private Long id;
    private String name;
    private float rating;
    private int numOpinions;
    private int numCredits;
    private String period;
    private String type;
    private String description;
}
