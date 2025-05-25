import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String USER = "your_username"; // TODO: replace with your MySQL username
    private static final String PASSWORD = "your_password"; // TODO: replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class Book {
    private final String title;
    private final String author;
    private final String category;
    private final String subject;
    private boolean isAvailable;
    private String borrowedBy; // username of borrower, null if available
    private LocalDate borrowedDate;

    public Book(String title, String author, String category, String subject) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.subject = subject;
        this.isAvailable = true;
        this.borrowedBy = null;
        this.borrowedDate = null;
    }

    public Book(String title, String author, String category, String subject, boolean isAvailable, String borrowedBy, LocalDate borrowedDate) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.subject = subject;
        this.isAvailable = isAvailable;
        this.borrowedBy = borrowedBy;
        this.borrowedDate = borrowedDate;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public boolean isAvailable() { return isAvailable; }
    public String getBorrowedBy() { return borrowedBy; }
    public LocalDate getBorrowedDate() { return borrowedDate; }

    @Override
    public String toString() {
        String status = isAvailable ? "✅ Available" : "❌ Borrowed by " + (borrowedBy != null ? borrowedBy : "N/A");
        return String.format("📖 %-35s | ✍️ %-25s | 🏷️ %-15s | 📚 %-15s | %s",
                title, author, category, subject, status);
    }
}

class User {
    private final String username;
    private final String password;
    private final String fullName;
    private final String gender;
    private final String aadhaarNumber;
    private final String phoneNumber;
    private final String address;
    private boolean hasPaidFees;

    public User(String username, String password, String fullName, String gender, String aadhaarNumber,
                String phoneNumber, String address, boolean hasPaidFees) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.gender = gender;
        this.aadhaarNumber = aadhaarNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.hasPaidFees = hasPaidFees;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getGender() { return gender; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public boolean hasPaidFees() { return hasPaidFees; }

    public void setHasPaidFees(boolean paid) { this.hasPaidFees = paid; }

    @Override
    public String toString() {
        return String.format("👤 %-15s | 📛 %-25s | 🚻 %-6s | 🆔 %-12s | 📱 %-10s | 🏠 %-20s | %s",
                username, fullName, gender, aadhaarNumber, phoneNumber, address, (hasPaidFees ? "✅ Yes" : "❌ No"));
    }
}

class Library {
    private final String librarianUsername = "admin";
    private final String librarianPassword = "admin123";
    private int income = 0;
    private static final int FINE_PER_DAY = 1;
    private static final int BORROWING_PERIOD_DAYS = 15;
    private static final int MAX_BORROWED_BOOKS = 3;
    private static final int REGISTRATION_FEE = 100;

    public Library() {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found. Add the driver library to classpath.");
        }
    }

