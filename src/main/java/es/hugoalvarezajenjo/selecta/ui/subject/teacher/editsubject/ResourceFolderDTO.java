package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceFolder;
import lombok.Value;

import java.util.List;

@Value
public class ResourceFolderDTO {
    Long id;
    String name;
    Long parentId;
    String parentName;
    int displayOrder;
    List<ResourceFolderDTO> children;

    public static ResourceFolderDTO createFromDomain(final ResourceFolder folder) {
        return new ResourceFolderDTO(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getParent() != null ? folder.getParent().getName() : null,
                folder.getDisplayOrder(),
                folder.getChildren() != null
                        ? folder.getChildren().stream().map(ResourceFolderDTO::createFromDomain).toList()
                        : List.of()
        );
    }

    public static ResourceFolderDTO createFlat(final ResourceFolder folder) {
        return new ResourceFolderDTO(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getParent() != null ? folder.getParent().getName() : null,
                folder.getDisplayOrder(),
                List.of()
        );
    }
}
