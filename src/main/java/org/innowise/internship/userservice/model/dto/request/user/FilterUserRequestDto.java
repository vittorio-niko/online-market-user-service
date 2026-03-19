package org.innowise.internship.userservice.model.dto.request.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.query.SortDirection;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterUserRequestDto {
    private String name;
    private String surname;

    @Email
    private String email;

    private Boolean active;

    private LocalDate bornFrom;
    private LocalDate bornTo;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
