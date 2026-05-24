package org.innowise.internship.userservice.model.mapper.response;

import lombok.NonNull;
import org.innowise.internship.userservice.model.dto.response.user.*;
import org.innowise.internship.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {
    UserResponseDto toUserResponseDto(@NonNull User user);

    List<UserResponseDto> toUserResponseDtoList(@NonNull List<User> users);

    UserWithCardsResponseDto toUserWithCardsResponseDto(@NonNull User user);

    List<UserWithCardsResponseDto> toUserWithCardsResponseDtoList(@NonNull List<User> users);

    UserProfileResponseDto toUserProfileResponseDto(@NonNull User user);

    List<UserProfileResponseDto> toUserProfileResponseDtoList(@NonNull List<User> users);

    UserProfileWithCardsResponseDto toUserProfileWithCardsResponseDto(@NonNull User user);

    List<UserProfileWithCardsResponseDto> toUserProfileWithCardsResponseDtoList(@NonNull List<User> users);

    @Mapping(target = "fullName",
                expression = "java(getFullName(user.getName(), user.getSurname()))")
    UserSummaryResponseDto toUserSummaryResponseDto(@NonNull User user);

    List<UserSummaryResponseDto> toUserSummaryResponseDtoList(@NonNull List<User> users);

    @Mapping(target = "fullName",
            expression = "java(getFullName(user.getName(), user.getSurname()))")
    UserSummaryWithCardsResponseDto toUserSummaryWithCardsResponseDto(@NonNull User user);

    List<UserSummaryWithCardsResponseDto> toUserSummaryWithCardsResponseDtoList(@NonNull List<User> users);

    default String getFullName(String name, String surname) {
        return String.format("%s %s", name, surname);
    }
}