    // Add book in DB
    public void addBook(String title, String author, String category, String subject) {
        String checkQuery = "SELECT title FROM books WHERE title = ?";
        String insertQuery = "INSERT INTO books (title, author, category, subject, is_available) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("❌ A book with this title already exists.");
                return;
            }
            insertStmt.setString(1, title);
            insertStmt.setString(2, author);
            insertStmt.setString(3, category);
            insertStmt.setString(4, subject);
            insertStmt.executeUpdate();
            System.out.println("✅ Book added successfully: " + title);
        } catch (SQLException e) {
            System.out.println("❌ Error adding book: " + e.getMessage());
        }
    }

    // Remove book from DB
    public void removeBook(String title) {
        String checkQuery = "SELECT is_available FROM books WHERE title = ?";
        String deleteQuery = "DELETE FROM books WHERE title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Book not found.");
                return;
            }
            boolean isAvailable = rs.getBoolean("is_available");
            if (!isAvailable) {
                System.out.println("❌ Cannot remove a book that is currently borrowed.");
                return;
            }
            deleteStmt.setString(1, title);
            deleteStmt.executeUpdate();
            System.out.println("✅ Book removed successfully: " + title);
        } catch (SQLException e) {
            System.out.println("❌ Error removing book: " + e.getMessage());
        }
    }

    // Register user in DB
    public boolean registerUser(String username, String password, String fullName, String gender,
                                String aadhaarNumber, String phoneNumber, String address) {
        // Validation moved outside, only DB logic here
        String checkQuery = "SELECT username FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password, full_name, gender, aadhaar_number, phone_number, address, has_paid_fees) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("❌ Error: Username '" + username + "' is already taken. Please choose another.");
                return false;
            }
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, fullName);
            insertStmt.setString(4, gender);
            insertStmt.setString(5, aadhaarNumber);
            insertStmt.setString(6, phoneNumber);
            insertStmt.setString(7, address);
            insertStmt.executeUpdate();
            System.out.println("✅ User '" + username + "' registered successfully!");
            System.out.println("   ℹ️ Please note: A one-time registration fee of ₹" + REGISTRATION_FEE + " is required before borrowing books.");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Authenticate user with username and password
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("gender"),
                        rs.getString("aadhaar_number"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getBoolean("has_paid_fees")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    // Authenticate librarian
    public boolean authenticateLibrarian(String username, String password) {
        return username.equals(librarianUsername) && password.equals(librarianPassword);
    }

    // Borrow book - update DB and check constraints
    public void borrowBook(User user, String title) {
        if (!user.hasPaidFees()) {
            System.out.println("❌ You must pay the registration fee before borrowing books.");
            return;
        }
        List<Book> borrowedBooks = getBorrowedBooksByUser(user.getUsername());
        if (borrowedBooks.size() >= MAX_BORROWED_BOOKS) {
            System.out.println("❌ You have reached the borrowing limit (" + MAX_BORROWED_BOOKS + " books). Please return a book first.");
            return;
        }

        String checkQuery = "SELECT is_available FROM books WHERE title = ?";
        String updateQuery = "UPDATE books SET is_available = FALSE, borrowed_by = ?, borrowed_date = ? WHERE title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Book '" + title + "' not found.");
                return;
            }
            boolean isAvailable = rs.getBoolean("is_available");
            if (!isAvailable) {
                System.out.println("❌ Book '" + title + "' is currently borrowed.");
                return;
            }
            updateStmt.setString(1, user.getUsername());
            updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            updateStmt.setString(3, title);
            updateStmt.executeUpdate();
            System.out.println("✅ Book '" + title + "' borrowed successfully. Return by: " +
                    LocalDate.now().plusDays(BORROWING_PERIOD_DAYS));
        } catch (SQLException e) {
            System.out.println("❌ Error borrowing book: " + e.getMessage());
        }
    }

    // Return book - update DB and calculate fine if any
    public void returnBook(User user, String title) {
        String checkQuery = "SELECT borrowed_by, borrowed_date FROM books WHERE title = ?";
        String updateQuery = "UPDATE books SET is_available = TRUE, borrowed_by = NULL, borrowed_date = NULL WHERE title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Book '" + title + "' not found.");
                return;
            }
            String borrower = rs.getString("borrowed_by");
            java.sql.Date borrowedDateSql = rs.getDate("borrowed_date");
            if (borrower == null || !borrower.equals(user.getUsername())) {
                System.out.println("❌ You haven't borrowed a book with the title '" + title + "' or it was not found.");
                return;
            }
            LocalDate borrowedDate = borrowedDateSql.toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(borrowedDate, LocalDate.now());

            if (daysBetween > BORROWING_PERIOD_DAYS) {
                long daysOverdue = daysBetween - BORROWING_PERIOD_DAYS;
                int fine = (int) daysOverdue * FINE_PER_DAY;
                income += fine;
                System.out.println("⚠️ Book returned " + daysOverdue + " days late. Fine imposed: ₹" + fine);
            }
            updateStmt.setString(1, title);
            updateStmt.executeUpdate();
            System.out.println("✅ Book '" + title + "' returned successfully.");
        } catch (SQLException e) {
            System.out.println("❌ Error returning book: " + e.getMessage());
        }
    }

    // List all books in DB
    public void listAllBooks() {
        String query = "SELECT * FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- 📚 Library Book Collection ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");
                String subject = rs.getString("subject");
                boolean isAvailable = rs.getBoolean("is_available");
                String borrowedBy = rs.getString("borrowed_by");
                System.out.printf("📖 %-35s | ✍️ %-25s | 🏷️ %-15s | 📚 %-15s | %s%n",
                        title, author, category, subject, isAvailable ? "✅ Available" : "❌ Borrowed by " + borrowedBy);
            }
            if (!found) {
                System.out.println("ℹ️ The library currently has no books.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error listing books: " + e.getMessage());
        }
    }

    // List all users
    public void listAllUsers() {
        String query = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- 👥 Registered Library Users ---");
            boolean found = false;
            System.out.printf("%-15s | %-25s | %-6s | %-12s | %-10s | %-20s | %s%n",
                    "👤 Username", "📛 Full Name", "🚻 Gender", "🆔 Aadhaar", "📱 Phone", "🏠 Address", "💰 Fees Paid?");
            printSeparator('=');
            while (rs.next()) {
                found = true;
                String username = rs.getString("username");
                String fullName = rs.getString("full_name");
                String gender = rs.getString("gender");
                String aadhaar = rs.getString("aadhaar_number");
                String phone = rs.getString("phone_number");
                String address = rs.getString("address");
                boolean paidFees = rs.getBoolean("has_paid_fees");
                System.out.printf("%-15s | %-25s | %-6s | %-12s | %-10s | %-20s | %s%n",
                        username, fullName, gender, aadhaar, phone, address, paidFees ? "✅ Yes" : "❌ No");

                List<Book> borrowedBooks = getBorrowedBooksByUser(username);
                if (!borrowedBooks.isEmpty()) {
                    System.out.println("    📚 Borrowed Books:");
                    for (Book book : borrowedBooks) {
                        System.out.println("      - " + book.getTitle() + " (Borrowed on: " + book.getBorrowedDate() + ")");
                    }
                }
                printSeparator('-');
            }
            if (!found) {
                System.out.println("ℹ️ No users are registered yet.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error listing users: " + e.getMessage());
        }
    }

    // Get borrowed books by username from DB
    public List<Book> getBorrowedBooksByUser(String username) {
        List<Book> borrowedBooks = new ArrayList<>();
        String query = "SELECT * FROM books WHERE borrowed_by = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                borrowedBooks.add(new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getString("subject"),
                        rs.getBoolean("is_available"),
                        rs.getString("borrowed_by"),
                        rs.getDate("borrowed_date") != null ? rs.getDate("borrowed_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching borrowed books: " + e.getMessage());
        }
        return borrowedBooks;
    }

    public void showIncome() {
        System.out.println("\n--- 💰 Library Income Report ---");
        System.out.println("   Total Library Income (from fines & fees): ₹" + income);
        printSeparator('-');
    }

    public void collectFees(User user) {
        if (!user.hasPaidFees()) {
            String query = "UPDATE users SET has_paid_fees = TRUE WHERE username = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, user.getUsername());
                pstmt.executeUpdate();
                income += REGISTRATION_FEE;
                user.setHasPaidFees(true);
                System.out.println("✅ Registration fee of ₹" + REGISTRATION_FEE + " paid successfully for " + user.getUsername() + ".");
            } catch (SQLException e) {
                System.out.println("❌ Error collecting fees: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Fees already paid for " + user.getUsername() + ".");
        }
    }

    public void searchAndFilterBooks(String filter, String value) {
        String queryBase = "SELECT * FROM books";
        List<String> conditions = new ArrayList<>();
        switch (filter.toLowerCase()) {
            case "all":
                // no filter condition
                break;
            case "available":
                conditions.add("is_available = TRUE");
                break;
            case "borrowed":
                conditions.add("is_available = FALSE");
                break;
            case "category":
                conditions.add("category LIKE ?");
                break;
            case "subject":
                conditions.add("subject LIKE ?");
                break;
            default:
                System.out.println("❌ Invalid filter.");
                return;
        }
        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        String query = queryBase + whereClause;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (filter.equalsIgnoreCase("category") || filter.equalsIgnoreCase("subject")) {
                pstmt.setString(1, value);
            }
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            printBookListHeader();
            while (rs.next()) {
                found = true;
                System.out.printf("📖 %-35s | ✍️ %-25s | 🏷️ %-15s | 📚 %-15s | %s%n",
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getString("subject"),
                        rs.getBoolean("is_available") ? "✅ Available" : "❌ Borrowed");
            }
            if (!found) {
                System.out.println("   ℹ️ No books found matching your criteria.");
            }
            printSeparator('-');
        } catch (SQLException e) {
            System.out.println("❌ Error searching books: " + e.getMessage());
        }
    }

    private void printBookListHeader() {
        printSeparator('=');
        System.out.printf("%-35s | %-25s | %-15s | %-15s | %s%n",
                "📖 Title", "✍️ Author", "🏷️ Category", "📚 Subject", "🔍 Status");
        printSeparator('-');
    }

    public static void printSeparator(char c) {
        for (int i = 0; i < 110; i++) {
            System.out.print(c);
        }
        System.out.println();
    }

    public static void printCentered(String text, int width) {
        int padding = (width - text.length()) / 2;
        if (padding > 0) {
            System.out.printf("%" + padding + "s%s%" + padding + "s%n", "", text, "");
        } else {
            System.out.println(text);
        }
    }
}

