package com.albaraka.bank.DAO;

import com.albaraka.bank.entity.Transaction;
import com.albaraka.bank.entity.TypeTransaction;
import com.albaraka.bank.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAO {

    public Transaction save(Transaction transaction) throws SQLException {

        String sql = """
                INSERT INTO transactions (
                    date_transaction,
                    montant,
                    type,
                    lieu,
                    id_compte
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(transaction.date()));

            statement.setBigDecimal(2, transaction.montant());

            statement.setString(3, transaction.type().name());

            statement.setString(4, transaction.lieu());

            statement.setInt(5, transaction.idCompte());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Transaction(result.getInt("id"), transaction.date(), transaction.montant(), transaction.type(), transaction.lieu(), transaction.idCompte());
                }
            }
        }

        throw new SQLException("Impossible de créer la transaction.");
    }


    public Optional<Transaction> findById(int id) throws SQLException {

        String sql = """
                SELECT id,
                       date_transaction,
                       montant,
                       type,
                       lieu,
                       id_compte
                FROM transactions
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return Optional.of(mapResultSetToTransaction(result));
                }
            }
        }

        return Optional.empty();
    }


    public List<Transaction> findByCompte(int idCompte) throws SQLException {

        String sql = """
                SELECT id,
                       date_transaction,
                       montant,
                       type,
                       lieu,
                       id_compte
                FROM transactions
                WHERE id_compte = ?
                ORDER BY date_transaction DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCompte);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    transactions.add(mapResultSetToTransaction(result));
                }
            }
        }

        return transactions;
    }


    public List<Transaction> findAll() throws SQLException {

        String sql = """
                SELECT id,
                       date_transaction,
                       montant,
                       type,
                       lieu,
                       id_compte
                FROM transactions
                ORDER BY date_transaction DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                transactions.add(mapResultSetToTransaction(result));
            }
        }

        return transactions;
    }


    public List<Transaction> findByType(TypeTransaction type) throws SQLException {

        String sql = """
                SELECT id,
                       date_transaction,
                       montant,
                       type,
                       lieu,
                       id_compte
                FROM transactions
                WHERE type = ?
                ORDER BY date_transaction DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.name());

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    transactions.add(mapResultSetToTransaction(result));
                }
            }
        }

        return transactions;
    }


    public boolean update(Transaction transaction) throws SQLException {

        String sql = """
                UPDATE transactions
                SET date_transaction = ?,
                    montant = ?,
                    type = ?,
                    lieu = ?,
                    id_compte = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(transaction.date()));

            statement.setBigDecimal(2, transaction.montant());

            statement.setString(3, transaction.type().name());

            statement.setString(4, transaction.lieu());

            statement.setInt(5, transaction.idCompte());

            statement.setInt(6, transaction.id());

            return statement.executeUpdate() > 0;
        }
    }


    public boolean delete(int id) throws SQLException {

        String sql = """
                DELETE FROM transactions
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }


    private Transaction mapResultSetToTransaction(ResultSet result) throws SQLException {

        int id = result.getInt("id");

        Timestamp timestamp = result.getTimestamp("date_transaction");

        TypeTransaction type = TypeTransaction.valueOf(result.getString("type"));

        return new Transaction(id, timestamp.toLocalDateTime(), result.getBigDecimal("montant"), type, result.getString("lieu"), result.getInt("id_compte"));
    }

    public Transaction save(Connection connection, Transaction transaction) throws SQLException {

        String sql = """
                INSERT INTO transactions (
                    date_transaction,
                    montant,
                    type,
                    lieu,
                    id_compte
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(transaction.date()));

            statement.setBigDecimal(2, transaction.montant());

            statement.setString(3, transaction.type().name());

            statement.setString(4, transaction.lieu());

            statement.setInt(5, transaction.idCompte());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Transaction(result.getInt("id"), transaction.date(), transaction.montant(), transaction.type(), transaction.lieu(), transaction.idCompte());
                }
            }
        }

        throw new SQLException("Impossible d'enregistrer la transaction.");
    }
}