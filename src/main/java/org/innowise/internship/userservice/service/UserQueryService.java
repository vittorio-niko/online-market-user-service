package org.innowise.internship.userservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.innowise.internship.userservice.model.dto.request.user.FilterUserRequestDto;
import org.innowise.internship.userservice.model.entity.User;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.businessexception.NotFoundException;
import org.innowise.internship.userservice.service.specification.UserSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserQueryService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "UserQueryService::getUserById", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "UserQueryService::getInternalIdByKeycloakId", key = "#keycloakId")
    public Long getInternalIdByKeycloakId(String keycloakId) {
        return userRepository.findIdByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User with such keycloak id does not exist"));
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(@NonNull Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(@NonNull FilterUserRequestDto filter,
                                  @NonNull Pageable pageable) {
        Sort sort = Sort.by(
                filter.getSortDirection() == SortDirection.ASCENDING
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                filter.getSortBy().split(",")
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Specification<User> specification = UserSpecification.filter(filter);

        return userRepository.findAll(specification, sortedPageable);
    }
}
