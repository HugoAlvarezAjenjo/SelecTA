package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.types.Languages;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Service-layer criteria object for subject recommendation.
 * Decoupled from the UI layer DTO.
 */
@Getter
@Builder
public class SubjectRecommendationCriteria {
    private final List<String> semesterTypes;
    private final Languages language;
    private final Integer maxCredits;
    private final String searchKeywords;
    private final List<String> selectedTags;
}
