package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.user.User;

import java.util.Optional;

public interface SubjectRatingService {

    /**
     * Returns the average rating for a subject.
     */
    Double getAverageRating(Long subjectId);

    /**
     * Returns the total number of ratings for a subject.
     */
    long getRatingCount(Long subjectId);

    /**
     * Returns the user's own rating for a subject, if it exists.
     */
    Optional<SubjectRating> getUserRating(Long subjectId, Long userId);

    /**
     * Creates or updates a user's rating for a subject.
     * If the same rating is submitted again, it is toggled off (deleted).
     *
     * @return the saved or deleted rating state
     */
    SubjectRating setRating(Long subjectId, User user, int rating);

    /**
     * Deletes a rating entity.
     */
    void deleteRating(SubjectRating rating);
}
