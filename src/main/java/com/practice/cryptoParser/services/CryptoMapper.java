package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import com.practice.cryptoParser.models.CryptoDomain;
import com.practice.cryptoParser.models.CryptoModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CryptoMapper {
    CryptoDomain entityToDomain(CryptoModel crypto);
    CryptoModel domainToEntity(CryptoDomain crypto);
    CryptoDTO domainToDto(CryptoDomain crypto);
}
