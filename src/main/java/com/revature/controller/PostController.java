package com.revature.controller;

import com.revature.dto.ApiResponse;
import com.revature.dto.PostRequest;
import com.revature.entity.Post;
import com.revature.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Post>>> getAllPublishedPosts() {
        List<Post> posts = postService.getAllPublishedPosts();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Post>> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post created successfully", post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.updatePost(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Post>> publishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.publishPost(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post published successfully", post));
    }
}
