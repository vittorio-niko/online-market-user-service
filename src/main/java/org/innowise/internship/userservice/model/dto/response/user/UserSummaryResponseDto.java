package org.innowise.internship.userservice.model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponseDto {
    private Long id;
    private String authId;
    private String fullName;
    private String email;
}
