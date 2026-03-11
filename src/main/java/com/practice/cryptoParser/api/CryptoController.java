package com.practice.cryptoParser.api;

import com.practice.cryptoParser.services.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cryptoService")
@RequiredArgsConstructor
public class CryptoController {
    private final CryptoService cryptoService;

    @GetMapping("/get/{maxPage}")
    private ResponseEntity<?> getCrypto(@PathVariable int maxPage) {
        cryptoService.parse(maxPage);
        return ResponseEntity.ok(String.format("Parsed %d pages", maxPage));
    }

    @GetMapping("/getAll")
    private ResponseEntity<?> getAllCrypto() {
        List<CryptoDTO> all = cryptoService.getAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/getById/{id}")
    private ResponseEntity<?> getCryptoById(@PathVariable Long id) {
        try {
            CryptoDTO cryptoDTO = cryptoService.getById(id);
            return ResponseEntity.ok(cryptoDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause());
        }
    }

    @GetMapping("/getByName/{name}")
    private ResponseEntity<?> getCryptoByName(@PathVariable String name) {
        try {
            CryptoDTO crypto = cryptoService.getByName(name);
            return ResponseEntity.ok(crypto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause());
        }
    }

    @DeleteMapping("/deleteById/{id}")
    private ResponseEntity<?> deleteCryptoById(@PathVariable Long id) {
        try {
            cryptoService.deleteCrypto(id);
            return ResponseEntity.ok(String.format("Crypto %d deleted", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause());
        }
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
