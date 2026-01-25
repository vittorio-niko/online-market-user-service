package org.innowise.internship.userservice.service;

import org.hibernate.query.SortDirection;
import org.innowise.internship.userservice.model.dto.request.user.FilterUserRequestDto;
import org.innowise.internship.userservice.model.entity.User;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    @DisplayName("Should get user by id successfully")
    void getUserById_shouldGetSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userQueryService.getUserById(1L);

        assertEquals(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting non-existent user by id")
    void getUserById_shouldThrowNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userQueryService.getUserById(1L));

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get all users with pageable")
    void getAllUsers_shouldGetAllWithPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = Page.empty();

        when(userRepository.findAll(pageable))
                .thenReturn(page);

        Page<User> result = userQueryService.getAllUsers(pageable);

        assertEquals(page, result);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get all users with filter and sorting")
    void getAllUsers_shouldGetAllWithFilterAndSorting() {
        FilterUserRequestDto filter = new FilterUserRequestDto();
        filter.setSortBy("email,surname");
        filter.setSortDirection(SortDirection.ASCENDING);

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = Page.empty();

        when(userRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<User> result = userQueryService.getAllUsers(filter, pageable);

        assertEquals(page, result);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(userRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable usedPageable = pageableCaptor.getValue();
        Sort sort = usedPageable.getSort();

        assertTrue(sort.getOrderFor("email").isAscending());
        assertTrue(sort.getOrderFor("surname").isAscending());
    }
}
