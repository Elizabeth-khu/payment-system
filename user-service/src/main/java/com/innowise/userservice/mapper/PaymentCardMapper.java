package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardCreateDto createDto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponseDto toResponseDto(PaymentCard paymentCard);
}
