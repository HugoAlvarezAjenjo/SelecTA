package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectRatingServiceImpl implements SubjectRatingService {

    private final SubjectRatingRepository ratingRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public Double getAverageRating(final Long subjectId) {
        return ratingRepository.getAverageRating(subjectId);
    }

    @Override
    public long getRatingCount(final Long subjectId) {
        return ratingRepository.countBySubjectId(subjectId);
    }

    @Override
    public Optional<SubjectRating> getUserRating(final Long subjectId, final Long userId) {
        return ratingRepository.findBySubjectIdAndUserId(subjectId, userId);
    }

    @Override
    @Transactional
    public SubjectRating setRating(final Long subjectId, final User user, final int rating) {
        final Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));

        final Optional<SubjectRating> existing = ratingRepository.findBySubjectIdAndUserId(subjectId, user.getId());

        if (existing.isPresent() && existing.get().getRating() == rating) {
            // Same rating clicked again → toggle off (delete)
            ratingRepository.delete(existing.get());
            log.info("Rating removed: user={}, subject={}, rating={}", user.getUsername(), subjectId, rating);
            return null;
        }

        final SubjectRating entity = existing.orElseGet(() -> {
            final SubjectRating r = new SubjectRating();
            r.setSubject(subject);
            r.setUser(user);
            return r;
        });
        entity.setRating(rating);
        ratingRepository.save(entity);
        log.info("Rating set: user={}, subject={}, rating={}", user.getUsername(), subjectId, rating);
        return entity;
    }

    @Override
    @Transactional
    public void deleteRating(final SubjectRating rating) {
        ratingRepository.delete(rating);
    }
}
