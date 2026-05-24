package org.innowise.internship.userservice.model.dto.response.paymentcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCardSummaryResponseDto {
    private Long id;
    private String maskedNumber; // only last 4 digits shown
    private String holder;
    private Boolean active;
}
