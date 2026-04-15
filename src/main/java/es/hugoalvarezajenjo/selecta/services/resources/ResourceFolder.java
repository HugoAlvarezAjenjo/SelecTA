package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "name", "parent_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ResourceFolder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ResourceFolder> children = new ArrayList<>();

    private int displayOrder;

    public static String normalizeName(final String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }
        final String trimmed = rawName.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }
}
