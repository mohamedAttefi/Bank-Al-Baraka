package com.albaraka.bank.service;

import com.albaraka.bank.DAO.ClientDAO;
import com.albaraka.bank.entity.Client;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClientService {

    private final ClientDAO clientDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
    }

    // Ajouter un client
    public Client ajouter(Client client) throws SQLException {

        validateClient(client);

        return clientDAO.save(client);
    }

    // Modifier un client
    public boolean modifier(Client client) throws SQLException {

        validateClient(client);

        if (client.id() <= 0) {
            throw new IllegalArgumentException("L'identifiant du client est invalide.");
        }

        return clientDAO.update(client);
    }

    // Supprimer un client
    public boolean supprimer(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant du client est invalide.");
        }

        return clientDAO.delete(id);
    }

    // Rechercher par ID
    public Optional<Client> rechercherParId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant du client est invalide.");
        }

        return clientDAO.findById(id);
    }

    // Rechercher par nom
    public List<Client> rechercherParNom(String nom) throws SQLException {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide.");
        }

        return clientDAO.findByName(nom);
    }

    // Lister tous les clients
    public List<Client> lister() throws SQLException {
        return clientDAO.findAll();
    }

    // Validation métier
    private void validateClient(Client client) {

        if (client == null) {
            throw new IllegalArgumentException("Le client ne peut pas être null.");
        }

        if (client.nom() == null || client.nom().isBlank()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire.");
        }

        if (client.email() == null || client.email().isBlank()) {
            throw new IllegalArgumentException("L'email du client est obligatoire.");
        }

        if (!client.email().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException("Format d'email invalide.");
        }
    }
}