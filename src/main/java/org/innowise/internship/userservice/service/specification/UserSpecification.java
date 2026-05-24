package org.innowise.internship.userservice.service.specification;

import org.innowise.internship.userservice.model.dto.request.user.FilterUserRequestDto;
import org.innowise.internship.userservice.model.entity.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> filter(FilterUserRequestDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%")
                );
            }

            if (filter.getSurname() != null) {
                predicates.add(
                        cb.like(cb.lower(root.get("surname")),
                                "%" + filter.getSurname().toLowerCase() + "%")
                );
            }

            if (filter.getEmail() != null) {
                predicates.add(
                        cb.equal(cb.lower(root.get("email")),
                                filter.getEmail().toLowerCase())
                );
            }

            if (filter.getActive() != null) {
                predicates.add(
                        cb.equal(root.get("active"), filter.getActive())
                );
            }

            if (filter.getBornFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("birthDate"),
                                filter.getBornFrom())
                );
            }

            if (filter.getBornTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("birthDate"),
                                filter.getBornTo())
                );
            }

            if (filter.getCreatedFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("createdAt"),
                                filter.getCreatedFrom())
                );
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("createdAt"),
                                filter.getCreatedTo())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
