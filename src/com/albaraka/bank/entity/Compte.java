package com.albaraka.bank.entity;

import java.math.BigDecimal;

public sealed abstract class Compte permits CompteCourant, CompteEpargne {

    private final int id;
    private final String numero;
    private BigDecimal solde;
    private final int idClient;

    protected Compte(int id, String numero, BigDecimal solde, int idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    public int getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }
}