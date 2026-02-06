package com.revature.service;

import com.revature.dto.PostRequest;
import com.revature.entity.Post;
import com.revature.entity.User;
import com.revature.exception.ResourceNotFoundException;
import com.revature.repository.PostRepository;
import com.revature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<Post> getAllPublishedPosts() {
        return postRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException ("Post", "id", id));
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    @Transactional
    public Post createPost(PostRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPublished(request.getPublished() != null ? request.getPublished() : false);
        post.setAuthor(author);

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, PostRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        if (!post.getAuthor().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException ("You don't have permission to update this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getPublished() != null) {
            post.setPublished(request.getPublished());
        }

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        if (!post.getAuthor().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public Post publishPost(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to publish this post");
        }

        post.setPublished(true);
        return postRepository.save(post);
    }
}
