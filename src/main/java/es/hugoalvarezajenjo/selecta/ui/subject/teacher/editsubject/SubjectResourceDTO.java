package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import lombok.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
public class SubjectResourceDTO {
    private Long id;
    private String name;
    private String description;
    private String type;
    private String language;
    private String uploadDate;
    private boolean isPrivate;
    private Set<TagReference> tags;

    @Value
    public static class TagReference {
        Long id;
        String name;
    }

    public static SubjectResourceDTO createFromDomain(final SubjectResource subjectResource) {
        final Set<TagReference> tagRefs = subjectResource.getTags() != null
                ? subjectResource.getTags().stream()
                    .map(t -> new TagReference(t.getId(), t.getName()))
                    .collect(Collectors.toSet())
                : Set.of();

        return new SubjectResourceDTO(
                subjectResource.getId(),
                subjectResource.getName(),
                subjectResource.getDescription(),
                subjectResource.getType().toString(),
                subjectResource.getLanguage(),
                subjectResource.getCreationDate().toString(),
                subjectResource.isPrivate(),
                tagRefs);
    }

    public static List<SubjectResourceDTO> createFromDomain(final List<SubjectResource> subjectResources) {
        return subjectResources.stream()
                .map(SubjectResourceDTO::createFromDomain)
                .toList();
    }
}
