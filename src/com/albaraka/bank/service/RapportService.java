package com.albaraka.bank.service;

import com.albaraka.bank.DAO.ClientDAO;
import com.albaraka.bank.DAO.CompteDAO;
import com.albaraka.bank.DAO.TransactionDAO;
import com.albaraka.bank.entity.Client;
import com.albaraka.bank.entity.Compte;
import com.albaraka.bank.entity.Transaction;
import com.albaraka.bank.entity.TypeTransaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RapportService {

    private static final BigDecimal SEUIL_SOLDE_BAS = new BigDecimal("1000");

    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;

    public RapportService() {
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
        this.transactionDAO = new TransactionDAO();
    }


    public List<ClientSolde> top5ClientsParSolde() throws SQLException {

        List<Client> clients = clientDAO.findAll();

        return clients.stream().map(client -> {

            BigDecimal soldeTotal = null;
            try {
                soldeTotal = compteDAO.findByClient(client.id()).stream().map(Compte::getSolde).reduce(BigDecimal.ZERO, BigDecimal::add);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return new ClientSolde(client, soldeTotal);
        }).sorted(Comparator.comparing(ClientSolde::soldeTotal).reversed()).limit(5).toList();
    }


    public Map<TypeTransaction, Long> nombreTransactionsParType(int annee, int mois) throws SQLException {

        List<Transaction> transactions = transactionDAO.findAll();

        return transactions.stream().filter(transaction -> transaction.date().getYear() == annee && transaction.date().getMonthValue() == mois).collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }


    public BigDecimal volumeTotalMensuel(int annee, int mois) throws SQLException {

        List<Transaction> transactions = transactionDAO.findAll();

        return transactions.stream().filter(transaction -> transaction.date().getYear() == annee && transaction.date().getMonthValue() == mois).map(Transaction::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<Compte> comptesInactifs(int nombreJours) throws SQLException {

        if (nombreJours < 0) {
            throw new IllegalArgumentException("Le nombre de jours doit être positif.");
        }

        LocalDateTime limite = LocalDateTime.now().minusDays(nombreJours);

        List<Compte> comptes = compteDAO.findAll();

        List<Transaction> transactions = transactionDAO.findAll();

        return comptes.stream().filter(compte -> {

            Optional<LocalDateTime> derniereTransaction = transactions.stream().filter(transaction -> transaction.idCompte() == compte.getId()).map(Transaction::date).max(LocalDateTime::compareTo);

            return derniereTransaction.map(date -> date.isBefore(limite)).orElse(true);
        }).toList();
    }


    public List<Transaction> transactionsSuspectes() throws SQLException {

        List<Transaction> transactions = transactionDAO.findAll();

        return transactions.stream().filter(this::montantEleve).toList();
    }

    private boolean montantEleve(Transaction transaction) {

        return transaction.montant().compareTo(new BigDecimal("10000")) > 0;
    }


    public List<Compte> comptesSoldeBas() throws SQLException {

        return compteDAO.findAll().stream().filter(compte -> compte.getSolde().compareTo(SEUIL_SOLDE_BAS) < 0).toList();
    }


    public record ClientSolde(Client client, BigDecimal soldeTotal) {
    }
}