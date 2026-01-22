package org.innowise.internship.userservice.model.mapper.request;

import lombok.NonNull;
import org.innowise.internship.userservice.model.dto.request.paymentcard.CreatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentCardRequestMapper {
    PaymentCard toPaymentCard(@NonNull CreatePaymentCardRequestDto dto);
}
