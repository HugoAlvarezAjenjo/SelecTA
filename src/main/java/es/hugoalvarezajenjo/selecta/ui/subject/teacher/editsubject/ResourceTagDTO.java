package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceTag;
import lombok.Value;

import java.util.List;

@Value
public class ResourceTagDTO {
    Long id;
    String name;

    public static ResourceTagDTO createFromDomain(final ResourceTag tag) {
        return new ResourceTagDTO(tag.getId(), tag.getName());
    }

    public static List<ResourceTagDTO> createListFromDomain(final List<ResourceTag> tags) {
        return tags.stream().map(ResourceTagDTO::createFromDomain).toList();
    }
}
