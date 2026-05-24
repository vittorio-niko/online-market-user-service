package org.innowise.internship.userservice.model.dto.response.user;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import lombok.experimental.SuperBuilder;
import org.innowise.internship.userservice.model.dto.response.paymentcard.PaymentCardSummaryResponseDto;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserWithCardsResponseDto extends UserResponseDto {
    private List<PaymentCardSummaryResponseDto> paymentCards;
}
