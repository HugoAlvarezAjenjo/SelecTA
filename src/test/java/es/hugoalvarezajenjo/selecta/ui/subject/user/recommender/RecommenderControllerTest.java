package es.hugoalvarezajenjo.selecta.ui.subject.user.recommender;

import es.hugoalvarezajenjo.selecta.services.recommendation.RecommendationEngine;
import es.hugoalvarezajenjo.selecta.services.recommendation.SubjectScoreDTO;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRecommendationCriteria;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommenderControllerTest {

    @Mock
    private SubjectService subjectService;

    @Mock
    private RecommendationEngine recommendationEngine;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private RecommenderController controller;

    @Nested
    @DisplayName("GET /recommender")
    class ShowFormTests {

        @Test
        @DisplayName("Returns recommender view with empty DTO and available tags")
        void returnsFormView() {
            Subject s = new Subject();
            s.setTags(Set.of("AI", "ML"));
            when(subjectService.getActiveSubjects()).thenReturn(List.of(s));

            String result = controller.recommenderForm(model);

            assertThat(result).isEqualTo("subject/user/recommender");
            verify(model).addAttribute(eq("recommenderDTO"), any(SubjectRecommenderDTO.class));
            verify(model).addAttribute(eq("allLanguages"), eq(Languages.values()));
            verify(model).addAttribute(eq("availableTags"), any());
        }
    }

    @Nested
    @DisplayName("POST /recommender")
    class RecommendTests {

        @Test
        @DisplayName("Returns recommendations for anonymous user")
        void returnsRecommendationsForAnonymous() {
            when(userService.getCurrentUser()).thenReturn(null);
            when(subjectService.getActiveSubjects()).thenReturn(List.of());

            SubjectScoreDTO dto = SubjectScoreDTO.builder()
                    .subjectId(1L).name("AI").totalScore(0.8).matchPercentage(80)
                    .signalBreakdown(Map.of()).explanation("Popular").tags(Set.of("AI"))
                    .attributes(List.of()).build();
            when(recommendationEngine.recommend(eq(null), any(), eq(20))).thenReturn(List.of(dto));

            SubjectRecommenderDTO formDto = new SubjectRecommenderDTO();
            formDto.setSelectedTags(List.of("AI"));

            String result = controller.recommendSubjects(formDto, model);

            assertThat(result).isEqualTo("subject/user/recommender");
            verify(model).addAttribute(eq("recommendations"), any(List.class));
            verify(model).addAttribute("isLoggedIn", false);
        }

        @Test
        @DisplayName("Returns recommendations for logged-in user")
        void returnsRecommendationsForLoggedInUser() {
            Student student = new Student();
            student.setId(1L);
            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectService.getActiveSubjects()).thenReturn(List.of());
            when(recommendationEngine.recommend(eq(student), any(), eq(20))).thenReturn(List.of());

            SubjectRecommenderDTO formDto = new SubjectRecommenderDTO();
            formDto.setLanguage(Languages.SPANISH);

            String result = controller.recommendSubjects(formDto, model);

            assertThat(result).isEqualTo("subject/user/recommender");
            verify(model).addAttribute("isLoggedIn", true);
            verify(recommendationEngine).recommend(eq(student), any(SubjectRecommendationCriteria.class), eq(20));
        }

        @Test
        @DisplayName("Passes criteria correctly from DTO to engine")
        void passesCriteriaCorrectly() {
            when(userService.getCurrentUser()).thenReturn(null);
            when(subjectService.getActiveSubjects()).thenReturn(List.of());
            when(recommendationEngine.recommend(any(), any(), anyInt())).thenReturn(List.of());

            SubjectRecommenderDTO formDto = new SubjectRecommenderDTO();
            formDto.setSelectedTags(List.of("AI", "ML"));
            formDto.setLanguage(Languages.ENGLISH);
            formDto.setMaxCredits(6);
            formDto.setSemesterTypes(List.of("ODD"));

            controller.recommendSubjects(formDto, model);

            verify(recommendationEngine).recommend(isNull(), argThat(criteria ->
                    criteria.getSelectedTags().equals(List.of("AI", "ML")) &&
                    criteria.getLanguage() == Languages.ENGLISH &&
                    criteria.getMaxCredits() == 6 &&
                    criteria.getSemesterTypes().contains("ODD")
            ), eq(20));
        }
    }
}
