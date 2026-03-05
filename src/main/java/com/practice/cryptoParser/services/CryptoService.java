package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private final CryptoRepository cryptoRepository;
    private final CryptoMapper cryptoMapper;

    public void saveModel(CryptoDTO model) {
        cryptoRepository.save(cryptoMapper.dtoToEntity(model));
        log.info("Crypto saved");
    }

    public CryptoDTO getById(Long id) {
        return cryptoMapper.entityToDto(
                cryptoRepository.findById(id)
                        .orElseThrow(() -> new NullPointerException(String.format("Dont found crypto by id: %s", id)))
        );
    }

    public void deleteCrypto(Long id) {
        cryptoRepository.deleteById(id);
        log.info("Crypto {} deleted", id);
    }

}
    public void deleteCryptoByName(Long Name){
        cryptoRepository.deleteById(Name);
        log.info("Crypto {} deleted", Name);
    }

}
