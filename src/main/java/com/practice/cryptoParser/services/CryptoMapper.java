package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import com.practice.cryptoParser.models.CryptoModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CryptoMapper {

    CryptoModel dtoToEntity(CryptoDTO dto);

    CryptoDTO entityToDto(CryptoModel entity);

}
