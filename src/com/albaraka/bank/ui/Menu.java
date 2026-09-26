package com.albaraka.bank.ui;

import com.albaraka.bank.entity.Client;
import com.albaraka.bank.entity.Compte;
import com.albaraka.bank.entity.Transaction;
import com.albaraka.bank.service.ClientService;
import com.albaraka.bank.service.CompteService;
import com.albaraka.bank.service.RapportService;
import com.albaraka.bank.service.TransactionService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private final Scanner scanner;

    private final ClientService clientService;
    private final CompteService compteService;
    private final TransactionService transactionService;
    private final RapportService rapportService;

    public Menu() {

        scanner = new Scanner(System.in);

        clientService = new ClientService();
        compteService = new CompteService();
        transactionService = new TransactionService();
        rapportService = new RapportService();
    }

    public void start() {

        boolean running = true;

        while (running) {

            afficherMenu();

            String choix = scanner.nextLine();

            try {

                switch (choix) {

                    case "1" -> menuClients();

                    case "2" -> menuComptes();

                    case "3" -> menuTransactions();

                    case "4" -> menuHistorique();

                    case "5" -> menuRapports();

                    case "6" -> menuAlertes();

                    case "0" -> {
                        running = false;
                        System.out.println("Au revoir !");
                    }

                    default -> System.out.println("Choix invalide.");
                }

            } catch (Exception e) {

                System.out.println("\nErreur : " + e.getMessage());
            }
        }

        scanner.close();
    }

    private void afficherMenu() {

        System.out.println();
        System.out.println("====================================");
        System.out.println("          AL BARAKA BANK");
        System.out.println("====================================");
        System.out.println("1. Gestion des clients");
        System.out.println("2. Gestion des comptes");
        System.out.println("3. Transactions");
        System.out.println("4. Historique des transactions");
        System.out.println("5. Analyse & rapports");
        System.out.println("6. Alertes");
        System.out.println("0. Quitter");
        System.out.println("====================================");
        System.out.print("Votre choix : ");
    }

    private void menuClients() throws SQLException {

        System.out.println();
        System.out.println("=== CLIENTS ===");
        System.out.println("1. Ajouter");
        System.out.println("2. Rechercher par ID");
        System.out.println("3. Rechercher par nom");
        System.out.println("4. Lister");
        System.out.println("5. Modifier");
        System.out.println("6. Supprimer");
        System.out.println("0. Retour");

        System.out.print("Choix : ");

        String choix = scanner.nextLine();

        switch (choix) {

            case "1" -> ajouterClient();

            case "2" -> rechercherClientParId();

            case "3" -> rechercherClientParNom();

            case "4" -> listerClients();

            case "5" -> modifierClient();

            case "6" -> supprimerClient();

            case "0" -> {
            }

            default -> System.out.println("Choix invalide.");
        }
    }

    private void listerClients() throws SQLException {

        System.out.println("\n=== LISTE DES CLIENTS ===");

        List<Client> clients = clientService.lister();

        if (clients.isEmpty()) {
            System.out.println("Aucun client trouvé.");
            return;
        }

        clients.forEach(System.out::println);
    }

    private void supprimerClient() throws SQLException {

        System.out.println("\n=== SUPPRIMER CLIENT ===");

        System.out.print("ID du client : ");

        int id = Integer.parseInt(
                scanner.nextLine()
        );

        boolean deleted = clientService.supprimer(id);

        if (deleted) {
            System.out.println(
                    "Client supprimé avec succès."
            );
        } else {
            System.out.println(
                    "Client introuvable."
            );
        }
    }

    private void menuComptes() throws SQLException {

        System.out.println();
        System.out.println("=== COMPTES ===");
        System.out.println("1. Créer compte courant");
        System.out.println("2. Créer compte épargne");
        System.out.println("3. Rechercher par numéro");
        System.out.println("4. Comptes d'un client");
        System.out.println("5. Solde maximum");
        System.out.println("6. Solde minimum");
        System.out.println("0. Retour");

        System.out.print("Choix : ");

        String choix = scanner.nextLine();

        switch (choix) {

            case "1" -> creerCompteCourant();

            case "2" -> creerCompteEpargne();

            case "3" -> rechercherCompte();

            case "4" -> comptesClient();

            case "5" -> soldeMaximum();

            case "6" -> soldeMinimum();

            case "0" -> {
            }

            default -> System.out.println("Choix invalide.");
        }
    }

    private void menuTransactions() throws SQLException {

        System.out.println();
        System.out.println("=== TRANSACTIONS ===");
        System.out.println("1. Versement");
        System.out.println("2. Retrait");
        System.out.println("3. Virement");
        System.out.println("0. Retour");

        System.out.print("Choix : ");

        String choix = scanner.nextLine();

        switch (choix) {

            case "1" -> effectuerVersement();

            case "2" -> effectuerRetrait();

            case "3" -> effectuerVirement();

            case "0" -> {
            }

            default -> System.out.println("Choix invalide.");
        }
    }

    private void menuHistorique() throws SQLException {

        System.out.println();
        System.out.println("=== HISTORIQUE ===");

        System.out.print("ID du compte : ");

        int idCompte = Integer.parseInt(scanner.nextLine());

        transactionService.rechercherParCompte(idCompte).forEach(System.out::println);
    }

    private void menuRapports() throws SQLException {

        System.out.println();
        System.out.println("=== RAPPORTS ===");

        System.out.println("1. Top 5 clients");

        System.out.println("2. Rapport mensuel");

        System.out.println("3. Transactions suspectes");

        System.out.println("4. Comptes inactifs");

        System.out.println("0. Retour");

        System.out.print("Choix : ");

        String choix = scanner.nextLine();

        switch (choix) {

            case "1" -> afficherTop5();

            case "2" -> afficherRapportMensuel();

            case "3" -> afficherSuspectes();

            case "4" -> afficherInactifs();

            case "0" -> {
            }

            default -> System.out.println("Choix invalide.");
        }
    }

    private void menuAlertes() throws SQLException {

        System.out.println();
        System.out.println("=== ALERTES ===");

        System.out.println("\nComptes avec solde bas :");

        rapportService.comptesSoldeBas().forEach(System.out::println);

        System.out.println("\nComptes inactifs :");

        rapportService.comptesInactifs(30).forEach(System.out::println);
    }

    private void ajouterClient() throws SQLException {

        System.out.println("\n=== AJOUTER CLIENT ===");

        System.out.print("Nom : ");
        String nom = scanner.nextLine();

        System.out.print("Email : ");
        String email = scanner.nextLine();

        Client client = new Client(0, nom, email);

        Client saved = clientService.ajouter(client);

        System.out.println("Client créé : " + saved);
    }

    private void rechercherClientParId() throws SQLException {

        System.out.print("ID : ");

        int id = Integer.parseInt(scanner.nextLine());

        clientService.rechercherParId(id).ifPresentOrElse(System.out::println, () -> System.out.println("Client introuvable."));
    }

    private void rechercherClientParNom() throws SQLException {

        System.out.print("Nom : ");

        String nom = scanner.nextLine();

        var clients = clientService.rechercherParNom(nom);

        if (clients.isEmpty()) {

            System.out.println("Aucun client trouvé.");

        } else {

            clients.forEach(System.out::println);
        }
    }

    private void modifierClient() throws SQLException {

        System.out.print("ID : ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Nouveau nom : ");
        String nom = scanner.nextLine();

        System.out.print("Nouvel email : ");
        String email = scanner.nextLine();

        Client client = new Client(id, nom, email);

        boolean updated = clientService.modifier(client);

        System.out.println(updated ? "Client modifié." : "Client introuvable.");
    }

    private void creerCompteCourant() throws SQLException {

        System.out.println("\n=== COMPTE COURANT ===");

        System.out.print("Numéro : ");
        String numero = scanner.nextLine();

        System.out.print("Solde initial : ");
        BigDecimal solde = new BigDecimal(scanner.nextLine());

        System.out.print("ID client : ");
        int idClient = Integer.parseInt(scanner.nextLine());

        System.out.print("Découvert autorisé : ");

        BigDecimal decouvert = new BigDecimal(scanner.nextLine());

        Compte compte = compteService.creerCompteCourant(numero, solde, idClient, decouvert);

        System.out.println("Compte créé : " + compte);
    }

    private void creerCompteEpargne() throws SQLException {

        System.out.println("\n=== COMPTE ÉPARGNE ===");

        System.out.print("Numéro : ");
        String numero = scanner.nextLine();

        System.out.print("Solde initial : ");
        BigDecimal solde = new BigDecimal(scanner.nextLine());

        System.out.print("ID client : ");
        int idClient = Integer.parseInt(scanner.nextLine());

        System.out.print("Taux d'intérêt : ");

        BigDecimal taux = new BigDecimal(scanner.nextLine());

        Compte compte = compteService.creerCompteEpargne(numero, solde, idClient, taux);

        System.out.println("Compte créé : " + compte);
    }

    private void rechercherCompte() throws SQLException {

        System.out.print("Numéro du compte : ");

        String numero = scanner.nextLine();

        compteService.rechercherParNumero(numero).ifPresentOrElse(System.out::println, () -> System.out.println("Compte introuvable."));
    }

    private void comptesClient() throws SQLException {

        System.out.print("ID client : ");

        int idClient = Integer.parseInt(scanner.nextLine());

        compteService.rechercherParClient(idClient).forEach(System.out::println);
    }

    private void soldeMaximum() throws SQLException {

        compteService.compteAvecSoldeMaximum().ifPresentOrElse(System.out::println, () -> System.out.println("Aucun compte."));
    }

    private void soldeMinimum() throws SQLException {

        compteService.compteAvecSoldeMinimum().ifPresentOrElse(System.out::println, () -> System.out.println("Aucun compte."));
    }

    private void effectuerVersement() throws SQLException {

        System.out.println("\n=== VERSEMENT ===");

        System.out.print("ID compte : ");

        int idCompte = Integer.parseInt(scanner.nextLine());

        System.out.print("Montant : ");

        BigDecimal montant = new BigDecimal(scanner.nextLine());

        System.out.print("Lieu : ");

        String lieu = scanner.nextLine();

        Transaction transaction = transactionService.effectuerVersement(idCompte, montant, lieu);

        System.out.println("Versement effectué.");

        System.out.println(transaction);
    }

    private void effectuerRetrait() throws SQLException {

        System.out.println("\n=== RETRAIT ===");

        System.out.print("ID compte : ");

        int idCompte = Integer.parseInt(scanner.nextLine());

        System.out.print("Montant : ");

        BigDecimal montant = new BigDecimal(scanner.nextLine());

        System.out.print("Lieu : ");

        String lieu = scanner.nextLine();

        Transaction transaction = transactionService.effectuerRetrait(idCompte, montant, lieu);

        System.out.println("Retrait effectué.");

        System.out.println(transaction);
    }

    private void effectuerVirement() throws SQLException {

        System.out.println("\n=== VIREMENT ===");

        System.out.print("Compte source : ");

        int source = Integer.parseInt(scanner.nextLine());

        System.out.print("Compte destination : ");

        int destination = Integer.parseInt(scanner.nextLine());

        System.out.print("Montant : ");

        BigDecimal montant = new BigDecimal(scanner.nextLine());

        System.out.print("Lieu : ");

        String lieu = scanner.nextLine();

        transactionService.effectuerVirement(source, destination, montant, lieu);

        System.out.println("Virement effectué avec succès.");
    }

    private void afficherTop5() throws SQLException {

        System.out.println("\n=== TOP 5 CLIENTS ===");

        rapportService.top5ClientsParSolde().forEach(clientSolde -> System.out.println(clientSolde.client().nom() + " | Solde : " + clientSolde.soldeTotal()));
    }

    private void afficherRapportMensuel() throws SQLException {

        System.out.print("Année : ");

        int annee = Integer.parseInt(scanner.nextLine());

        System.out.print("Mois : ");

        int mois = Integer.parseInt(scanner.nextLine());

        System.out.println("\n=== RAPPORT MENSUEL ===");

        rapportService.nombreTransactionsParType(annee, mois).forEach((type, nombre) -> System.out.println(type + " : " + nombre));

        System.out.println("Volume total : " + rapportService.volumeTotalMensuel(annee, mois));
    }

    private void afficherSuspectes() throws SQLException {

        System.out.println("\n=== TRANSACTIONS SUSPECTES ===");

        var suspectes = rapportService.transactionsSuspectes();

        if (suspectes.isEmpty()) {

            System.out.println("Aucune transaction suspecte.");

        } else {

            suspectes.forEach(System.out::println);
        }
    }

    private void afficherInactifs() throws SQLException {

        System.out.print("Inactif depuis combien de jours ? ");

        int jours = Integer.parseInt(scanner.nextLine());

        rapportService.comptesInactifs(jours).forEach(System.out::println);
    }
}