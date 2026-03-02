package org.innowise.internship.userservice.controller.securitycontext;

import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.service.UserQueryService;
import org.innowise.internship.userservice.service.exception.securityexception.NoJwtAuthenticationFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {
    private final UserQueryService userQueryService;

    public String getCurrentKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NoJwtAuthenticationFoundException("No Jwt found");
        }
        return authentication.getName(); // sub
    }

    public Long getCurrentInternalId() {
        String keycloakId = getCurrentKeycloakId();
        return userQueryService.getInternalIdByKeycloakId(keycloakId);
    }
}
