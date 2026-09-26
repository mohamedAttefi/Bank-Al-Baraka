package com.albaraka.bank.service;

import com.albaraka.bank.DAO.TransactionDAO;
import com.albaraka.bank.entity.Transaction;
import com.albaraka.bank.entity.TypeTransaction;

import com.albaraka.bank.DAO.CompteDAO;
import com.albaraka.bank.entity.Compte;
import com.albaraka.bank.entity.CompteCourant;
import com.albaraka.bank.entity.CompteEpargne;
import com.albaraka.bank.util.DatabaseConnection;

import java.sql.Connection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private static final BigDecimal SUSPICIOUS_AMOUNT = new BigDecimal("10000");

    private final TransactionDAO transactionDAO;
    private final CompteDAO compteDAO;

    public TransactionService() {
        this.compteDAO = new CompteDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public Transaction enregistrer(Transaction transaction) throws SQLException {

        validateTransaction(transaction);

        return transactionDAO.save(transaction);
    }

    private void validateMontant(BigDecimal montant) {

        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }
    }


    public List<Transaction> rechercherParCompte(int idCompte) throws SQLException {

        if (idCompte <= 0) {
            throw new IllegalArgumentException("L'identifiant du compte est invalide.");
        }

        return transactionDAO.findByCompte(idCompte);
    }

    public Transaction effectuerVersement(int idCompte, BigDecimal montant, String lieu) throws SQLException {

        validateMontant(montant);

        try (Connection connection = DatabaseConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                Compte compte = compteDAO.findById(idCompte).orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

                BigDecimal nouveauSolde = compte.getSolde().add(montant);

                boolean soldeMisAJour = compteDAO.updateSolde(connection, idCompte, nouveauSolde);

                if (!soldeMisAJour) {
                    throw new SQLException("Impossible de mettre à jour le solde.");
                }

                Transaction transaction = new Transaction(0, LocalDateTime.now(), montant, TypeTransaction.VERSEMENT, lieu, idCompte);

                Transaction saved = transactionDAO.save(connection, transaction);

                connection.commit();

                return saved;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    public Transaction effectuerRetrait(int idCompte, BigDecimal montant, String lieu) throws SQLException {

        validateMontant(montant);

        try (Connection connection = DatabaseConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                Compte compte = compteDAO.findById(idCompte).orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

                BigDecimal nouveauSolde = compte.getSolde().subtract(montant);

                verifierSoldeRetrait(compte, nouveauSolde);

                boolean soldeMisAJour = compteDAO.updateSolde(connection, idCompte, nouveauSolde);

                if (!soldeMisAJour) {
                    throw new SQLException("Impossible de mettre à jour le solde.");
                }

                Transaction transaction = new Transaction(0, LocalDateTime.now(), montant, TypeTransaction.RETRAIT, lieu, idCompte);

                Transaction saved = transactionDAO.save(connection, transaction);

                connection.commit();

                return saved;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }

    public void effectuerVirement(int compteSource, int compteDestination, BigDecimal montant, String lieu) throws SQLException {

        validateMontant(montant);

        if (compteSource == compteDestination) {
            throw new IllegalArgumentException("Les comptes source et destination doivent être différents.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                Compte source = compteDAO.findById(compteSource).orElseThrow(() -> new IllegalArgumentException("Compte source introuvable."));

                Compte destination = compteDAO.findById(compteDestination).orElseThrow(() -> new IllegalArgumentException("Compte destination introuvable."));

                BigDecimal nouveauSoldeSource = source.getSolde().subtract(montant);

                verifierSoldeRetrait(source, nouveauSoldeSource);

                BigDecimal nouveauSoldeDestination = destination.getSolde().add(montant);

                compteDAO.updateSolde(connection, compteSource, nouveauSoldeSource);

                compteDAO.updateSolde(connection, compteDestination, nouveauSoldeDestination);

                Transaction transaction = new Transaction(0, LocalDateTime.now(), montant, TypeTransaction.VIREMENT, lieu, compteSource);

                transactionDAO.save(connection, transaction);

                connection.commit();

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    private void verifierSoldeRetrait(Compte compte, BigDecimal nouveauSolde) {

        if (compte instanceof CompteCourant courant) {

            BigDecimal decouvert = courant.getDecouvertAutorise();

            BigDecimal limite = decouvert.negate();

            if (nouveauSolde.compareTo(limite) < 0) {

                throw new IllegalArgumentException("Retrait impossible : découvert autorisé dépassé.");
            }

        } else if (compte instanceof CompteEpargne) {

            if (nouveauSolde.compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException("Retrait impossible : solde insuffisant.");
            }
        }
    }


    public List<Transaction> rechercherParType(TypeTransaction type) throws SQLException {

        if (type == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire.");
        }

        return transactionDAO.findByType(type);
    }

    public List<Transaction> lister() throws SQLException {

        return transactionDAO.findAll();
    }


    public List<Transaction> trierParDate(List<Transaction> transactions) {

        return transactions.stream().sorted(Comparator.comparing(Transaction::date).reversed()).toList();
    }


    public List<Transaction> filtrerParMontantMinimum(List<Transaction> transactions, BigDecimal montantMinimum) {

        if (montantMinimum == null || montantMinimum.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException("Le montant minimum est invalide.");
        }

        return transactions.stream().filter(transaction -> transaction.montant().compareTo(montantMinimum) >= 0).toList();
    }


    public List<Transaction> filtrerParType(List<Transaction> transactions, TypeTransaction type) {

        if (type == null) {
            throw new IllegalArgumentException("Le type est obligatoire.");
        }

        return transactions.stream().filter(transaction -> transaction.type() == type).toList();
    }


    public List<Transaction> filtrerParPeriode(List<Transaction> transactions, LocalDateTime debut, LocalDateTime fin) {

        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Les dates sont obligatoires.");
        }

        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
        }

        return transactions.stream().filter(transaction -> !transaction.date().isBefore(debut) && !transaction.date().isAfter(fin)).toList();
    }


    public List<Transaction> filtrerParLieu(List<Transaction> transactions, String lieu) {

        if (lieu == null || lieu.isBlank()) {
            throw new IllegalArgumentException("Le lieu est obligatoire.");
        }

        return transactions.stream().filter(transaction -> transaction.lieu() != null && transaction.lieu().equalsIgnoreCase(lieu)).toList();
    }


    public BigDecimal calculerTotal(List<Transaction> transactions) {

        return transactions.stream().map(Transaction::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal calculerMoyenne(List<Transaction> transactions) {

        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = calculerTotal(transactions);

        return total.divide(BigDecimal.valueOf(transactions.size()), 2, java.math.RoundingMode.HALF_UP);
    }


    public Map<TypeTransaction, List<Transaction>> regrouperParType(List<Transaction> transactions) {

        return transactions.stream().collect(Collectors.groupingBy(Transaction::type));
    }


    public Map<TypeTransaction, Long> compterParType(List<Transaction> transactions) {

        return transactions.stream().collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }


    public List<Transaction> detecterTransactionsSuspectes(List<Transaction> transactions) {

        return transactions.stream().filter(this::estSuspecte).toList();
    }

    private boolean estSuspecte(Transaction transaction) {


        if (transaction.montant().compareTo(SUSPICIOUS_AMOUNT) > 0) {

            return true;
        }

        return false;
    }

    public List<Transaction> detecterFrequenceExcessive(List<Transaction> transactions) {

        return transactions.stream().filter(transaction -> transactions.stream().anyMatch(other -> !other.equals(transaction) && other.idCompte() == transaction.idCompte() && Math.abs(Duration.between(transaction.date(), other.date()).toSeconds()) < 60)).toList();
    }

    private void validateTransaction(Transaction transaction) {

        if (transaction == null) {
            throw new IllegalArgumentException("La transaction ne peut pas être null.");
        }

        if (transaction.date() == null) {
            throw new IllegalArgumentException("La date est obligatoire.");
        }

        if (transaction.montant() == null || transaction.montant().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }

        if (transaction.type() == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire.");
        }

        if (transaction.idCompte() <= 0) {
            throw new IllegalArgumentException("Le compte est invalide.");
        }
    }
}