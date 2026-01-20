package org.innowise.internship.userservice.model.dto.request.paymentcard;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentCardRequestDto {
    @NotNull
    private Long id;

    @Size(min = 16, max = 16)
    @Pattern(regexp = "^[0-9]+$")
    private String number;

    @Size(min = 1, max = 100)
    private String holder;

    @Future
    private LocalDate expirationDate;
}
