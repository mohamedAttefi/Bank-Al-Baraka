package com.albaraka.bank.DAO;

import com.albaraka.bank.entity.Client;
import com.albaraka.bank.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    public Client save(Client client) throws SQLException {

        String sql = """
                INSERT INTO client (nom, email)
                VALUES (?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.nom());
            statement.setString(2, client.email());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    int id = result.getInt("id");

                    return new Client(id, client.nom(), client.email());
                }
            }
        }

        throw new SQLException("Impossible de créer le client.");
    }

    // FIND BY ID
    public Optional<Client> findById(int id) throws SQLException {

        String sql = """
                SELECT id, nom, email
                FROM client
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Client client = new Client(result.getInt("id"), result.getString("nom"), result.getString("email"));

                    return Optional.of(client);
                }
            }
        }

        return Optional.empty();
    }

    public List<Client> findByName(String nom) throws SQLException {

        String sql = """
                SELECT id, nom, email
                FROM client
                WHERE LOWER(nom) LIKE LOWER(?)
                ORDER BY nom
                """;

        List<Client> clients = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + nom + "%");

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    clients.add(new Client(result.getInt("id"), result.getString("nom"), result.getString("email")));
                }
            }
        }

        return clients;
    }

    public List<Client> findAll() throws SQLException {

        String sql = """
                SELECT id, nom, email
                FROM client
                ORDER BY id
                """;

        List<Client> clients = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                clients.add(new Client(result.getInt("id"), result.getString("nom"), result.getString("email")));
            }
        }

        return clients;
    }

    // UPDATE
    public boolean update(Client client) throws SQLException {

        String sql = """
                UPDATE client
                SET nom = ?, email = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.nom());
            statement.setString(2, client.email());
            statement.setInt(3, client.id());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = """
                DELETE FROM client
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}