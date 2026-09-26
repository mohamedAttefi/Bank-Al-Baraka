package com.albaraka.bank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        int id,
        LocalDateTime date,
        BigDecimal montant,
        TypeTransaction type,
        String lieu,
        int idCompte
) {
}