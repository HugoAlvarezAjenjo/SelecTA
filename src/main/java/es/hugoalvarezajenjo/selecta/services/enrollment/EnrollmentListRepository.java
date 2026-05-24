package es.hugoalvarezajenjo.selecta.services.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentListRepository extends JpaRepository<EnrollmentListItem, Long> {

    /** Get user's main enrollment list (non-reserve) ordered by position */
    List<EnrollmentListItem> findByUserIdAndReserveFalseOrderByPositionAsc(Long userId);

    /** Get user's reserve list ordered by position */
    List<EnrollmentListItem> findByUserIdAndReserveTrueOrderByPositionAsc(Long userId);

    /** Get all items for a user ordered by position */
    List<EnrollmentListItem> findByUserIdOrderByPositionAsc(Long userId);

    /** Check if a subject is already in the user's list (either main or reserve) */
    Optional<EnrollmentListItem> findByUserIdAndSubjectId(Long userId, Long subjectId);

    /** Check existence */
    boolean existsByUserIdAndSubjectId(Long userId, Long subjectId);

    /** Count items in user's main list */
    int countByUserIdAndReserveFalse(Long userId);

    /** Count items in user's reserve list */
    int countByUserIdAndReserveTrue(Long userId);

    /** Count all items */
    int countByUserId(Long userId);

    /** Delete a specific item */
    void deleteByUserIdAndSubjectId(Long userId, Long subjectId);
}
