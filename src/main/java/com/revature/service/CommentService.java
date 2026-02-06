package com.revature.service;

import com.revature.dto.CommentRequest;
import com.revature.dto.CommentResponse;
import com.revature.entity.Comment;
import com.revature.entity.Post;
import com.revature.entity.User;
import com.revature.exception.ResourceNotFoundException;
import com.revature.repository.CommentRepository;
import com.revature.repository.PostRepository;
import com.revature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException ("User", "username", username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Verify ownership
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("You can only update your own comments");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return mapToResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Allow deletion if owner or admin
        if (!comment.getAuthor().getUsername().equals(username) &&
                currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setPostId(comment.getPost().getId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        CommentResponse.UserResponse userResponse = new CommentResponse.UserResponse();
        userResponse.setId(comment.getAuthor().getId());
        userResponse.setUsername(comment.getAuthor().getUsername());
        response.setAuthor(userResponse);

        return response;
    }
}