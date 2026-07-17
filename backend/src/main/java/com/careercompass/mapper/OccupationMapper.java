package com.careercompass.mapper;

import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.entity.Occupation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OccupationMapper {

    CareerSummaryResponse toSummary(Occupation occupation);
}
