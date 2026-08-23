package com.spacefurni.identity.api.dto;

import com.spacefurni.identity.domain.UserRole;
import java.util.UUID;

public record CurrentUserResponse(UUID id, String email, String fullName, UserRole role) {
}
