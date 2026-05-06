package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.VoteType;
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
    private boolean isPrivate;
    private boolean official;
    private String uploadedByName;
    private long upvotes;
    private long downvotes;
    private String userVote; // "UPVOTE", "DOWNVOTE", or null

    public static SubjectResourceDTO createFromDomain(final SubjectResource subjectResource,
                                                       final long upvotes,
                                                       final long downvotes,
                                                       final VoteType userVote) {
        return new SubjectResourceDTO(
                subjectResource.getId(),
                subjectResource.getName(),
                subjectResource.getDescription(),
                subjectResource.getType().toString(),
                subjectResource.getLanguage(),
                subjectResource.getCreationDate().toString(),
                subjectResource.isPrivate(),
                subjectResource.isOfficial(),
                subjectResource.getUploadedBy() != null ? subjectResource.getUploadedBy().getUsername() : null,
                upvotes,
                downvotes,
                userVote != null ? userVote.name() : null);
    }

    public static List<SubjectResourceDTO> createFromDomain(final List<SubjectResource> subjectResources,
                                                             final java.util.function.Function<Long, Long> upvoteCounter,
                                                             final java.util.function.Function<Long, Long> downvoteCounter,
                                                             final java.util.function.Function<Long, VoteType> userVoteResolver) {
        return subjectResources.stream()
                .map(r -> createFromDomain(r,
                        upvoteCounter.apply(r.getId()),
                        downvoteCounter.apply(r.getId()),
                        userVoteResolver.apply(r.getId())))
                .toList();
    }
}
