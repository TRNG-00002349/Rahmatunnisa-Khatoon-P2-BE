package com.revature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(min = 1, max = 5000, message = "Comment must be between 1 and 5000 characters")
    private String content;
}

