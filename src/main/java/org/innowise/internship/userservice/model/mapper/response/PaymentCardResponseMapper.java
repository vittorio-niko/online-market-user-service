package org.innowise.internship.userservice.model.mapper.response;

import lombok.NonNull;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.innowise.internship.userservice.model.dto.response.paymentcard.PaymentCardResponseDto;
import org.innowise.internship.userservice.model.dto.response.paymentcard.PaymentCardSummaryResponseDto;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardResponseMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(
            target = "maskedNumber",
            source = "number",
            qualifiedByName = "maskCardNumber"
    )
    PaymentCardResponseDto toPaymentCardResponseDto(@NonNull PaymentCard card);

    List<PaymentCardResponseDto> toPaymentCardResponseDtoList(@NonNull List<PaymentCard> cards);

    @Mapping(
            target = "maskedNumber",
            source = "number",
            qualifiedByName = "maskCardNumber"
    )
    PaymentCardSummaryResponseDto toPaymentCardSummaryResponseDto(@NonNull PaymentCard card);

    List<PaymentCardSummaryResponseDto> toPaymentCardSummaryResponseDtoList(@NonNull List<PaymentCard> cards);

    @Named("maskCardNumber")
    default String maskCardNumber(String number) {
        final int digitsToShow = 4;
        int maskLength = number.length() - digitsToShow;
        if (maskLength > 0) {
            String mask = "*".repeat(maskLength);
            return mask + number.substring(maskLength);
        } else {
            return "*".repeat(number.length());
        }
    }
}
