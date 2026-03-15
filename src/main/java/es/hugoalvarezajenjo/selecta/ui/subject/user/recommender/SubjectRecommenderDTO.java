package es.hugoalvarezajenjo.selecta.ui.subject.user.recommender;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SubjectRecommenderDTO {
    private List<Semester> semesters;
    private Languages language;
    private Integer maxCredits;
    private String searchKeywords;
}
