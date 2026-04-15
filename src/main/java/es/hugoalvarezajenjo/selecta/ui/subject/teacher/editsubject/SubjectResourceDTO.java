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
    private Long folderId;
    private String folderName;
    private Set<TagRef> tags;

    @Value
    public static class TagRef {
        Long id;
        String name;
    }

    public static SubjectResourceDTO createFromDomain(final SubjectResource r) {
        final Set<TagRef> tagRefs = r.getTags() != null
                ? r.getTags().stream().map(t -> new TagRef(t.getId(), t.getName())).collect(Collectors.toSet())
                : Set.of();
        return new SubjectResourceDTO(
                r.getId(), r.getName(), r.getDescription(), r.getType().toString(),
                r.getLanguage(), r.getCreationDate().toString(), r.isPrivate(),
                r.getFolder() != null ? r.getFolder().getId() : null,
                r.getFolder() != null ? r.getFolder().getName() : null,
                tagRefs);
    }

    public static List<SubjectResourceDTO> createFromDomain(final List<SubjectResource> resources) {
        return resources.stream().map(SubjectResourceDTO::createFromDomain).toList();
    }
}
