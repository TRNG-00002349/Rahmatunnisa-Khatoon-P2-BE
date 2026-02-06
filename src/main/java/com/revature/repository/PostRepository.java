package com.revature.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.revature.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublishedTrueOrderByCreatedAtDesc();

    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    Optional<Post> findByIdAndAuthorId(Long id, Long authorId);
}
