package com.practice.cryptoParser.api;

import com.practice.cryptoParser.services.CryptoMapper;
import com.practice.cryptoParser.services.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cryptoService")
public class CryptoController {
    private final CryptoService cryptoService;
    private final CryptoMapper cryptoMapper;

    @GetMapping("/parse/{maxPage}")
    private ResponseEntity<?> getCrypto(@PathVariable int maxPage) {
        cryptoService.parse(maxPage);
        return ResponseEntity.ok(String.format("Parsed %d pages", maxPage));
    }

    @GetMapping("/getAll")
    private ResponseEntity<?> getAllCrypto() {
        List<CryptoDTO> allCrypto = cryptoService.getAll().stream()
                .map(cryptoMapper::domainToDto)
                .toList();
        return ResponseEntity.ok(allCrypto);
    }

    @GetMapping("/getById/{id}")
    private ResponseEntity<?> getCryptoById(@PathVariable Long id) {
        CryptoDTO cryptoDTO = cryptoMapper.domainToDto(cryptoService.getById(id));
        return ResponseEntity.ok(cryptoDTO);
    }

    @GetMapping("/getByName/{name}")
    private ResponseEntity<?> getCryptoByName(@PathVariable String name) {
        CryptoDTO crypto = cryptoMapper.domainToDto(cryptoService.getByName(name));
        return ResponseEntity.ok(crypto);
    }

    @DeleteMapping("/deleteById/{id}")
    private ResponseEntity<?> deleteCryptoById(@PathVariable Long id) {
        cryptoService.deleteCrypto(id);
        return ResponseEntity.ok(String.format("Crypto %d deleted", id));
    }

    @DeleteMapping("/deleteByName/{name}")
    private ResponseEntity<?> deleteCryptoByName(@PathVariable String name) {
        try {
            cryptoService.deleteCryptoByName(name);
            return ResponseEntity.ok(String.format("Crypto %s deleted", name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause());
        }
    }

}
