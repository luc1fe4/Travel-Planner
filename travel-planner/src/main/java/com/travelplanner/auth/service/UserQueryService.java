package com.travelplanner.auth.service;

import com.travelplanner.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {
    
    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }
}
