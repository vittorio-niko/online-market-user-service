package org.innowise.internship.userservice.model.mapper.request;

import lombok.NonNull;
import org.innowise.internship.userservice.model.dto.request.paymentcard.CreatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.dto.request.paymentcard.UpdatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PaymentCardRequestMapper {
    PaymentCard toPaymentCard(@NonNull CreatePaymentCardRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentCardFromDto(@NonNull UpdatePaymentCardRequestDto dto,
                                  @MappingTarget PaymentCard card);
}
