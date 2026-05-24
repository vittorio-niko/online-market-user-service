package org.innowise.internship.userservice.model.dto.request.paymentcard;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePaymentCardRequestDto {
    @NotBlank
    @Size(min = 16, max = 16)
    @Pattern(regexp = "^[0-9]+$")
    private String number;

    @NotBlank
    @Size(min = 1, max = 100)
    private String holder;

    @NotNull
    @Future
    private LocalDate expirationDate;
}
