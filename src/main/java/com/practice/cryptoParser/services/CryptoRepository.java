package com.practice.cryptoParser.services;

import com.practice.cryptoParser.models.CryptoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoRepository extends JpaRepository<CryptoModel, Long> {

    void deleteByName(String name);

    Optional<CryptoModel> findCryptoModelByName(String name);

}
