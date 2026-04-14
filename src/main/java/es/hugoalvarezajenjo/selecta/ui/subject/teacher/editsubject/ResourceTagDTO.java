package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceTag;
import lombok.Value;

import java.util.List;

@Value
public class ResourceTagDTO {
    Long id;
    String name;
    Long parentId;
    String parentName;
    int displayOrder;
    List<ResourceTagDTO> children;

    public static ResourceTagDTO createFromDomain(final ResourceTag tag) {
        return new ResourceTagDTO(
                tag.getId(),
                tag.getName(),
                tag.getParent() != null ? tag.getParent().getId() : null,
                tag.getParent() != null ? tag.getParent().getName() : null,
                tag.getDisplayOrder(),
                tag.getChildren() != null
                        ? tag.getChildren().stream().map(ResourceTagDTO::createFromDomain).toList()
                        : List.of()
        );
    }

    /**
     * Flat version without children (for autocomplete/search results).
     */
    public static ResourceTagDTO createFlat(final ResourceTag tag) {
        return new ResourceTagDTO(
                tag.getId(),
                tag.getName(),
                tag.getParent() != null ? tag.getParent().getId() : null,
                tag.getParent() != null ? tag.getParent().getName() : null,
                tag.getDisplayOrder(),
                List.of()
        );
    }
}
