package org.innowise.internship.userservice.model.mapper.request;

import lombok.NonNull;
import org.innowise.internship.userservice.model.dto.request.user.CreateUserRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.UpdateUserRequestDto;
import org.innowise.internship.userservice.model.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserRequestMapper {
    User toUser(@NonNull CreateUserRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(@NonNull UpdateUserRequestDto dto, @MappingTarget User user);
}
