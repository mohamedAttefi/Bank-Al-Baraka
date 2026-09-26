package com.albaraka.bank.DAO;

import com.albaraka.bank.entity.Compte;
import com.albaraka.bank.entity.CompteCourant;
import com.albaraka.bank.entity.CompteEpargne;
import com.albaraka.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompteDAO {

    public Compte save(Compte compte) throws SQLException {

        String sql = """
                INSERT INTO compte (
                    numero,
                    solde,
                    id_client,
                    type_compte,
                    decouvert_autorise,
                    taux_interet
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, compte.getNumero());
            statement.setBigDecimal(2, compte.getSolde());
            statement.setInt(3, compte.getIdClient());

            if (compte instanceof CompteCourant courant) {

                statement.setString(4, "COURANT");
                statement.setBigDecimal(5, courant.getDecouvertAutorise());
                statement.setNull(6, Types.NUMERIC);

            } else if (compte instanceof CompteEpargne epargne) {

                statement.setString(4, "EPARGNE");
                statement.setNull(5, Types.NUMERIC);
                statement.setBigDecimal(6, epargne.getTauxInteret());

            } else {
                throw new SQLException("Type de compte inconnu.");
            }

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    int id = result.getInt("id");

                    return createCompteFromData(id, compte.getNumero(), compte.getSolde(), compte.getIdClient(), compte instanceof CompteCourant courant ? "COURANT" : "EPARGNE", compte instanceof CompteCourant courant ? courant.getDecouvertAutorise() : null, compte instanceof CompteEpargne epargne ? epargne.getTauxInteret() : null);
                }
            }
        }

        throw new SQLException("Impossible de créer le compte.");
    }

    public Optional<Compte> findById(int id) throws SQLException {

        String sql = """
                SELECT id,
                       numero,
                       solde,
                       id_client,
                       type_compte,
                       decouvert_autorise,
                       taux_interet
                FROM compte
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return Optional.of(mapResultSetToCompte(result));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Compte> findByNumero(String numero) throws SQLException {

        String sql = """
                SELECT id,
                       numero,
                       solde,
                       id_client,
                       type_compte,
                       decouvert_autorise,
                       taux_interet
                FROM compte
                WHERE numero = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, numero);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return Optional.of(mapResultSetToCompte(result));
                }
            }
        }

        return Optional.empty();
    }

    public List<Compte> findByClient(int idClient) throws SQLException {

        String sql = """
                SELECT id,
                       numero,
                       solde,
                       id_client,
                       type_compte,
                       decouvert_autorise,
                       taux_interet
                FROM compte
                WHERE id_client = ?
                ORDER BY id
                """;

        List<Compte> comptes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    comptes.add(mapResultSetToCompte(result));
                }
            }
        }

        return comptes;
    }

    public List<Compte> findAll() throws SQLException {

        String sql = """
                SELECT id,
                       numero,
                       solde,
                       id_client,
                       type_compte,
                       decouvert_autorise,
                       taux_interet
                FROM compte
                ORDER BY id
                """;

        List<Compte> comptes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                comptes.add(mapResultSetToCompte(result));
            }
        }

        return comptes;
    }

    public boolean update(Compte compte) throws SQLException {

        String sql = """
                UPDATE compte
                SET numero = ?,
                    solde = ?,
                    type_compte = ?,
                    decouvert_autorise = ?,
                    taux_interet = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, compte.getNumero());
            statement.setBigDecimal(2, compte.getSolde());

            if (compte instanceof CompteCourant courant) {

                statement.setString(3, "COURANT");

                statement.setBigDecimal(4, courant.getDecouvertAutorise());

                statement.setNull(5, Types.NUMERIC);

            } else if (compte instanceof CompteEpargne epargne) {

                statement.setString(3, "EPARGNE");

                statement.setNull(4, Types.NUMERIC);

                statement.setBigDecimal(5, epargne.getTauxInteret());

            } else {
                throw new SQLException("Type de compte inconnu.");
            }

            statement.setInt(6, compte.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = """
                DELETE FROM compte
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Compte mapResultSetToCompte(ResultSet result) throws SQLException {

        int id = result.getInt("id");
        String numero = result.getString("numero");
        BigDecimal solde = result.getBigDecimal("solde");
        int idClient = result.getInt("id_client");

        String typeCompte = result.getString("type_compte");

        BigDecimal decouvert = result.getBigDecimal("decouvert_autorise");

        BigDecimal taux = result.getBigDecimal("taux_interet");

        return createCompteFromData(id, numero, solde, idClient, typeCompte, decouvert, taux);
    }

    private Compte createCompteFromData(int id, String numero, BigDecimal solde, int idClient, String typeCompte, BigDecimal decouvert, BigDecimal taux) {

        return switch (typeCompte) {

            case "COURANT" -> new CompteCourant(id, numero, solde, idClient, decouvert);

            case "EPARGNE" -> new CompteEpargne(id, numero, solde, idClient, taux);

            default -> throw new IllegalArgumentException("Type de compte invalide : " + typeCompte);
        };
    }

    public boolean updateSolde(Connection connection, int idCompte, BigDecimal nouveauSolde) throws SQLException {

        String sql = """
                UPDATE compte
                SET solde = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, nouveauSolde);
            statement.setInt(2, idCompte);

            return statement.executeUpdate() > 0;
        }
    }
}