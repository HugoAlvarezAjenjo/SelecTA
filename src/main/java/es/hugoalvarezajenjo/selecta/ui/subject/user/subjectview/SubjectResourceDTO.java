package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import lombok.Value;

import java.util.List;

@Value
public class SubjectResourceDTO {
    private Long id;
    private String name;
    private String description;
    private String type;
    private String language;
    private String uploadDate;
    private String url;

    public static SubjectResourceDTO createFromDomain(final SubjectResource subjectResource) {
        String storagePath = subjectResource.getUrl();
        if (storagePath != null && storagePath.startsWith("/")) {
            storagePath = storagePath.substring(1);
        }

        return new SubjectResourceDTO(
                subjectResource.getId(),
                subjectResource.getName(),
                subjectResource.getDescription(),
                subjectResource.getType().toString(),
                subjectResource.getLanguage(),
                subjectResource.getCreationDate().toString(),
                storagePath
        );
    }

    public static List<SubjectResourceDTO> createFromDomain(final List<SubjectResource> subjectResources) {
        return subjectResources.stream()
                .map(SubjectResourceDTO::createFromDomain)
                .toList();
    }

}
