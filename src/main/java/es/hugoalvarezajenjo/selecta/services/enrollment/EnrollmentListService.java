package es.hugoalvarezajenjo.selecta.services.enrollment;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing a user's enrollment priority list.
 * Supports main list and reserve list with add, remove, reorder, toggle, and note operations.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentListService {

    private final EnrollmentListRepository repository;
    private final SubjectService subjectService;

    /**
     * Gets the user's main enrollment list (non-reserve) ordered by position.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentListItem> getMainList(Long userId) {
        return repository.findByUserIdAndReserveFalseOrderByPositionAsc(userId);
    }

    /**
     * Gets the user's reserve list ordered by position.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentListItem> getReserveList(Long userId) {
        return repository.findByUserIdAndReserveTrueOrderByPositionAsc(userId);
    }

    /**
     * Gets all items (legacy compatibility).
     */
    @Transactional(readOnly = true)
    public List<EnrollmentListItem> getUserList(Long userId) {
        return repository.findByUserIdOrderByPositionAsc(userId);
    }

    /**
     * Adds a subject to the main list (end position).
     * Returns false if already present in either list.
     */
    @Transactional
    public boolean addSubject(User user, Long subjectId, boolean asReserve) {
        if (repository.existsByUserIdAndSubjectId(user.getId(), subjectId)) {
            return false;
        }

        Subject subject = subjectService.getSubjectById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));

        int nextPosition = asReserve
                ? repository.countByUserIdAndReserveTrue(user.getId())
                : repository.countByUserIdAndReserveFalse(user.getId());

        EnrollmentListItem item = new EnrollmentListItem(user, subject, nextPosition, asReserve);
        repository.save(item);
        return true;
    }

    /**
     * Adds a subject to the main list (default, non-reserve).
     */
    @Transactional
    public boolean addSubject(User user, Long subjectId) {
        return addSubject(user, subjectId, false);
    }

    /**
     * Removes a subject from the user's list and re-normalizes positions.
     */
    @Transactional
    public void removeSubject(Long userId, Long subjectId) {
        var item = repository.findByUserIdAndSubjectId(userId, subjectId);
        if (item.isPresent()) {
            boolean wasReserve = item.get().isReserve();
            repository.deleteByUserIdAndSubjectId(userId, subjectId);
            if (wasReserve) {
                normalizePositions(userId, true);
            } else {
                normalizePositions(userId, false);
            }
        }
    }

    /**
     * Toggles a subject between main list and reserve list.
     */
    @Transactional
    public void toggleReserve(Long userId, Long subjectId) {
        repository.findByUserIdAndSubjectId(userId, subjectId).ifPresent(item -> {
            boolean newReserveState = !item.isReserve();
            item.setReserve(newReserveState);
            // Set position to end of the target list
            int newPosition = newReserveState
                    ? repository.countByUserIdAndReserveTrue(userId)
                    : repository.countByUserIdAndReserveFalse(userId);
            item.setPosition(newPosition);
            repository.save(item);
            // Normalize positions in the source list
            normalizePositions(userId, !newReserveState);
        });
    }

    /**
     * Reorders a specific list (main or reserve) based on ordered subject IDs.
     */
    @Transactional
    public void reorder(Long userId, List<Long> orderedSubjectIds, boolean isReserve) {
        List<EnrollmentListItem> items = isReserve
                ? repository.findByUserIdAndReserveTrueOrderByPositionAsc(userId)
                : repository.findByUserIdAndReserveFalseOrderByPositionAsc(userId);

        for (int i = 0; i < orderedSubjectIds.size(); i++) {
            final int position = i;
            final Long subjectId = orderedSubjectIds.get(i);
            items.stream()
                    .filter(item -> item.getSubject().getId().equals(subjectId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setPosition(position);
                        repository.save(item);
                    });
        }
    }

    /**
     * Reorders the main list (backward compat).
     */
    @Transactional
    public void reorder(Long userId, List<Long> orderedSubjectIds) {
        reorder(userId, orderedSubjectIds, false);
    }

    /**
     * Updates the note for a specific item.
     */
    @Transactional
    public void updateNote(Long userId, Long subjectId, String note) {
        repository.findByUserIdAndSubjectId(userId, subjectId)
                .ifPresent(item -> {
                    item.setNote(note != null ? note.trim() : null);
                    repository.save(item);
                });
    }

    /**
     * Checks if a subject is in the user's enrollment list.
     */
    @Transactional(readOnly = true)
    public boolean isInList(Long userId, Long subjectId) {
        return repository.existsByUserIdAndSubjectId(userId, subjectId);
    }

    /**
     * Gets the total credits sum of the main list.
     */
    @Transactional(readOnly = true)
    public int getMainCredits(Long userId) {
        return getMainList(userId).stream()
                .mapToInt(item -> item.getSubject().getCredits())
                .sum();
    }

    /**
     * Gets the total credits sum of the reserve list.
     */
    @Transactional(readOnly = true)
    public int getReserveCredits(Long userId) {
        return getReserveList(userId).stream()
                .mapToInt(item -> item.getSubject().getCredits())
                .sum();
    }

    /**
     * Gets total credits (both lists combined).
     */
    @Transactional(readOnly = true)
    public int getTotalCredits(Long userId) {
        return getUserList(userId).stream()
                .mapToInt(item -> item.getSubject().getCredits())
                .sum();
    }

    /**
     * Re-normalizes positions for a specific list type.
     */
    private void normalizePositions(Long userId, boolean isReserve) {
        List<EnrollmentListItem> items = isReserve
                ? repository.findByUserIdAndReserveTrueOrderByPositionAsc(userId)
                : repository.findByUserIdAndReserveFalseOrderByPositionAsc(userId);

        for (int i = 0; i < items.size(); i++) {
            EnrollmentListItem item = items.get(i);
            if (item.getPosition() != i) {
                item.setPosition(i);
                repository.save(item);
            }
        }
    }
}
