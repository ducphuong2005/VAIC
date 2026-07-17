package com.careercompass.mapper;

import com.careercompass.dto.response.UserResponse;
import com.careercompass.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
