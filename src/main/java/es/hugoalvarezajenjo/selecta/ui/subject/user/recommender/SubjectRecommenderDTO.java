package es.hugoalvarezajenjo.selecta.ui.subject.user.recommender;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SubjectRecommenderDTO {
    private List<String> semesterTypes;
    private Languages language;
    private Integer maxCredits;
    private String searchKeywords;
}
