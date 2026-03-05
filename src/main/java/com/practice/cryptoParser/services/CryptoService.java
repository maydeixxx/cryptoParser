package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import com.practice.cryptoParser.models.CryptoModel;
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
        CryptoModel savedModel = cryptoRepository.save(cryptoMapper.dtoToEntity(model));

        if (savedModel != null && savedModel.getId() != null) {
            log.info("Crypto saved successfully with id: {}", savedModel.getId());
        } else {
            log.error("unlucky");
        }
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

    public void checkstatus(String Status) {

    }

    public void deleteCryptoByName(String Name) {
        cryptoRepository.deleteByName(Name);
        log.info("Crypto {} deleted", Name);
    }

}
