package com.recallmap.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String displayName,
        UUID organizationId,
        UUID workspaceId) {
}
