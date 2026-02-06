package com.revature.controller;

import com.revature.dto.ApiResponse;
import com.revature.dto.CommentRequest;
import com.revature.dto.CommentResponse;
import com.revature.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Comment added successfully", comment));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPost(
            @PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comments retrieved successfully", comments));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.updateComment(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment updated successfully", comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment deleted successfully", null));
    }
}
