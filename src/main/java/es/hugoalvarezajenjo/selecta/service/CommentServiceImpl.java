package es.hugoalvarezajenjo.selecta.service;

import es.hugoalvarezajenjo.selecta.entity.Comment;
import es.hugoalvarezajenjo.selecta.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserAuthentication userAuthentication;

    public CommentServiceImpl(final CommentRepository commentRepository, final UserAuthentication userAuthentication) {
        this.commentRepository = commentRepository;
        this.userAuthentication = userAuthentication;
    }

    @Override
    public void saveComment(final Comment comment) {
        comment.setCreationDate(LocalDateTime.now());
        comment.setAuthorId(this.userAuthentication.getCurrentUser().getId());
        this.commentRepository.save(comment);
    }
}
