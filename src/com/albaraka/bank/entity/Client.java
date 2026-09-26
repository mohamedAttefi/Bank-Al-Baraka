package com.albaraka.bank.entity;

public record Client(
        int id,
        String nom,
        String email
) {
}