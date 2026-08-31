package com.spacefurni.identity.application;

import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserQueryService {

    private final UserRepository userRepository;

    public CurrentUserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    @Transactional(readOnly = true)
    public String getEmailById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)).getEmail();
    }
}
