package es.hugoalvarezajenjo.selecta.mapper;

import es.hugoalvarezajenjo.selecta.dto.UserProfileDto;
import es.hugoalvarezajenjo.selecta.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    UserProfileDto userToUserProfileDto(final User user);

}
