package es.hugoalvarezajenjo.selecta.services.subjects.repository;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRatingRepository extends JpaRepository<SubjectRating, Long> {

    Optional<SubjectRating> findBySubjectIdAndUserId(Long subjectId, Long userId);

    @Query("SELECT AVG(r.rating) FROM SubjectRating r WHERE r.subject.id = :subjectId")
    Double getAverageRating(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(r) FROM SubjectRating r WHERE r.subject.id = :subjectId")
    long countBySubjectId(@Param("subjectId") Long subjectId);
}
