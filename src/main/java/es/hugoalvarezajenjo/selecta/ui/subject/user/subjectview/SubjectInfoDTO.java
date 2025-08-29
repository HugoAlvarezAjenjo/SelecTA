package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import lombok.Value;

@Value
public class SubjectInfoDTO {
    private Long id;
    private String name;
    private String description;
    private Iterable<String> attributes;
}
