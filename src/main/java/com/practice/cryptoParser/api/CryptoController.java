package com.practice.cryptoParser.api;

import com.practice.cryptoParser.services.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cryptoService")
@RequiredArgsConstructor
public class CryptoController {
    private final CryptoService cryptoService;

    @GetMapping("/get/{maxPage}")
    private ResponseEntity<?> getCrypto(@PathVariable int maxPage) {
        cryptoService.parse(maxPage);
        return ResponseEntity.ok("vse okey");
    }

    @GetMapping("/getAll")
    private ResponseEntity<?> getAllCrypto() {
        List<CryptoDTO> all = cryptoService.getAll();
        return ResponseEntity.ok(all);

    }
}
