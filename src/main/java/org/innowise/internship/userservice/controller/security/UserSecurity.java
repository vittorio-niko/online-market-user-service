package org.innowise.internship.userservice.controller.security;

import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.CurrentKeycloakIdNotFound;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSecurity {
    private final UserRepository userRepository;

    public boolean isOwner(Long userId) {
        String keycloakId = getCurrentKeycloakId();

        return userRepository
                .existsByIdAndKeycloakId(userId, keycloakId);
    }

    public static String getCurrentKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        } else {
            throw new CurrentKeycloakIdNotFound("Current keycloak id for authorization is not found");
        }
    }
}
