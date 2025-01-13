package ex1;
import java.sql.*;
import java.util.Scanner;

public class ex1 {
    private static final String URL = "jdbc:mysql://localhost:3306/lab8";
    private static final String USER = "root";
    private static final String PASSWORD = "password"; // Modificați parola dacă este cazul.

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Conectat la baza de date.");

            boolean exit = false;
            while (!exit) {
                System.out.println("\nMeniu:");
                System.out.println("1. Adaugă persoană");
                System.out.println("2. Adaugă excursie");
                System.out.println("3. Afișează persoane și excursii");
                System.out.println("4. Afișează excursiile unei persoane");
                System.out.println("5. Afișează persoanele care au vizitat o destinație");
                System.out.println("6. Afișează persoanele care au făcut excursii într-un an");
                System.out.println("7. Șterge excursie");
                System.out.println("8. Șterge persoană");
                System.out.println("9. Ieșire");

                System.out.print("Alege o opțiune: ");
                int optiune = scanner.nextInt();
                scanner.nextLine(); // Consumă newline

                switch (optiune) {
                    case 1 -> adaugaPersoana(connection, scanner);
                    case 2 -> adaugaExcursie(connection, scanner);
                    case 3 -> afiseazaPersoaneSiExcursii(connection);
                    case 4 -> afiseazaExcursiilePersoanei(connection, scanner);
                    case 5 -> afiseazaPersoanePentruDestinatie(connection, scanner);
                    case 6 -> afiseazaPersoanePentruAn(connection, scanner);
                    case 7 -> stergeExcursie(connection, scanner);
                    case 8 -> stergePersoana(connection, scanner);
                    case 9 -> exit = true;
                    default -> System.out.println("Opțiune invalidă!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void adaugaPersoana(Connection connection, Scanner scanner) {
        try {
            System.out.print("Introduceți numele: ");
            String nume = scanner.nextLine();

            System.out.print("Introduceți vârsta: ");
            int varsta = citesteVarsta(scanner);

            String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nume);
                stmt.setInt(2, varsta);
                stmt.executeUpdate();
                System.out.println("Persoană adăugată cu succes.");
            }
        } catch (ExceptieVarsta e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void adaugaExcursie(Connection connection, Scanner scanner) {
        try {
            System.out.print("Introduceți ID-ul persoanei: ");
            int idPersoana = scanner.nextInt();
            scanner.nextLine();

            String verificaPersoana = "SELECT COUNT(*) FROM persoane WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(verificaPersoana)) {
                stmt.setInt(1, idPersoana);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Persoana nu există în baza de date.");
                        return;
                    }
                }
            }

            System.out.print("Introduceți destinația: ");
            String destinatia = scanner.nextLine();

            System.out.print("Introduceți anul excursiei: ");
            int anul = citesteAnExcursie(scanner);

            String sql = "INSERT INTO excursii (id_persoana, destinatia, anul) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, idPersoana);
                stmt.setString(2, destinatia);
                stmt.setInt(3, anul);
                stmt.executeUpdate();
                System.out.println("Excursie adăugată cu succes.");
            }
        } catch (ExceptieAnExcursie e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int citesteVarsta(Scanner scanner) throws ExceptieVarsta {
        int varsta = scanner.nextInt();
        scanner.nextLine();
        if (varsta < 0 || varsta > 120) {
            throw new ExceptieVarsta("Vârsta trebuie să fie între 0 și 120.");
        }
        return varsta;
    }

    private static int citesteAnExcursie(Scanner scanner) throws ExceptieAnExcursie {
        int anul = scanner.nextInt();
        scanner.nextLine();
        int anulCurent = java.time.Year.now().getValue();
        if (anul < 1900 || anul > anulCurent) {
            throw new ExceptieAnExcursie("Anul excursiei trebuie să fie între 1900 și " + anulCurent + ".");
        }
        return anul;
    }

    private static void afiseazaPersoaneSiExcursii(Connection connection) throws SQLException {
        String sql = "SELECT p.id, p.nume, p.varsta, e.destinatia, e.anul " +
                "FROM persoane p LEFT JOIN excursii e ON p.id = e.id_persoana";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("ID: %d, Nume: %s, Vârsta: %d, Destinație: %s, An: %s%n",
                        rs.getInt("id"), rs.getString("nume"), rs.getInt("varsta"),
                        rs.getString("destinatia"), rs.getString("anul"));
            }
        }
    }

    private static void afiseazaExcursiilePersoanei(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Introduceți numele persoanei: ");
        String nume = scanner.nextLine();

        String sql = "SELECT e.destinatia, e.anul FROM persoane p " +
                "JOIN excursii e ON p.id = e.id_persoana WHERE p.nume = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nume);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Destinație: %s, An: %d%n", rs.getString("destinatia"), rs.getInt("anul"));
                }
            }
        }
    }

    private static void afiseazaPersoanePentruDestinatie(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Introduceți destinația: ");
        String destinatia = scanner.nextLine();

        String sql = "SELECT DISTINCT p.nume FROM persoane p " +
                "JOIN excursii e ON p.id = e.id_persoana WHERE e.destinatia = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, destinatia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Nume: %s%n", rs.getString("nume"));
                }
            }
        }
    }

    private static void afiseazaPersoanePentruAn(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Introduceți anul: ");
        int anul = scanner.nextInt();
        scanner.nextLine();

        String sql = "SELECT DISTINCT p.nume FROM persoane p " +
                "JOIN excursii e ON p.id = e.id_persoana WHERE e.anul = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, anul);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Nume: %s%n", rs.getString("nume"));
                }
            }
        }
    }

    private static void stergeExcursie(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Introduceți ID-ul excursiei: ");
        int idExcursie = scanner.nextInt();
        scanner.nextLine();

        String sql = "DELETE FROM excursii WHERE id_excursie = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idExcursie);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Excursie ștearsă cu succes.");
            } else {
                System.out.println("Excursie nu a fost găsită.");
            }
        }
    }

    private static void stergePersoana(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Introduceți ID-ul persoanei: ");
        int idPersoana = scanner.nextInt();
        scanner.nextLine();

        String sql = "DELETE FROM persoane WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersoana);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Persoană ștearsă cu succes.");
            } else {
                System.out.println("Persoana nu a fost găsită.");
            }
        }
    }
}
