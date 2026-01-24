package es.hugoalvarezajenjo.selecta.mapper;

import es.hugoalvarezajenjo.selecta.dto.UserProfileDto;
import es.hugoalvarezajenjo.selecta.services.user.User;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserProfileDto userToUserProfileDto(final User user);

}
