package com.albaraka.bank.service;

import com.albaraka.bank.DAO.CompteDAO;
import com.albaraka.bank.entity.Compte;
import com.albaraka.bank.entity.CompteCourant;
import com.albaraka.bank.entity.CompteEpargne;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CompteService {

    private final CompteDAO compteDAO;

    public CompteService() {
        this.compteDAO = new CompteDAO();
    }


    public Compte creerCompteCourant(String numero, BigDecimal solde, int idClient, BigDecimal decouvertAutorise) throws SQLException {

        validateCommonData(numero, solde, idClient);

        if (decouvertAutorise == null || decouvertAutorise.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException("Le découvert autorisé doit être positif.");
        }

        if (compteDAO.findByNumero(numero).isPresent()) {
            throw new IllegalArgumentException("Un compte avec ce numéro existe déjà.");
        }

        CompteCourant compte = new CompteCourant(0, numero, solde, idClient, decouvertAutorise);

        return compteDAO.save(compte);
    }

    public Compte creerCompteEpargne(String numero, BigDecimal solde, int idClient, BigDecimal tauxInteret) throws SQLException {

        validateCommonData(numero, solde, idClient);

        if (tauxInteret == null || tauxInteret.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException("Le taux d'intérêt doit être positif.");
        }

        if (compteDAO.findByNumero(numero).isPresent()) {
            throw new IllegalArgumentException("Un compte avec ce numéro existe déjà.");
        }

        CompteEpargne compte = new CompteEpargne(0, numero, solde, idClient, tauxInteret);

        return compteDAO.save(compte);
    }

    public Optional<Compte> rechercherParId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant du compte est invalide.");
        }

        return compteDAO.findById(id);
    }

    public Optional<Compte> rechercherParNumero(String numero) throws SQLException {

        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Le numéro du compte est obligatoire.");
        }

        return compteDAO.findByNumero(numero);
    }

    public List<Compte> rechercherParClient(int idClient) throws SQLException {

        if (idClient <= 0) {
            throw new IllegalArgumentException("L'identifiant du client est invalide.");
        }

        return compteDAO.findByClient(idClient);
    }

    public List<Compte> lister() throws SQLException {
        return compteDAO.findAll();
    }

    public boolean modifierSolde(int idCompte, BigDecimal nouveauSolde) throws SQLException {

        if (idCompte <= 0) {
            throw new IllegalArgumentException("L'identifiant du compte est invalide.");
        }

        if (nouveauSolde == null) {
            throw new IllegalArgumentException("Le solde ne peut pas être null.");
        }

        Compte compte = rechercherParId(idCompte).orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        compte.setSolde(nouveauSolde);

        return compteDAO.update(compte);
    }

    public boolean modifierParametre(Compte compte) throws SQLException {

        if (compte == null) {
            throw new IllegalArgumentException("Le compte ne peut pas être null.");
        }

        if (compte.getId() <= 0) {
            throw new IllegalArgumentException("L'identifiant du compte est invalide.");
        }

        if (compte instanceof CompteCourant courant) {

            if (courant.getDecouvertAutorise() == null || courant.getDecouvertAutorise().compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException("Le découvert autorisé est invalide.");
            }

        } else if (compte instanceof CompteEpargne epargne) {

            if (epargne.getTauxInteret() == null || epargne.getTauxInteret().compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException("Le taux d'intérêt est invalide.");
            }
        }

        return compteDAO.update(compte);
    }


    public boolean supprimer(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant du compte est invalide.");
        }

        return compteDAO.delete(id);
    }


    public Optional<Compte> compteAvecSoldeMaximum() throws SQLException {

        return compteDAO.findAll().stream().max(Comparator.comparing(Compte::getSolde));
    }

    public Optional<Compte> compteAvecSoldeMinimum() throws SQLException {

        return compteDAO.findAll().stream().min(Comparator.comparing(Compte::getSolde));
    }


    private void validateCommonData(String numero, BigDecimal solde, int idClient) {

        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Le numéro du compte est obligatoire.");
        }

        if (solde == null) {
            throw new IllegalArgumentException("Le solde ne peut pas être null.");
        }

        if (idClient <= 0) {
            throw new IllegalArgumentException("L'identifiant du client est invalide.");
        }
    }
}