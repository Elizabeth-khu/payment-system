package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardCreateDto createDto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponseDto toResponseDto(PaymentCard paymentCard);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromDto(PaymentCardUpdateDto updateDto, @MappingTarget PaymentCard paymentCard);
}