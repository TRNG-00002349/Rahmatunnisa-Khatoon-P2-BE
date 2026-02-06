package com.revature.service;

import java.util.List;

import com.revature.entity.User;
import com.revature.exception.ResourceNotFoundException;
import com.revature.repository.PostRepository;
import com.revature.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException ("User", "id", userId));
        userRepository.delete(user);
    }

    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsBanned(true);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAnyPost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    public User changeUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            user.setRole(User.Role.valueOf(role.toUpperCase()));
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
    }
}
