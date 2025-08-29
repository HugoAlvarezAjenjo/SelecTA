package es.hugoalvarezajenjo.selecta.ui.subject.user.list;

import lombok.Value;

import java.util.List;

@Value
public class SubjectListItemDTO {
    private Long id;
    private String name;
    private String description;
    private List<String> attributes;
}