public class LibraryManagement {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        // Add some sample data only if DB is empty (optional)
        addSampleDataIfEmpty();

        displayWelcomeScreen();

        mainLoop:
        while (true) {
            clearScreen();
            printHeader("🏛️ Library Management System");
            System.out.println("1. 👨‍🎓 Student Portal");
            System.out.println("2. 👩‍💼 Librarian Portal");
            System.out.println("3. 🚪 Exit");
            library.printSeparator('═');
            System.out.print("🖊️  Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    studentMenu();
                    break;
                case 2:
                    librarianMenu();
                    break;
                case 3:
                    System.out.println("\n✅ Thank you for using the Library System. Goodbye!");
                    break mainLoop;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
                    pressEnterToContinue();
            }
        }
        scanner.close();
    }

    private static void addSampleDataIfEmpty() {
        // Checks if books exist. If not, adds sample books.
        String countQuery = "SELECT COUNT(*) as cnt FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt("cnt") == 0) {
                System.out.println("ℹ️ Adding sample books to the library...");
                library.addBook("The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Sci-Fi", "Humor");
                library.addBook("Pride and Prejudice", "Jane Austen", "Classic", "Romance");
                library.addBook("To Kill a Mockingbird", "Harper Lee", "Fiction", "Classic");
                library.addBook("1984", "George Orwell", "Dystopian", "Political");
                library.addBook("The Lord of the Rings", "J.R.R. Tolkien", "Fantasy", "Adventure");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error checking book count: " + e.getMessage());
        }
    }

    private static void displayWelcomeScreen() {
        clearScreen();
        System.out.println();
        library.printSeparator('*');
        Library.printCentered("🏛️ WELCOME TO LIBRARY MANAGEMENT SYSTEM", 110);
        Library.printCentered("📚 Your Gateway to Knowledge", 110);
        library.printSeparator('*');
        System.out.println();
        Library.printCentered("Developed by: Your Name", 110);
        Library.printCentered("Version: 1.0", 110);
        System.out.println();
        pressEnterToContinue();
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clearing screen fails, just print some newlines
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    private static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void printHeader(String title) {
        System.out.println();
        library.printSeparator('═');
        Library.printCentered(title, 110);
        library.printSeparator('═');
    }

    private static int getIntInput() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.print("❌ Input cannot be empty. Please enter a number: ");
                    continue;
                }
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input. Please enter a number: ");
            }
        }
    }

    private static String getStringInput(String prompt) {
        String input = "";
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                break;
            } else {
                System.out.println("❌ Input cannot be empty. Please try again.");
            }
        }
        return input;
    }

    // --- Student Menus ---
    private static void studentMenu() {
        while (true) {
            clearScreen();
            printHeader("👨‍🎓 Student Portal");
            System.out.println("1. ✍️ Register New Student");
            System.out.println("2. 🔑 Student Login");
            System.out.println("3. ↩️ Back to Main Menu");
            library.printSeparator('═');
            System.out.print("🖊️  Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    registerStudent();
                    break;
                case 2:
                    User user = loginStudent();
                    if (user != null) {
                        studentLoggedInMenu(user);
                    }
                    break;
                case 3:
                    return;
                default:
                    System.out.println("❌ Invalid choice. Try again.");
                    pressEnterToContinue();
            }
        }
    }

    private static void registerStudent() {
        clearScreen();
        printHeader("✍️ New Student Registration");
        String username = getStringInput("Enter username: ");
        String password = getStringInput("Enter password: ");
        String fullName = getStringInput("Enter full name: ");

        String gender;
        while (true) {
            System.out.print("Enter gender (Male/Female/Trans): ");
            gender = scanner.nextLine().trim();
            if (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("Trans")) {
                gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
                break;
            }
            System.out.println("❌ Invalid input. Please enter Male, Female, or Trans.");
        }

        String aadhaarNumber;
        while (true) {
            aadhaarNumber = getStringInput("Enter Aadhaar number (12 digits): ");
            if (aadhaarNumber.matches("\\d{12}")) {
                break;
            }
            System.out.println("❌ Error: Aadhaar number must be exactly 12 digits long.");
        }

        String phoneNumber;
        while (true) {
            phoneNumber = getStringInput("Enter phone number (10 digits): ");
            if (phoneNumber.matches("\\d{10}")) {
                break;
            }
            System.out.println("❌ Error: Phone number must be exactly 10 digits long.");
        }

        String address = getStringInput("Enter address: ");

        library.registerUser(username, password, fullName, gender, aadhaarNumber, phoneNumber, address);
        pressEnterToContinue();
    }

    private static User loginStudent() {
        clearScreen();
        printHeader("🔑 Student Login");
        String username = getStringInput("Enter username: ");
        String password = getStringInput("Enter password: ");

        User user = library.authenticateUser(username, password);

        if (user == null) {
            System.out.println("❌ Invalid username or password.");
            pressEnterToContinue();
            return null;
        }

        System.out.println("✅ Login successful. Welcome " + user.getFullName() + "!");

        if (!user.hasPaidFees()) {
            System.out.println("\n⚠️ A one-time registration fee of ₹100 is pending.");
            System.out.print("   Do you want to pay now? (yes/no): ");
            String payChoice = scanner.nextLine().trim();
            if (payChoice.equalsIgnoreCase("yes")) {
                library.collectFees(user);
            } else {
                System.out.println("⚠️ You must pay the fee to borrow books. You can log in again later to pay.");
                pressEnterToContinue();
                return null;
            }
        }
        pressEnterToContinue();
        return user;
    }

    private static void studentLoggedInMenu(User user) {
        while (true) {
            clearScreen();
            printHeader("👋 Welcome, " + user.getUsername() + "!");
            System.out.println("1. 🔍 Search & View Books");
            System.out.println("2. 📖 Borrow a Book");
            System.out.println("3. ↩️ Return a Book");
            System.out.println("4. 📚 View My Borrowed Books");
            System.out.println("5. 🚪 Logout");
            library.printSeparator('═');
            System.out.print("🖊️  Choose an option: ");
            int option = getIntInput();

            switch (option) {
                case 1:
                    searchBookMenu();
                    break;
                case 2:
                    clearScreen();
                    printHeader("📖 Borrow a Book");
                    String borrowTitle = getStringInput("Enter the exact title of the book to borrow: ");
                    library.borrowBook(user, borrowTitle);
                    pressEnterToContinue();
                    break;
                case 3:
                    clearScreen();
                    printHeader("↩️ Return a Book");
                    String returnTitle = getStringInput("Enter the exact title of the book to return: ");
                    library.returnBook(user, returnTitle);
                    pressEnterToContinue();
                    break;
                case 4:
                    viewMyBooks(user);
                    pressEnterToContinue();
                    break;
                case 5:
                    System.out.println("✅ Logging out.");
                    pressEnterToContinue();
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
                    pressEnterToContinue();
            }
        }
    }

    private static void viewMyBooks(User user) {
        clearScreen();
        printHeader("📚 My Borrowed Books");
        List<Book> myBooks = library.getBorrowedBooksByUser(user.getUsername());
        if (myBooks.isEmpty()) {
            System.out.println("   ℹ️ You currently have no borrowed books.");
        } else {
            System.out.printf("%-35s | %-25s | %s%n", "📖 Title", "✍️ Author", "📅 Borrowed Date");
            Library.printSeparator('-');
            for (Book b : myBooks) {
                System.out.printf("%-35s | %-25s | %s%n", b.getTitle(), b.getAuthor(), b.getBorrowedDate());
            }
        }
        Library.printSeparator('-');
    }

    private static void searchBookMenu() {
        while (true) {
            clearScreen();
            printHeader("🔍 Search Books");
            System.out.println("1. 👀 View All Books");
            System.out.println("2. ✅ View Available Books");
            System.out.println("3. ❌ View Borrowed Books");
            System.out.println("4. 🏷️ Search by Category");
            System.out.println("5. 📚 Search by Subject");
            System.out.println("6. ↩️ Back to Student Menu");
            library.printSeparator('═');
            System.out.print("🖊️  Choose an option: ");
            int filterOption = getIntInput();

            switch (filterOption) {
                case 1:
                    library.searchAndFilterBooks("all", "");
                    pressEnterToContinue();
                    break;
                case 2:
                    library.searchAndFilterBooks("available", "");
                    pressEnterToContinue();
                    break;
                case 3:
                    library.searchAndFilterBooks("borrowed", "");
                    pressEnterToContinue();
                    break;
                case 4:
                    String category = getStringInput("Enter category: ");
                    library.searchAndFilterBooks("category", category);
                    pressEnterToContinue();
                    break;
                case 5:
                    String subject = getStringInput("Enter subject: ");
                    library.searchAndFilterBooks("subject", subject);
                    pressEnterToContinue();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
                    pressEnterToContinue();
            }
        }
    }

    // --- Librarian Menus ---
    private static void librarianMenu() {
        clearScreen();
        printHeader("👩‍💼 Librarian Login");
        String libUser = getStringInput("Enter librarian username: ");
        String libPass = getStringInput("Enter password: ");

        if (!library.authenticateLibrarian(libUser, libPass)) {
            System.out.println("❌ Invalid librarian credentials.");
            pressEnterToContinue();
            return;
        }

        System.out.println("✅ Librarian login successful.");
        pressEnterToContinue();

        while (true) {
            clearScreen();
            printHeader("👩‍💼 Librarian Menu");
            System.out.println("1. 📖 Add Book");
            System.out.println("2. 🗑️ Remove Book");
            System.out.println("3. 📚 List All Books");
            System.out.println("4. 👥 List All Users");
            System.out.println("5. 💰 View Library Income");
            System.out.println("6. 🚪 Logout");
            library.printSeparator('═');
            System.out.print("🖊️  Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    clearScreen();
                    printHeader("📖 Add New Book");
                    String title = getStringInput("Enter book title: ");
                    String author = getStringInput("Enter author: ");
                    String category = getStringInput("Enter category: ");
                    String subject = getStringInput("Enter subject: ");
                    library.addBook(title, author, category, subject);
                    pressEnterToContinue();
                    break;
                case 2:
                    clearScreen();
                    printHeader("🗑️ Remove Book");
                    String removeTitle = getStringInput("Enter title of the book to remove: ");
                    library.removeBook(removeTitle);
                    pressEnterToContinue();
                    break;
                case 3:
                    clearScreen();
                    library.listAllBooks();
                    pressEnterToContinue();
                    break;
                case 4:
                    clearScreen();
                    library.listAllUsers();
                    pressEnterToContinue();
                    break;
                case 5:
                    clearScreen();
                    library.showIncome();
                    pressEnterToContinue();
                    break;
                case 6:
                    System.out.println("✅ Logging out.");
                    pressEnterToContinue();
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
                    pressEnterToContinue();
            }
        }
    }
}

