package es.hugoalvarezajenjo.selecta.services.enrollment;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an item in a user's enrollment priority list.
 * The position field determines the display order (lower = higher priority).
 */
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"}),
    indexes = @Index(columnList = "user_id, position")
)
@Getter
@Setter
@NoArgsConstructor
public class EnrollmentListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /** Position in the list (0-based, lower = higher priority) */
    @Column(nullable = false)
    private int position;

    /** Whether this item is a reserve (backup) in case main choices are not available */
    @Column(nullable = false)
    private boolean reserve = false;

    /** Optional note the user can add (e.g. "grupo mañana", "si queda plaza") */
    @Column(length = 255)
    private String note;

    public EnrollmentListItem(User user, Subject subject, int position) {
        this.user = user;
        this.subject = subject;
        this.position = position;
        this.reserve = false;
    }

    public EnrollmentListItem(User user, Subject subject, int position, boolean reserve) {
        this.user = user;
        this.subject = subject;
        this.position = position;
        this.reserve = reserve;
    }
}
