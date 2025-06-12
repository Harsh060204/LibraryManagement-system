import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Database utility class to manage JDBC connection.
 * USER: You MUST update the DB_URL, DB_USER, and DB_PASSWORD constants with your MySQL database details.
 */
class DatabaseUtil {
    // --- IMPORTANT: CONFIGURE YOUR DATABASE CONNECTION HERE ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root"; // Your MySQL username
    private static final String DB_PASSWORD = "Harsh111@"; // Your MySQL password

    static {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC driver. Please ensure the connector JAR is in the classpath.", e);
        }
    }

    /**
     * Establishes a connection to the database.
     * @return A Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}


/**
 * Represents a book in the library.
 */
class Book {
    private String title;
    private String author;
    private String category;
    private String subject;
    private boolean isAvailable;
    private String borrowedByUsername; // Changed from User object to username string
    private LocalDate borrowedDate;
    private String currentBorrowingId;
    private String locationRow;
    private String locationSection;
    private String locationBlock;
    private String imagePath;

    public Book(String title, String author, String category, String subject,
                String locationRow, String locationSection, String locationBlock, String imagePath,
                boolean isAvailable, String borrowedByUsername, LocalDate borrowedDate, String currentBorrowingId) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.subject = subject;
        this.locationRow = locationRow;
        this.locationSection = locationSection;
        this.locationBlock = locationBlock;
        this.imagePath = imagePath;
        this.isAvailable = isAvailable;
        this.borrowedByUsername = borrowedByUsername;
        this.borrowedDate = borrowedDate;
        this.currentBorrowingId = currentBorrowingId;
    }


    // --- Getters ---
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public boolean isAvailable() { return isAvailable; }
    public String getBorrowedByUsername() { return borrowedByUsername; }
    public LocalDate getBorrowedDate() { return borrowedDate; }
    public String getCurrentBorrowingId() { return currentBorrowingId; }
    public String getLocationRow() { return locationRow; }
    public String getLocationSection() { return locationSection; }
    public String getLocationBlock() { return locationBlock; }
    public String getImagePath() { return imagePath; }

    // --- Setters for local changes (DB is the source of truth) ---
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setLocationRow(String locationRow) { this.locationRow = locationRow; }
    public void setLocationSection(String locationSection) { this.locationSection = locationSection; }
    public void setLocationBlock(String locationBlock) { this.locationBlock = locationBlock; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }


    @Override
    public String toString() {
        String status = isAvailable ? "Available" : "Borrowed by " + (borrowedByUsername != null ? borrowedByUsername : "N/A");
        if (!isAvailable && currentBorrowingId != null) {
            status += " (ID: " + currentBorrowingId + ")";
        }
        return String.format("Title: %s, Author: %s, Category: %s, Subject: %s, Status: %s",
                title, author, category, subject, status);
    }
}

/**
 * Represents a user (student) of the library.
 */
class User {
    private String username;
    private String password; // Should be hashed in a real application
    private final String fullName;
    private final String gender;
    private final String aadhaarNumber;
    private final String phoneNumber;
    private final String address;
    private final String role; // "student" or "librarian"
    private boolean hasPaidFees;
    // Borrowed books are now managed in the database, not a local list

    public User(String username, String password, String fullName, String gender, String aadhaarNumber,
                String phoneNumber, String address, String role, boolean hasPaidFees) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.gender = gender;
        this.aadhaarNumber = aadhaarNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.hasPaidFees = hasPaidFees;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getGender() { return gender; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public boolean hasPaidFees() { return hasPaidFees; }
    public List<Book> getBorrowedBooks() {
        // This method now needs to query the database.
        // For simplicity in this refactor, this logic is kept in the Library class.
        // In a more complex MVC pattern, this would be in a UserDAO.
        return Library.getBorrowedBooksForUser(this.username);
    }

    // --- Setters ---
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setHasPaidFees(boolean hasPaidFees) { this.hasPaidFees = hasPaidFees; }

     @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", role='" + role + '\'' +
               '}';
    }
}

enum ActivityType {
    BOOK_BORROWED("Book Borrowed"), BOOK_RETURNED("Book Returned"),
    REGISTRATION_FEE_PAID("Reg. Fee Paid"), LATE_FINE_PAID("Late Fine Paid"),
    ACCOUNT_CREATED("Account Created"), ACCOUNT_DELETED("Account Deleted"),
    STUDENT_CREDENTIALS_UPDATED("Student Credentials Updated"), STUDENT_PASSWORD_RESET("Student Password Reset"),
    BOOK_ADDED("Book Added"), BOOK_EDITED("Book Edited"), BOOK_REMOVED("Book Removed"),
    LIBRARIAN_CREDENTIALS_UPDATED("Librarian Credentials Updated");

    private final String displayName;
    ActivityType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}

class TransactionRecord {
    private final String transactionId;
    private final String studentUsername;
    private final String bookTitle;
    private final ActivityType type;
    private final LocalDate date;
    private final String details;
    private final double amount;

    public TransactionRecord(String transactionId, String studentUsername, String bookTitle, ActivityType type,
                             LocalDate date, String details, double amount) {
        this.transactionId = transactionId;
        this.studentUsername = studentUsername;
        this.bookTitle = bookTitle;
        this.type = type;
        this.date = date;
        this.details = details;
        this.amount = amount;
    }

    public String getTransactionId() { return transactionId; }
    public String getStudentUsername() { return studentUsername != null ? studentUsername : "N/A (Admin Action)"; }
    public String getBookTitle() { return bookTitle != null ? bookTitle : "N/A"; }
    public ActivityType getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDetails() { return details; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("[%s] %s - User: %s, Type: %s, Details: %s, Amount: %.2f, Book: %s",
                transactionId, date.format(DateTimeFormatter.ISO_DATE), getStudentUsername(),
                type.getDisplayName(), details, amount, getBookTitle());
    }
}


/**
 * Represents the library, managing books, users, and operations via a database.
 */
class Library {
    // In-memory collections are now removed. All data is fetched from the database.
    private static final String LIBRARIAN_ACCESS_KEY = "SECRET123";

    private static final int FINE_PER_DAY = 1;
    public static final int BORROWING_PERIOD_DAYS = 15;
    private static final int MAX_BORROWED_BOOKS = 3;
    private static final int REGISTRATION_FEE = 100;
    private final Random random = new Random();

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$";


    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < 8 || password.length() > 16) errors.add("Password must be 8-16 characters.");
        if (!Pattern.compile(".*[A-Z].*").matcher(password).matches()) errors.add("Requires one uppercase letter.");
        if (!Pattern.compile(".*[a-z].*").matcher(password).matches()) errors.add("Requires one lowercase letter.");
        if (!Pattern.compile(".*[0-9].*").matcher(password).matches()) errors.add("Requires one digit.");
        if (!Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*").matcher(password).matches()) errors.add("Requires one special character.");
        return errors;
    }

    private void logTransaction(String studentUsername, String bookTitle, ActivityType type, String details, double amount) {
        String sql = "INSERT INTO transactions (student_username, book_title, activity_type, details, amount, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, bookTitle);
            pstmt.setString(3, type.name());
            pstmt.setString(4, details);
            pstmt.setDouble(5, amount);
            pstmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // In a real app, show an error to the user
        }
    }

    public String addBook(String title, String author, String category, String subject, String row, String section, String block, String imagePath) {
        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || subject.isEmpty()) {
            return "(!) Error: Core book fields are mandatory.";
        }
        if (searchBook(title) != null) {
            return "(!) A book with this title already exists.";
        }
        String sql = "INSERT INTO books (title, author, category, subject, location_row, location_section, location_block, image_path, is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, category);
            pstmt.setString(4, subject);
            pstmt.setString(5, row);
            pstmt.setString(6, section);
            pstmt.setString(7, block);
            pstmt.setString(8, imagePath);
            pstmt.setBoolean(9, true);
            pstmt.executeUpdate();
            logTransaction(null, title, ActivityType.BOOK_ADDED, "Book added: '" + title + "' by " + author, 0);
            return "(+) Book added successfully: " + title;
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) Database error while adding book: " + e.getMessage();
        }
    }

    public String editBook(String originalTitle, String newTitle, String newAuthor, String newCategory, String newSubject,
                           String newRow, String newSection, String newBlock, String newImagePath) {
        Book bookToEdit = searchBook(originalTitle);
        if (bookToEdit == null) {
            return "(!) Error: The book to edit was not found.";
        }
        if (!bookToEdit.isAvailable()) {
            return "(!) Error: Cannot edit a borrowed book.";
        }
        if (!originalTitle.equals(newTitle) && searchBook(newTitle) != null) {
            return "(!) Error: Another book with the new title '" + newTitle + "' already exists.";
        }

        String sql = "UPDATE books SET title = ?, author = ?, category = ?, subject = ?, location_row = ?, " +
                     "location_section = ?, location_block = ?, image_path = ? WHERE title = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newAuthor);
            pstmt.setString(3, newCategory);
            pstmt.setString(4, newSubject);
            pstmt.setString(5, newRow);
            pstmt.setString(6, newSection);
            pstmt.setString(7, newBlock);
            pstmt.setString(8, newImagePath);
            pstmt.setString(9, originalTitle);
            pstmt.executeUpdate();
            logTransaction(null, newTitle, ActivityType.BOOK_EDITED, "Book edited: '" + originalTitle + "' to '" + newTitle + "'", 0);
            return "(+) Book details updated successfully for: " + newTitle;
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) Database error during book update: " + e.getMessage();
        }
    }

    public String removeBook(String title) {
        Book bookToRemove = searchBook(title);
        if (bookToRemove != null) {
            if (!bookToRemove.isAvailable()) {
                return "(!) Cannot remove a book that is currently borrowed.";
            } else {
                String sql = "DELETE FROM books WHERE title = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, title);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        logTransaction(null, title, ActivityType.BOOK_REMOVED, "Book removed: '" + title + "'", 0);
                        return "(-) Book removed successfully: " + title;
                    }
                    return "(!) Error removing book, record not found after check.";
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "(!) Database error while removing book: " + e.getMessage();
                }
            }
        } else {
            return "(!) Book not found: " + title;
        }
    }

    public String registerUser(String username, String password, String fullName, String gender,
                               String aadhaarNumber, String phoneNumber, String address) {
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || aadhaarNumber.isEmpty() ||
            phoneNumber.isEmpty() || address.isEmpty() || gender == null || gender.isEmpty()) {
            return "(!) Error: All fields are mandatory.";
        }
        List<String> passwordErrors = validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            return "(!) Password validation failed:\n- " + String.join("\n- ", passwordErrors);
        }
        if (!Arrays.asList("Male", "Female", "Trans").contains(gender)) {
            return "(!) Error: Invalid gender.";
        }
        if (!aadhaarNumber.matches("\\d{12}") || !phoneNumber.matches("\\d{10}")) {
            return "(!) Error: Aadhaar must be 12 digits and Phone must be 10 digits.";
        }
        if (findUserByUsername(username) != null) {
            return "(!) Error: Username '" + username + "' is already taken.";
        }
        String sql = "INSERT INTO users (username, password, full_name, gender, aadhaar_number, phone_number, address, role, has_paid_fees) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, hash this password
            pstmt.setString(3, fullName);
            pstmt.setString(4, gender);
            pstmt.setString(5, aadhaarNumber);
            pstmt.setString(6, phoneNumber);
            pstmt.setString(7, address);
            pstmt.setString(8, "student"); // Default role
            pstmt.setBoolean(9, false); // Fees not paid on registration
            pstmt.executeUpdate();
            logTransaction(username, null, ActivityType.ACCOUNT_CREATED, "Account created for " + fullName, 0);
            return "(+) User '" + username + "' registered successfully!\nA one-time fee of ₹" + REGISTRATION_FEE + " is required.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) Database error during registration: " + e.getMessage();
        }
    }

    public User authenticateUser(String username, String password) {
        User user = findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean authenticateLibrarian(String username, String password) {
        User user = findUserByUsername(username);
        return user != null && "librarian".equals(user.getRole()) && user.getPassword().equals(password);
    }

    public String updateLibrarianCredentials(String currentUsername, String currentPassword,
                                             String newUsername, String newPassword,
                                             String enteredAccessKey) {
        if (newUsername.trim().isEmpty() || newPassword.isEmpty()) return "(!) New username/password cannot be empty.";
        if (!validatePassword(newPassword).isEmpty()) return "(!) New password validation failed.";
        if (!LIBRARIAN_ACCESS_KEY.equals(enteredAccessKey)) return "(!) Invalid Access Key.";

        User librarian = findUserByUsername(currentUsername);
        if (librarian == null || !"librarian".equals(librarian.getRole()) || !librarian.getPassword().equals(currentPassword)) {
            return "(!) Incorrect current credentials.";
        }

        if (!currentUsername.equals(newUsername) && findUserByUsername(newUsername) != null) {
            return "(!) New username is already taken.";
        }
        
        String sql = "UPDATE users SET username = ?, password = ? WHERE username = ? AND role = 'librarian'";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername.trim());
            pstmt.setString(2, newPassword);
            pstmt.setString(3, currentUsername);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                 logTransaction(newUsername, null, ActivityType.LIBRARIAN_CREDENTIALS_UPDATED, "Librarian '" + currentUsername + "' changed to '" + newUsername + "'", 0);
                 return "(+) Librarian credentials updated. Please log out and log in again.";
            }
            return "(!) Failed to update librarian credentials in DB.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on update: " + e.getMessage();
        }
    }

    public String resetLibrarianCredentialsWithAccessKey(String enteredAccessKey, String newUsername, String newPassword) {
         if (newUsername.trim().isEmpty() || newPassword.isEmpty()) return "(!) New username/password cannot be empty.";
         if (!validatePassword(newPassword).isEmpty()) return "(!) New password validation failed.";
         if (!LIBRARIAN_ACCESS_KEY.equals(enteredAccessKey)) return "(!) Invalid Access Key.";

        // This is a simplified reset. A real app might have a fixed admin user or other recovery.
        // This will reset the FIRST librarian account found.
        String sql = "UPDATE users SET username = ?, password = ? WHERE role = 'librarian' LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername.trim());
            pstmt.setString(2, newPassword);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                 logTransaction(newUsername, null, ActivityType.LIBRARIAN_CREDENTIALS_UPDATED, "Librarian reset to '" + newUsername + "' via access key", 0);
                 return "(+) Librarian credentials reset successfully.";
            }
            return "(!) No librarian account found to reset.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on reset: " + e.getMessage();
        }
    }

    public String updateStudentCredentials(String currentUsername, String newUsername, String newPassword) {
        User student = findUserByUsername(currentUsername);
        if (student == null) return "(!) Student '" + currentUsername + "' not found.";
        
        String effectiveNewUsername = (newUsername == null || newUsername.trim().isEmpty()) ? currentUsername : newUsername.trim();
        String effectiveNewPassword = (newPassword == null || newPassword.isEmpty()) ? student.getPassword() : newPassword;

        if (newPassword != null && !newPassword.isEmpty() && !validatePassword(newPassword).isEmpty()) {
            return "(!) New password validation failed.";
        }
        if (!currentUsername.equals(effectiveNewUsername) && findUserByUsername(effectiveNewUsername) != null) {
            return "(!) The new username '" + effectiveNewUsername + "' is already taken.";
        }

        String sql = "UPDATE users SET username = ?, password = ? WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, effectiveNewUsername);
            pstmt.setString(2, effectiveNewPassword);
            pstmt.setString(3, currentUsername);
            pstmt.executeUpdate();
            logTransaction(effectiveNewUsername, null, ActivityType.STUDENT_CREDENTIALS_UPDATED, "Credentials updated for " + currentUsername, 0);
            return "(+) Student credentials updated successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on update: " + e.getMessage();
        }
    }
    
    public String deleteStudent(String studentUsername) {
        User student = findUserByUsername(studentUsername);
        if (student == null) return "(!) Student '" + studentUsername + "' not found.";
        if (!student.getBorrowedBooks().isEmpty()) return "(!) Cannot delete student with borrowed books.";

        String sql = "DELETE FROM users WHERE username = ? AND role = 'student'";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logTransaction(studentUsername, null, ActivityType.ACCOUNT_DELETED, "Account deleted for " + student.getFullName(), 0);
                return "(-) Student account '" + studentUsername + "' deleted.";
            }
            return "(!) Failed to delete student from DB.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on deletion: " + e.getMessage();
        }
    }

    public User verifyStudentDetailsForPasswordReset(String username, String aadhaar, String phone) {
        String sql = "SELECT * FROM users WHERE username = ? AND aadhaar_number = ? AND phone_number = ? AND role = 'student'";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, aadhaar);
            pstmt.setString(3, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String resetStudentPassword(String username, String newPassword) {
        if (!validatePassword(newPassword).isEmpty()) return "(!) New password validation failed.";
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            logTransaction(username, null, ActivityType.STUDENT_PASSWORD_RESET, "Password reset for " + username, 0);
            return "(+) Password for '" + username + "' has been reset.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on password reset: " + e.getMessage();
        }
    }

    public Book searchBook(String title) {
        String sql = "SELECT * FROM books WHERE title = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createBookFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return getFilteredBooks("all", "");
    }

    public List<Book> getFilteredBooks(String filter, String value) {
        List<Book> bookList = new ArrayList<>();
        String sql = "SELECT * FROM books";
        if (!"all".equalsIgnoreCase(filter)) {
            switch (filter.toLowerCase()) {
                case "available": sql += " WHERE is_available = true"; break;
                case "borrowed": sql += " WHERE is_available = false"; break;
                case "category": sql += " WHERE category = ?"; break;
                case "subject": sql += " WHERE subject = ?"; break;
            }
        }
        sql += " ORDER BY title";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (filter.equalsIgnoreCase("category") || filter.equalsIgnoreCase("subject")) {
                pstmt.setString(1, value);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookList.add(createBookFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    private String generateUniqueBorrowingId() {
        String id;
        do {
            id = String.format("TXN%06d", random.nextInt(1_000_000));
        } while (isBorrowingIdActive(id));
        return id;
    }

    private boolean isBorrowingIdActive(String id) {
        String sql = "SELECT COUNT(*) FROM books WHERE current_borrowing_id = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String borrowBook(User user, String title) {
        if (!user.hasPaidFees()) return "(!) You must pay the registration fee before borrowing.";
        if (user.getBorrowedBooks().size() >= MAX_BORROWED_BOOKS) return "(!) You have reached the borrowing limit.";
        
        Book book = searchBook(title);
        if (book != null && book.isAvailable()) {
            String borrowingId = generateUniqueBorrowingId();
            String sql = "UPDATE books SET is_available = false, borrowed_by_username = ?, borrowed_date = ?, current_borrowing_id = ? WHERE title = ?";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setString(3, borrowingId);
                pstmt.setString(4, title);
                pstmt.executeUpdate();
                logTransaction(user.getUsername(), title, ActivityType.BOOK_BORROWED, "Book borrowed (ID: " + borrowingId + ")", 0);
                return String.format("(>) Book '%s' borrowed successfully.\nReturn by: %s\nBorrowing ID: %s",
                                     title, LocalDate.now().plusDays(BORROWING_PERIOD_DAYS).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), borrowingId);
            } catch (SQLException e) {
                e.printStackTrace();
                return "(!) DB error during borrow: " + e.getMessage();
            }
        } else if (book != null) {
            return "(!) Book '" + title + "' is currently borrowed by " + book.getBorrowedByUsername() + ".";
        } else {
            return "(!) Book '" + title + "' not found.";
        }
    }
    
    public String returnBook(User user, String title) {
        Book bookToReturn = null;
        for (Book book : user.getBorrowedBooks()) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                bookToReturn = book;
                break;
            }
        }
        if (bookToReturn != null) {
            LocalDate borrowedDate = bookToReturn.getBorrowedDate();
            long daysBetween = ChronoUnit.DAYS.between(borrowedDate, LocalDate.now());
            String message = "";
            double fine = 0;

            if (daysBetween > BORROWING_PERIOD_DAYS) {
                long daysOverdue = daysBetween - BORROWING_PERIOD_DAYS;
                fine = daysOverdue * FINE_PER_DAY;
                message = "(!) Book returned " + daysOverdue + " days late. Fine imposed: ₹" + fine + "\n";
                logTransaction(user.getUsername(), title, ActivityType.LATE_FINE_PAID, "Late fine for " + title, fine);
            }
            
            String sql = "UPDATE books SET is_available = true, borrowed_by_username = NULL, borrowed_date = NULL, current_borrowing_id = NULL WHERE title = ?";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.executeUpdate();
                logTransaction(user.getUsername(), title, ActivityType.BOOK_RETURNED, "Book returned", 0);
                return message + "(<) Book '" + title + "' returned successfully.";
            } catch (SQLException e) {
                 e.printStackTrace();
                return "(!) DB error during return: " + e.getMessage();
            }
        } else {
            return "(!) You haven't borrowed a book with the title '" + title + "'.";
        }
    }

    public String getBorrowingDetailsById(String borrowingId) {
        if (borrowingId == null || borrowingId.trim().isEmpty()) return "(!) Borrowing ID cannot be empty.";
        
        String sql = "SELECT * FROM books b JOIN users u ON b.borrowed_by_username = u.username WHERE b.current_borrowing_id = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, borrowingId);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                     LocalDate borrowedOn = rs.getDate("borrowed_date").toLocalDate();
                     LocalDate dueDate = borrowedOn.plusDays(BORROWING_PERIOD_DAYS);
                     return String.format("--- Borrowing Details (ID: %s) ---\n" +
                                     "Book Title: %s\n" +
                                     "Author: %s\n" +
                                     "Student Username: %s\n" +
                                     "Student Name: %s\n" +
                                     "Borrowed On: %s\n" +
                                     "Due Date: %s",
                                     borrowingId, rs.getString("title"), rs.getString("author"),
                                     rs.getString("username"), rs.getString("full_name"),
                                     borrowedOn.format(formatter), dueDate.format(formatter));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error: " + e.getMessage();
        }
        return "(!) No active borrowing transaction found with ID: " + borrowingId;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userList.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }
    
    public double getIncome() {
        String sql = "SELECT SUM(amount) FROM transactions WHERE activity_type IN ('LATE_FINE_PAID', 'REGISTRATION_FEE_PAID')";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public int getRegistrationFeeAmount() {
        return REGISTRATION_FEE;
    }

    public String collectFees(User user) {
        if (user.hasPaidFees()) return "(!) Fees already paid for " + user.getUsername() + ".";
        
        String sql = "UPDATE users SET has_paid_fees = true WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
            user.setHasPaidFees(true);
            logTransaction(user.getUsername(), null, ActivityType.REGISTRATION_FEE_PAID, "Registration fee paid", REGISTRATION_FEE);
            return "(+) Registration fee of ₹" + REGISTRATION_FEE + " paid successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "(!) DB error on fee collection: " + e.getMessage();
        }
    }

    public List<TransactionRecord> getTransactionsForUser(String username) {
        List<TransactionRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE student_username = ? ORDER BY transaction_date DESC, id DESC";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) records.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
     public static List<Book> getBorrowedBooksForUser(String username) {
        List<Book> borrowedBooks = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE borrowed_by_username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    borrowedBooks.add(createBookFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowedBooks;
    }
    

    // --- Helper Methods to create objects from ResultSet ---
    private static Book createBookFromResultSet(ResultSet rs) throws SQLException {
        java.sql.Date borrowedDateSql = rs.getDate("borrowed_date");
        LocalDate borrowedDate = (borrowedDateSql != null) ? borrowedDateSql.toLocalDate() : null;
        return new Book(
                rs.getString("title"), rs.getString("author"), rs.getString("category"),
                rs.getString("subject"), rs.getString("location_row"), rs.getString("location_section"),
                rs.getString("location_block"), rs.getString("image_path"), rs.getBoolean("is_available"),
                rs.getString("borrowed_by_username"), borrowedDate, rs.getString("current_borrowing_id")
        );
    }
    
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("username"), rs.getString("password"), rs.getString("full_name"),
                rs.getString("gender"), rs.getString("aadhaar_number"), rs.getString("phone_number"),
                rs.getString("address"), rs.getString("role"), rs.getBoolean("has_paid_fees")
        );
    }
    
    private TransactionRecord createTransactionFromResultSet(ResultSet rs) throws SQLException {
        return new TransactionRecord(
            "ACT" + rs.getInt("id"),
            rs.getString("student_username"),
            rs.getString("book_title"),
            ActivityType.valueOf(rs.getString("activity_type")),
            rs.getDate("transaction_date").toLocalDate(),
            rs.getString("details"),
            rs.getDouble("amount")
        );
    }
}


public class LibraryManagementGUI extends JFrame {
    private static final Library library = new Library();
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentStudent;

    // Panels
    private JPanel homePanel;
    private JPanel studentLoginPanel;
    private JPanel studentRegisterPanel;
    private JPanel studentDashboardPanel;
    private JPanel librarianLoginPanel;
    private JPanel librarianDashboardPanel;
    private JPanel bookSearchPanel;
    private JPanel myBooksPanel;

    // --- New Light & Bright Color Palette ---
    private static final Color COLOR_BACKGROUND_PRIMARY = new Color(248, 250, 252);
    private static final Color COLOR_BACKGROUND_SECONDARY = Color.WHITE;
    private static final Color COLOR_LEFT_PANE_DECORATIVE = new Color(229, 239, 252);
    private static final Color COLOR_TEXT_PRIMARY = new Color(28, 28, 28);
    private static final Color COLOR_TEXT_SECONDARY = new Color(74, 85, 104);
    private static final Color COLOR_TEXT_ON_IMAGE = Color.WHITE;
    private static final Color COLOR_LABEL_BG_ON_IMAGE = new Color(0, 0, 0, 100);

    private static final Color COLOR_ACCENT_PRIMARY = new Color(59, 130, 246);
    private static final Color COLOR_ACCENT_PRIMARY_HOVER = new Color(37, 99, 235);
    private static final Color COLOR_ACCENT_SECONDARY = new Color(16, 185, 129);
    private static final Color COLOR_ACCENT_SECONDARY_HOVER = new Color(5, 150, 105);
    private static final Color COLOR_BORDER_INPUT = new Color(203, 213, 225);
    private static final Color COLOR_ERROR = new Color(239, 68, 68);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_TABLE_GRID = new Color(226, 232, 240);


    // --- UI Fonts ---
    private static final Font FONT_TITLE_BIG = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font FONT_TITLE_MEDIUM = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_BUTTON_PRIMARY = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BUTTON_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 15);

    // --- Image Paths for Background Panels ---
    // USER: Ensure these images are accessible via the provided absolute paths or in your classpath root.
    private static final String LEFT_PANEL_IMAGE_PATH = "/images/left_panel_bg.png";
    private static final String RIGHT_PANEL_IMAGE_PATH = "D:\\New folder (5)\\New folder (15)\\sql\\image\\ChatGPT Image Jun 2, 2025, 10_08_49 PM.png";


    /**
     * Custom JPanel class to display a background image loaded from a file path or classpath resource.
     */
    private static class ImagePanel extends JPanel {
        private Image backgroundImage;
        private final Color fallbackBackgroundColor;

        public ImagePanel(String imagePath, Color fallbackColor) {
            this.fallbackBackgroundColor = fallbackColor;
            try {
                File f = new File(imagePath);
                if (f.isAbsolute() && f.exists() && !f.isDirectory()) {
                    this.backgroundImage = new ImageIcon(f.toURI().toURL()).getImage();
                    // System.out.println("Successfully loaded image from absolute file path: " + imagePath);
                } else {
                    URL imgUrl = getClass().getResource(imagePath); // Try as classpath resource
                    if (imgUrl != null) {
                        this.backgroundImage = new ImageIcon(imgUrl).getImage();
                         // System.out.println("Successfully loaded image from classpath resource: " + imagePath);
                    } else {
                        System.err.println("Warning: Image not found. Path: " + imagePath +
                                           ". Tried as absolute file path and classpath resource. Using fallback background.");
                        this.backgroundImage = null;
                    }
                }

                if (this.backgroundImage != null && this.backgroundImage.getWidth(null) == -1) {
                    System.err.println("Warning: ImageIcon could not properly load image from path: " + imagePath + ". Using fallback color.");
                    this.backgroundImage = null;
                }
            } catch (Exception e) {
                System.err.println("Error loading image from path: " + imagePath + " - " + e.getMessage());
                e.printStackTrace(); // Print stack trace for more details
                this.backgroundImage = null;
            }
            if (this.backgroundImage == null) {
                setBackground(this.fallbackBackgroundColor);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imageWidth = backgroundImage.getWidth(this);
                int imageHeight = backgroundImage.getHeight(this);

                if (imageWidth <= 0 || imageHeight <= 0) {
                     if (getBackground() != fallbackBackgroundColor) {
                         g2d.setColor(getBackground());
                     } else {
                         g2d.setColor(fallbackBackgroundColor);
                     }
                     g2d.fillRect(0, 0, panelWidth, panelHeight);
                     g2d.dispose();
                     return;
                }

                double panelAspect = (double) panelWidth / panelHeight;
                double imageAspect = (double) imageWidth / imageHeight;
                int newImgWidth, newImgHeight, xOff = 0, yOff = 0;

                // Cover strategy: fill panel, maintain aspect ratio, center image
                if (imageAspect > panelAspect) { // Image is wider than panel aspect ratio
                    newImgHeight = panelHeight;
                    newImgWidth = (int) (imageAspect * newImgHeight);
                    xOff = (panelWidth - newImgWidth) / 2; // Center horizontally
                } else { // Image is taller than panel aspect ratio (or same)
                    newImgWidth = panelWidth;
                    newImgHeight = (int) (newImgWidth / imageAspect);
                    yOff = (panelHeight - newImgHeight) / 2; // Center vertically
                }
                g2d.drawImage(backgroundImage, xOff, yOff, newImgWidth, newImgHeight, this);
                g2d.dispose();
            } else if (getBackground() != fallbackBackgroundColor) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setColor(fallbackBackgroundColor); // Ensure fallback is used if image is null
                 g2d.fillRect(0,0,getWidth(),getHeight());
                 g2d.dispose();
            }
        }
    }


    public LibraryManagementGUI() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BACKGROUND_PRIMARY);

        createHomePanel();
        createStudentLoginPanel();
        createStudentRegisterPanel();
        createStudentDashboardPanel();
        createLibrarianLoginPanel();
        createLibrarianDashboardPanel();
        createBookSearchPanel();
        createMyBooksPanel();

        mainPanel.add(homePanel, "Home");
        mainPanel.add(studentLoginPanel, "StudentLogin");
        mainPanel.add(studentRegisterPanel, "StudentRegister");
        mainPanel.add(studentDashboardPanel, "StudentDashboard");
        mainPanel.add(librarianLoginPanel, "LibrarianLogin");
        mainPanel.add(librarianDashboardPanel, "LibrarianDashboard");
        mainPanel.add(bookSearchPanel, "BookSearch");
        mainPanel.add(myBooksPanel, "MyBooks");

        add(mainPanel);
        cardLayout.show(mainPanel, "Home");
        setVisible(true);
    }

    // --- Styling Helper Methods ---
    private void stylePrimaryButton(JButton button, Color baseColor, Color hoverColor) {
        button.setFont(FONT_BUTTON_PRIMARY);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent evt) { button.setBackground(baseColor); }
        });
    }

    private void styleSecondaryButton(JButton button, boolean isLinkStyle) {
        button.setFont(FONT_BUTTON_SECONDARY);
        button.setForeground(COLOR_ACCENT_PRIMARY);
        button.setBackground(COLOR_BACKGROUND_SECONDARY);
        button.setFocusPainted(false);
        if (isLinkStyle) {
            button.setBorder(new EmptyBorder(5,0,5,0));
            button.setContentAreaFilled(false);
            button.setOpaque(false);
        } else {
             button.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT_PRIMARY, 1));
             button.setOpaque(true);
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 30));
    }

    private void styleTextField(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, COLOR_BORDER_INPUT),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setOpaque(true);
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(FONT_INPUT);
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, COLOR_BORDER_INPUT),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setOpaque(true);
    }

    private void styleLabel(JLabel label, boolean isTitle) {
        label.setFont(isTitle ? FONT_TITLE_MEDIUM : FONT_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);
        label.setOpaque(false);
    }
    
    private void stylePasswordShowCheckbox(JCheckBox checkbox) {
        checkbox.setFont(FONT_LABEL);
        checkbox.setForeground(COLOR_TEXT_ON_IMAGE);
        checkbox.setOpaque(false);
        checkbox.setFocusPainted(false);
    }

    private void styleLabelForImageBg(JLabel label, boolean isTitleFont, boolean withBackground) {
        label.setFont(isTitleFont ? FONT_TITLE_MEDIUM : FONT_LABEL);
        label.setForeground(COLOR_TEXT_ON_IMAGE);
        if (withBackground) {
            label.setOpaque(true);
            label.setBackground(COLOR_LABEL_BG_ON_IMAGE);
            label.setBorder(new EmptyBorder(3, 7, 3, 7));
        } else {
            label.setOpaque(false);
        }
    }


    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FONT_INPUT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(COLOR_TEXT_PRIMARY);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(COLOR_ACCENT_PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(COLOR_TEXT_PRIMARY);
                }
                return this;
            }
        });
    }

    private void applyDigitLimitFilter(JTextField textField, int limit) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (string.matches("\\d+") && (fb.getDocument().getLength() + string.length()) <= limit) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if (text.matches("\\d*") && (fb.getDocument().getLength() - length + text.length()) <= limit) { 
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }


    private JPanel createDecorativeLeftPanel(String title, String subtitle) {
        ImagePanel leftPanel = new ImagePanel(LEFT_PANEL_IMAGE_PATH, COLOR_LEFT_PANE_DECORATIVE);
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 25, 15, 25);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel mainTitleLabel = new JLabel(title);
        mainTitleLabel.setFont(FONT_TITLE_BIG);
        mainTitleLabel.setForeground(COLOR_TEXT_ON_IMAGE);
        mainTitleLabel.setOpaque(true);
        mainTitleLabel.setBackground(new Color(0, 0, 0, 120));
        mainTitleLabel.setBorder(new EmptyBorder(5,10,5,10));
        mainTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(mainTitleLabel, gbc);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(new Color(230, 230, 230));
        subtitleLabel.setOpaque(true);
        subtitleLabel.setBackground(new Color(0, 0, 0, 100));
        subtitleLabel.setBorder(new EmptyBorder(3,8,3,8));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(subtitleLabel, gbc);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(255, 255, 255, 100));
        sep.setBackground(new Color(255, 255, 255, 50));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(25, 25, 25, 25);
        leftPanel.add(sep, gbc);

        JLabel additionalInfo = new JLabel("<html><center>Your Gateway to<br>Knowledge and Adventure.</center></html>");
        additionalInfo.setFont(FONT_LABEL);
        additionalInfo.setForeground(new Color(220, 220, 220));
        additionalInfo.setOpaque(true);
        additionalInfo.setBackground(new Color(0,0,0,90));
        additionalInfo.setBorder(new EmptyBorder(5,10,5,10));
        additionalInfo.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(additionalInfo, gbc);

        return leftPanel;
    }

    private void createHomePanel() {
        homePanel = new ImagePanel(LEFT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_PRIMARY); // Or any other default if needed
        homePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(FONT_TITLE_BIG);
        titleLabel.setForeground(COLOR_TEXT_ON_IMAGE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(COLOR_LABEL_BG_ON_IMAGE);
        titleLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        gbc.insets = new Insets(30, 10, 40, 10);
        homePanel.add(titleLabel, gbc);
        gbc.insets = new Insets(15, 15, 15, 15);

        JButton studentPortalButton = new JButton("Student Portal");
        stylePrimaryButton(studentPortalButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        studentPortalButton.addActionListener(e -> cardLayout.show(mainPanel, "StudentLogin"));
        homePanel.add(studentPortalButton, gbc);

        JButton librarianPortalButton = new JButton("Librarian Portal");
        stylePrimaryButton(librarianPortalButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
        librarianPortalButton.addActionListener(e -> cardLayout.show(mainPanel, "LibrarianLogin"));
        homePanel.add(librarianPortalButton, gbc);

        JButton exitButton = new JButton("Exit Application");
        stylePrimaryButton(exitButton, COLOR_TEXT_SECONDARY, COLOR_TEXT_SECONDARY.darker());
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        gbc.insets = new Insets(30, 15, 15, 15);
        homePanel.add(exitButton, gbc);
    }

    private void createStudentLoginPanel() {
        studentLoginPanel = new JPanel(new BorderLayout());
        studentLoginPanel.add(createDecorativeLeftPanel("Student Portal", "Access Your Library Account"), BorderLayout.WEST);

        ImagePanel formPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Student Login");
        styleLabelForImageBg(titleLabel, true, true);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        styleLabelForImageBg(usernameLabel, false, true);
        gbc.insets = new Insets(15, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        styleLabelForImageBg(passwordLabel, false, true);
        gbc.insets = new Insets(10, 0, 2, 0);
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        stylePasswordField(passwordField);

        JCheckBox showPasswordCheck = new JCheckBox("Show");
        stylePasswordShowCheckbox(showPasswordCheck);
        final char defaultEchoChar = passwordField.getEchoChar();
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : defaultEchoChar);
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordCheck, BorderLayout.EAST);
        gbc.insets = new Insets(0, 0, 25, 0);
        formPanel.add(passwordPanel, gbc);


        JButton loginButton = new JButton("Login");
        stylePrimaryButton(loginButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(loginButton, gbc);

        JPanel linkButtonsPanel = new JPanel(new GridBagLayout());
        linkButtonsPanel.setOpaque(false);
        GridBagConstraints linkGbc = new GridBagConstraints();
        linkGbc.gridwidth = GridBagConstraints.REMAINDER;
        linkGbc.anchor = GridBagConstraints.CENTER;
        linkGbc.insets = new Insets(2, 0, 2, 0);

        JButton registerButton = new JButton("New User? Register");
        styleSecondaryButton(registerButton, true);
        linkButtonsPanel.add(registerButton, linkGbc);

        JButton forgotPasswordButton = new JButton("Forgot Password?");
        styleSecondaryButton(forgotPasswordButton, true);
        forgotPasswordButton.setForeground(COLOR_ERROR);
        linkButtonsPanel.add(forgotPasswordButton, linkGbc);

        JButton backButton = new JButton("Back to Home");
        styleSecondaryButton(backButton, true);
        linkButtonsPanel.add(backButton, linkGbc);

        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(linkButtonsPanel, gbc);

        studentLoginPanel.add(formPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            User user = library.authenticateUser(username, password);
            if (user != null) {
                currentStudent = user;
                String loginSuccessMsg = "Login successful. Welcome " + user.getFullName() + "!";

                if (!currentStudent.hasPaidFees()) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "A one-time registration fee of ₹" + library.getRegistrationFeeAmount() + " is pending.\nDo you want to pay now?",
                            "Registration Fee", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        String feeResult = library.collectFees(currentStudent);
                        JOptionPane.showMessageDialog(this, loginSuccessMsg + "\n" + feeResult, "Login & Fee Status", JOptionPane.INFORMATION_MESSAGE);
                        if (!currentStudent.hasPaidFees()) {
                            JOptionPane.showMessageDialog(this, "Fee payment is required to proceed. Please log in again to pay.", "Fee Required", JOptionPane.WARNING_MESSAGE);
                            currentStudent = null; cardLayout.show(mainPanel, "StudentLogin"); return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, loginSuccessMsg + "\nYou must pay the fee to borrow books. You can log in again later to pay.", "Login Success, Fee Pending", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                     JOptionPane.showMessageDialog(this, loginSuccessMsg, "Login Success", JOptionPane.INFORMATION_MESSAGE);
                }
                updateStudentDashboardPanel(); cardLayout.show(mainPanel, "StudentDashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            usernameField.setText(""); passwordField.setText("");
        });
        registerButton.addActionListener(e -> { usernameField.setText(""); passwordField.setText(""); cardLayout.show(mainPanel, "StudentRegister"); });
        forgotPasswordButton.addActionListener(e -> showStudentForgotPasswordDialog());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
    }

    private void createStudentRegisterPanel() {
        studentRegisterPanel = new JPanel(new BorderLayout());
        studentRegisterPanel.add(createDecorativeLeftPanel("New Student", "Join Our Library Community"), BorderLayout.WEST);

        ImagePanel formPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel titleLabel = new JLabel("Student Registration");
        styleLabelForImageBg(titleLabel, true, true);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 25, 0);
        formPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 8, 8, 8);


        String[] labels = {"Username:", "Password:", "Full Name:", "Gender:", "Aadhaar (12 digits):", "Phone (10 digits):", "Address:"};
        Component[] inputComponents = new Component[labels.length];
        JPasswordField passwordField = new JPasswordField(18); // Defined explicitly for the toggle
        JTextField usernameField = new JTextField(18); // Defined explicitly for validation

        inputComponents[0] = usernameField;
        inputComponents[1] = passwordField;
        passwordField.setToolTipText("8-16 chars, 1 upper, 1 lower, 1 digit, 1 special");
        inputComponents[2] = new JTextField(18);
        inputComponents[3] = new JComboBox<>(new String[]{"Male", "Female", "Trans"});
        styleComboBox((JComboBox<?>)inputComponents[3]);
        inputComponents[4] = new JTextField(18);
        applyDigitLimitFilter((JTextField)inputComponents[4], 12);
        inputComponents[5] = new JTextField(18);
        applyDigitLimitFilter((JTextField)inputComponents[5], 10);
        inputComponents[6] = new JTextField(18);
        
        final Border defaultBorder = usernameField.getBorder();
        final Border errorBorder = BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 2, 0, COLOR_ERROR), new EmptyBorder(8, 10, 8, 10));
        final Border successBorder = BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 2, 0, COLOR_SUCCESS), new EmptyBorder(8, 10, 8, 10));
        
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String user = usernameField.getText().trim();
                if (!user.isEmpty()) {
                    if (library.findUserByUsername(user) != null) {
                        usernameField.setBorder(errorBorder);
                        JOptionPane.showMessageDialog(formPanel, "Username '" + user + "' is already taken. Please choose another.", "Username Unavailable", JOptionPane.WARNING_MESSAGE);
                    } else {
                        usernameField.setBorder(successBorder);
                    }
                } else {
                    usernameField.setBorder(defaultBorder);
                }
            }
             @Override
            public void focusGained(FocusEvent e) {
                 if (usernameField.getBorder() == errorBorder || usernameField.getBorder() == successBorder) {
                    usernameField.setBorder(defaultBorder);
                }
            }
        });


        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.anchor = GridBagConstraints.EAST;
            JLabel currentLabel = new JLabel(labels[i]);
            styleLabelForImageBg(currentLabel, false, true);
            formPanel.add(currentLabel, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            Component inputComponent = inputComponents[i];
            
            if (inputComponent instanceof JTextField) styleTextField((JTextField)inputComponent);
            else if (inputComponent instanceof JPasswordField) stylePasswordField((JPasswordField)inputComponent);
            
            if(inputComponent == passwordField){
                JCheckBox showPassCheck = new JCheckBox("Show");
                stylePasswordShowCheckbox(showPassCheck);
                final char echo = passwordField.getEchoChar();
                showPassCheck.addActionListener(evt -> passwordField.setEchoChar(showPassCheck.isSelected() ? (char)0 : echo));
                
                JPanel passWrapper = new JPanel(new BorderLayout(5, 0));
                passWrapper.setOpaque(false);
                passWrapper.add(passwordField, BorderLayout.CENTER);
                passWrapper.add(showPassCheck, BorderLayout.EAST);
                formPanel.add(passWrapper, gbc);
            } else {
                formPanel.add(inputComponent, gbc);
            }
        }

        JButton registerButton = new JButton("Register");
        stylePrimaryButton(registerButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 0, 10, 0);
        formPanel.add(registerButton, gbc);

        JButton backButton = new JButton("Back to Login");
        styleSecondaryButton(backButton, true);
        gbc.gridy = labels.length + 2;
        gbc.insets = new Insets(5, 0, 0, 0);
        formPanel.add(backButton, gbc);

        studentRegisterPanel.add(formPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String fullName = ((JTextField)inputComponents[2]).getText();
            String gender = (String) ((JComboBox<?>)inputComponents[3]).getSelectedItem();
            String aadhaar = ((JTextField)inputComponents[4]).getText();
            String phone = ((JTextField)inputComponents[5]).getText();
            String address = ((JTextField)inputComponents[6]).getText();

            String result = library.registerUser(username, password, fullName, gender, aadhaar, phone, address);
            if (result.startsWith("(!) Password validation failed:")) {
                 JTextArea textArea = new JTextArea(result);
                 textArea.setEditable(false); textArea.setWrapStyleWord(true); textArea.setLineWrap(true);
                 textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                 JScrollPane scrollPane = new JScrollPane(textArea);
                 scrollPane.setPreferredSize(new Dimension(350, 120));
                 JOptionPane.showMessageDialog(this, scrollPane, "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result, "Registration Status", result.startsWith("(+)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }

            if (result.startsWith("(+)")) {
                for(Component comp : inputComponents) {
                    if (comp instanceof JTextField) ((JTextField)comp).setText("");
                    else if (comp instanceof JComboBox) ((JComboBox<?>)comp).setSelectedIndex(0);
                }
                passwordField.setText(""); // clear password field
                usernameField.setBorder(defaultBorder); // Reset border after successful registration
                cardLayout.show(mainPanel, "StudentLogin");
            }
        });
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "StudentLogin"));
    }

    private void createLibrarianLoginPanel() {
        librarianLoginPanel = new JPanel(new BorderLayout());
        librarianLoginPanel.add(createDecorativeLeftPanel("Librarian Portal", "Manage Library Resources"), BorderLayout.WEST);

        ImagePanel formPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Librarian Login");
        styleLabelForImageBg(titleLabel, true, true);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        styleLabelForImageBg(usernameLabel, false, true);
        gbc.insets = new Insets(15, 0, 2, 0); gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        styleLabelForImageBg(passwordLabel, false, true);
        gbc.insets = new Insets(10, 0, 2, 0);
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        stylePasswordField(passwordField);

        JCheckBox showPasswordCheck = new JCheckBox("Show");
        stylePasswordShowCheckbox(showPasswordCheck);
        final char defaultEchoChar = passwordField.getEchoChar();
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : defaultEchoChar);
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordCheck, BorderLayout.EAST);
        gbc.insets = new Insets(0, 0, 25, 0);
        formPanel.add(passwordPanel, gbc);

        JButton loginButton = new JButton("Login");
        stylePrimaryButton(loginButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(loginButton, gbc);

        JPanel linkButtonsPanel = new JPanel(new GridBagLayout());
        linkButtonsPanel.setOpaque(false);
        GridBagConstraints linkGbc = new GridBagConstraints();
        linkGbc.gridwidth = GridBagConstraints.REMAINDER;
        linkGbc.anchor = GridBagConstraints.CENTER;
        linkGbc.insets = new Insets(2, 0, 2, 0);

        JButton forgotPasswordButtonLibrarian = new JButton("Forgot Credentials?");
        styleSecondaryButton(forgotPasswordButtonLibrarian, true);
        forgotPasswordButtonLibrarian.setForeground(COLOR_ERROR);
        linkButtonsPanel.add(forgotPasswordButtonLibrarian, linkGbc);

        JButton backButton = new JButton("Back to Home");
        styleSecondaryButton(backButton, true);
        linkButtonsPanel.add(backButton, linkGbc);

        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(linkButtonsPanel, gbc);


        librarianLoginPanel.add(formPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (library.authenticateLibrarian(username, password)) {
                JOptionPane.showMessageDialog(this, "Librarian login successful.", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                updateLibrarianDashboardPanel();
                cardLayout.show(mainPanel, "LibrarianDashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid librarian credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            usernameField.setText(""); passwordField.setText("");
        });
        forgotPasswordButtonLibrarian.addActionListener(e -> showLibrarianForgotPasswordDialog());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
    }

    private void createStudentDashboardPanel() {
        studentDashboardPanel = new JPanel(new BorderLayout());
    }

    private void updateStudentDashboardPanel() {
        studentDashboardPanel.removeAll();
        if (currentStudent == null) return;
        studentDashboardPanel.setLayout(new BorderLayout());
        studentDashboardPanel.add(createDecorativeLeftPanel("Student Dashboard", "Your library activity hub"), BorderLayout.WEST);

        ImagePanel contentPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel welcomeLabel = new JLabel("Welcome, " + currentStudent.getFullName() + "!");
        styleLabelForImageBg(welcomeLabel, true, true);
        gbc.insets = new Insets(20,10,20,10);
        contentPanel.add(welcomeLabel, gbc);
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension dashboardButtonSize = new Dimension(280, 45);

        JButton searchBooksButton = new JButton("Search & View Books");
        stylePrimaryButton(searchBooksButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        searchBooksButton.setPreferredSize(dashboardButtonSize);
        searchBooksButton.addActionListener(e -> { updateBookSearchPanel("Student"); cardLayout.show(mainPanel, "BookSearch"); });
        contentPanel.add(searchBooksButton, gbc);

        JButton borrowBookButton = new JButton("Borrow a Book");
        stylePrimaryButton(borrowBookButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        borrowBookButton.setPreferredSize(dashboardButtonSize);
        borrowBookButton.addActionListener(e -> {
            String bookTitle = JOptionPane.showInputDialog(this, "Enter the exact title of the book to borrow:");
            if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                String result = library.borrowBook(currentStudent, bookTitle.trim());
                JTextArea textArea = new JTextArea(result);
                textArea.setWrapStyleWord(true); textArea.setLineWrap(true); textArea.setEditable(false);
                textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                JScrollPane scrollPane = new JScrollPane(textArea); scrollPane.setPreferredSize(new Dimension(350, 100));
                JOptionPane.showMessageDialog(this, scrollPane, "Borrow Status", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        contentPanel.add(borrowBookButton, gbc);

        JButton returnBookButton = new JButton("Return a Book");
        stylePrimaryButton(returnBookButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        returnBookButton.setPreferredSize(dashboardButtonSize);
        returnBookButton.addActionListener(e -> {
            String bookTitle = JOptionPane.showInputDialog(this, "Enter the exact title of the book to return:");
            if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                String result = library.returnBook(currentStudent, bookTitle.trim());
                JOptionPane.showMessageDialog(this, result);
                updateMyBooksPanel(); // Refresh borrowed books list after returning
            }
        });
        contentPanel.add(returnBookButton, gbc);

        JButton viewMyBooksButton = new JButton("View My Borrowed Books");
        stylePrimaryButton(viewMyBooksButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        viewMyBooksButton.setPreferredSize(dashboardButtonSize);
        viewMyBooksButton.addActionListener(e -> { updateMyBooksPanel(); cardLayout.show(mainPanel, "MyBooks"); });
        contentPanel.add(viewMyBooksButton, gbc);

        if (!currentStudent.hasPaidFees()) {
            JButton payFeesButton = new JButton("Pay Registration Fee (₹" + library.getRegistrationFeeAmount() + ")");
            stylePrimaryButton(payFeesButton, COLOR_ERROR, COLOR_ERROR.brighter());
            payFeesButton.setPreferredSize(dashboardButtonSize);
            payFeesButton.addActionListener(e -> {
                 int choice = JOptionPane.showConfirmDialog(this,
                            "Pay the one-time registration fee of ₹" + library.getRegistrationFeeAmount() + "?",
                            "Confirm Payment", JOptionPane.YES_NO_OPTION);
                 if (choice == JOptionPane.YES_OPTION) {
                    String feeResult = library.collectFees(currentStudent);
                    JOptionPane.showMessageDialog(this, feeResult);
                    updateStudentDashboardPanel();
                 }
            });
            contentPanel.add(payFeesButton, gbc);
        }

        JButton logoutButton = new JButton("Logout");
        styleSecondaryButton(logoutButton, false);
        logoutButton.setPreferredSize(dashboardButtonSize);
        logoutButton.setForeground(COLOR_TEXT_SECONDARY);
        logoutButton.setBorder(BorderFactory.createLineBorder(COLOR_TEXT_SECONDARY, 1));
        logoutButton.addActionListener(e -> {
            currentStudent = null;
            JOptionPane.showMessageDialog(this, "Logged out successfully.");
            cardLayout.show(mainPanel, "StudentLogin");
        });
        gbc.insets = new Insets(20,10,10,10);
        contentPanel.add(logoutButton, gbc);

        studentDashboardPanel.add(contentPanel, BorderLayout.CENTER);
        studentDashboardPanel.revalidate();
        studentDashboardPanel.repaint();
    }

    private void createLibrarianDashboardPanel() {
        librarianDashboardPanel = new JPanel(new BorderLayout());
    }
    private void updateLibrarianDashboardPanel() {
        librarianDashboardPanel.removeAll();
        librarianDashboardPanel.setLayout(new BorderLayout());
        librarianDashboardPanel.add(createDecorativeLeftPanel("Librarian Control", "Manage library operations"), BorderLayout.WEST);

        ImagePanel contentPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,10,5,10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Librarian Dashboard");
        styleLabelForImageBg(titleLabel, true, true);
        gbc.insets = new Insets(10,10,10,10);
        contentPanel.add(titleLabel, gbc);
        gbc.insets = new Insets(5,10,5,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension dashboardButtonSize = new Dimension(320, 38);

        JButton addBookButton = new JButton("Add/Edit Book");
        stylePrimaryButton(addBookButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
        addBookButton.setPreferredSize(dashboardButtonSize);
        addBookButton.addActionListener(e -> { updateBookSearchPanel("Librarian"); cardLayout.show(mainPanel, "BookSearch"); });
        contentPanel.add(addBookButton, gbc);

        JButton removeBookButton = new JButton("Remove Book");
        stylePrimaryButton(removeBookButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
        removeBookButton.setPreferredSize(dashboardButtonSize);
        removeBookButton.addActionListener(e -> {
            String titleToRemove = JOptionPane.showInputDialog(this, "Enter title of the book to remove:");
            if (titleToRemove != null && !titleToRemove.trim().isEmpty()) {
                String result = library.removeBook(titleToRemove.trim());
                JOptionPane.showMessageDialog(this, result);
            }
        });
        contentPanel.add(removeBookButton, gbc);
        
        JButton changeStudentCredentialsButton = new JButton("Change Student Credentials");
        stylePrimaryButton(changeStudentCredentialsButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        changeStudentCredentialsButton.setPreferredSize(dashboardButtonSize);
        changeStudentCredentialsButton.addActionListener(e -> showChangeStudentCredentialsDialog());
        contentPanel.add(changeStudentCredentialsButton, gbc);

        JButton deleteStudentButton = new JButton("Delete Student Account");
        stylePrimaryButton(deleteStudentButton, COLOR_ERROR, new Color(200, 50, 50));
        deleteStudentButton.setPreferredSize(dashboardButtonSize);
        deleteStudentButton.addActionListener(e -> showDeleteStudentDialog());
        contentPanel.add(deleteStudentButton, gbc);

        JButton findLoanByIdButton = new JButton("Find Loan by ID");
        stylePrimaryButton(findLoanByIdButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        findLoanByIdButton.setPreferredSize(dashboardButtonSize);
        findLoanByIdButton.addActionListener(e -> {
            String loanId = JOptionPane.showInputDialog(this, "Enter Borrowing ID (e.g., TXN123456):");
            if (loanId != null && !loanId.trim().isEmpty()) {
                String details = library.getBorrowingDetailsById(loanId.trim());
                JTextArea textArea = new JTextArea(details);
                textArea.setWrapStyleWord(true); textArea.setLineWrap(true); textArea.setEditable(false);
                textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                JScrollPane scrollPane = new JScrollPane(textArea); scrollPane.setPreferredSize(new Dimension(400, 150));
                JOptionPane.showMessageDialog(this, scrollPane, "Borrowing Details", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        contentPanel.add(findLoanByIdButton, gbc);

        JButton listAllUsersButton = new JButton("List All Users");
        stylePrimaryButton(listAllUsersButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        listAllUsersButton.setPreferredSize(dashboardButtonSize);
        listAllUsersButton.addActionListener(e -> showListAllUsersDialog());
        contentPanel.add(listAllUsersButton, gbc);

        JButton viewStudentActivityButton = new JButton("View Student Activity Log");
        stylePrimaryButton(viewStudentActivityButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        viewStudentActivityButton.setPreferredSize(dashboardButtonSize);
        viewStudentActivityButton.addActionListener(e -> showStudentActivityLogDialog());
        contentPanel.add(viewStudentActivityButton, gbc);


        JButton viewIncomeButton = new JButton("View Library Income");
        stylePrimaryButton(viewIncomeButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        viewIncomeButton.setPreferredSize(dashboardButtonSize);
        viewIncomeButton.addActionListener(e -> {
             JOptionPane.showMessageDialog(this, "Total Library Income: ₹" + library.getIncome(), "Library Income", JOptionPane.INFORMATION_MESSAGE);
        });
        contentPanel.add(viewIncomeButton, gbc);
        
        JButton changeMyCredentialsButton = new JButton("Change My Credentials");
        stylePrimaryButton(changeMyCredentialsButton, COLOR_ERROR, new Color(200,50,50));
        changeMyCredentialsButton.setPreferredSize(dashboardButtonSize);
        changeMyCredentialsButton.addActionListener(e -> showChangeLibrarianCredentialsDialog());
        contentPanel.add(changeMyCredentialsButton, gbc);


        JButton logoutButton = new JButton("Logout");
        styleSecondaryButton(logoutButton, false);
        logoutButton.setPreferredSize(dashboardButtonSize);
        logoutButton.setForeground(COLOR_TEXT_SECONDARY);
        logoutButton.setBorder(BorderFactory.createLineBorder(COLOR_TEXT_SECONDARY, 1));
        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Librarian logged out.");
            cardLayout.show(mainPanel, "LibrarianLogin");
        });
        gbc.insets = new Insets(10,10,5,10);
        contentPanel.add(logoutButton, gbc);

        librarianDashboardPanel.add(contentPanel, BorderLayout.CENTER);
        librarianDashboardPanel.revalidate();
        librarianDashboardPanel.repaint();
    }

    private void createBookSearchPanel() {
        bookSearchPanel = new JPanel(new BorderLayout());
    }

    private void updateBookSearchPanel(String userType) {
        bookSearchPanel.removeAll();
        bookSearchPanel.setLayout(new BorderLayout());
        bookSearchPanel.add(createDecorativeLeftPanel("Book Catalog", "Find your next read"), BorderLayout.WEST);

        ImagePanel mainContentWrapperPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        mainContentWrapperPanel.setLayout(new BorderLayout(10,10));
        mainContentWrapperPanel.setBorder(new EmptyBorder(10,15,10,15));

        JLabel titleLabel = new JLabel(userType.equals("Librarian") ? "Manage Books" : "Search & View Books");
        styleLabelForImageBg(titleLabel, true, true);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10,0,15,0));
        mainContentWrapperPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Title", "Author", "Category", "Subject", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable bookTable = new JTable(tableModel);
        bookTable.setBackground(Color.WHITE);
        bookTable.setForeground(COLOR_TEXT_PRIMARY);
        bookTable.setGridColor(COLOR_TABLE_GRID);
        bookTable.getTableHeader().setBackground(COLOR_LEFT_PANE_DECORATIVE);
        bookTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
        bookTable.getTableHeader().setFont(FONT_BUTTON_PRIMARY);
        bookTable.setFont(FONT_LABEL);
        bookTable.setRowHeight(28);
        bookTable.setFillsViewportHeight(true);
         bookTable.setOpaque(true);

        bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int selectedRow = bookTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String bookTitle = (String) bookTable.getValueAt(bookTable.convertRowIndexToModel(selectedRow), 0);
                        Book selectedBook = library.searchBook(bookTitle);
                        if (selectedBook != null) {
                            showBookDetailsDialog(selectedBook);
                        }
                    }
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);


        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(true);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT));
        mainContentWrapperPanel.add(scrollPane, BorderLayout.CENTER);

        List<Book> allBooks = library.getAllBooks();
        for (Book book : allBooks) {
            Object[] rowData = {
                book.getTitle(), book.getAuthor(), book.getCategory(), book.getSubject(),
                book.isAvailable() ? "Available" : "Borrowed"
            };
            tableModel.addRow(rowData);
        }

        JPanel controlsPanelOuter = new JPanel(new BorderLayout(0,10));
        controlsPanelOuter.setOpaque(false);
        controlsPanelOuter.setBorder(new EmptyBorder(15,0,0,0));
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter by:"); styleLabelForImageBg(filterLabel, false, true);
        controlsPanel.add(filterLabel);
        JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"All", "Available", "Borrowed", "Category", "Subject"});
        styleComboBox(filterComboBox);
        controlsPanel.add(filterComboBox);

        JLabel valueLabel = new JLabel("Value:"); styleLabelForImageBg(valueLabel, false, true);
        controlsPanel.add(valueLabel);
        JTextField filterValueField = new JTextField(15); styleTextField(filterValueField);
        filterValueField.setEnabled(false);
        controlsPanel.add(filterValueField);

        filterComboBox.addActionListener(e -> filterValueField.setEnabled(((String)filterComboBox.getSelectedItem()).matches("Category|Subject")));

        JButton searchButton = new JButton("Search");
        stylePrimaryButton(searchButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        controlsPanel.add(searchButton);
        controlsPanelOuter.add(controlsPanel, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String filter = (String) filterComboBox.getSelectedItem();
            String value = filterValueField.getText().trim();
             if ((filter.equals("Category") || filter.equals("Subject")) && value.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a value for Category/Subject search.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<Book> books = library.getFilteredBooks(filter, value);
            tableModel.setRowCount(0);
            if (books.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No books found matching your criteria.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Book bookItem : books) {
                    Object[] rowData = {
                            bookItem.getTitle(), bookItem.getAuthor(), bookItem.getCategory(), bookItem.getSubject(),
                            bookItem.isAvailable() ? "Available" : "Borrowed"
                    };
                    tableModel.addRow(rowData);
                }
            }
        });

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomButtonPanel.setOpaque(false);

        if (userType.equals("Librarian")) {
            JButton addButton = new JButton("Add New Book");
            stylePrimaryButton(addButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
            bottomButtonPanel.add(addButton);

            JButton editButton = new JButton("Edit Selected");
            stylePrimaryButton(editButton, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_HOVER);
            bottomButtonPanel.add(editButton);
             addButton.addActionListener(e -> {
                showBookFormDialog(null); 
                searchButton.doClick(); 
            });
            editButton.addActionListener(e -> {
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String bookTitle = (String) bookTable.getValueAt(bookTable.convertRowIndexToModel(selectedRow), 0);
                    Book bookToEdit = library.searchBook(bookTitle);
                    if (bookToEdit != null) {
                        showBookFormDialog(bookToEdit); 
                        searchButton.doClick(); 
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a book from the table to edit.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        JPanel backButtonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButtonContainer.setOpaque(false);
        JButton backButton = new JButton("Back");
        styleSecondaryButton(backButton, false);
        backButton.setForeground(COLOR_TEXT_SECONDARY);
        backButton.setBorder(BorderFactory.createLineBorder(COLOR_TEXT_SECONDARY));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, userType.equals("Student") ? "StudentDashboard" : "LibrarianDashboard"));
        backButtonContainer.add(backButton);

        controlsPanelOuter.add(bottomButtonPanel, BorderLayout.WEST);
        controlsPanelOuter.add(backButtonContainer, BorderLayout.EAST);

        mainContentWrapperPanel.add(controlsPanelOuter, BorderLayout.SOUTH);
        bookSearchPanel.add(mainContentWrapperPanel, BorderLayout.CENTER);
        bookSearchPanel.revalidate(); bookSearchPanel.repaint();
    }

    private void createMyBooksPanel() {
        myBooksPanel = new JPanel(new BorderLayout());
    }
    private void updateMyBooksPanel() {
        myBooksPanel.removeAll();
        myBooksPanel.setLayout(new BorderLayout());
        if (currentStudent == null) return;
        myBooksPanel.add(createDecorativeLeftPanel("My Books", "Track your borrowed items"), BorderLayout.WEST);

        ImagePanel mainContentWrapperPanel = new ImagePanel(RIGHT_PANEL_IMAGE_PATH, COLOR_BACKGROUND_SECONDARY);
        mainContentWrapperPanel.setLayout(new BorderLayout(10,10));
        mainContentWrapperPanel.setBorder(new EmptyBorder(10,15,10,15));

        JLabel titleLabel = new JLabel("My Borrowed Books");
        styleLabelForImageBg(titleLabel, true, true);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10,0,15,0));
        mainContentWrapperPanel.add(titleLabel, BorderLayout.NORTH);

        List<Book> borrowed = currentStudent.getBorrowedBooks();
        if (borrowed.isEmpty()) {
            JLabel noBooksLabel = new JLabel("You have no borrowed books.", JLabel.CENTER);
            styleLabelForImageBg(noBooksLabel, false, true);
            noBooksLabel.setFont(FONT_SUBTITLE);
            mainContentWrapperPanel.add(noBooksLabel, BorderLayout.CENTER);
        } else {
            String[] columnNames = {"Title", "Author", "Borrowed Date", "Return By", "Borrowing ID"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                 @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable booksTable = new JTable(tableModel);
            booksTable.setBackground(Color.WHITE);
            booksTable.setForeground(COLOR_TEXT_PRIMARY);
            booksTable.setGridColor(COLOR_TABLE_GRID);
            booksTable.getTableHeader().setBackground(COLOR_LEFT_PANE_DECORATIVE);
            booksTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
            booksTable.getTableHeader().setFont(FONT_BUTTON_PRIMARY);
            booksTable.setFont(FONT_LABEL);
            booksTable.setRowHeight(28);
            booksTable.setFillsViewportHeight(true);
            booksTable.setOpaque(true);
            booksTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) { 
                        int selectedRow = booksTable.getSelectedRow();
                        if (selectedRow >= 0) {
                            String bookTitle = (String) booksTable.getValueAt(booksTable.convertRowIndexToModel(selectedRow), 0);
                            Book selectedBook = library.searchBook(bookTitle); 
                            if (selectedBook != null) {
                                showBookDetailsDialog(selectedBook);
                            }
                        }
                    }
                }
            });
            
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
            booksTable.setRowSorter(sorter);


            for (Book book : borrowed) {
                 LocalDate returnByDate = book.getBorrowedDate() != null ? book.getBorrowedDate().plusDays(Library.BORROWING_PERIOD_DAYS) : null;
                Object[] rowData = {
                        book.getTitle(), book.getAuthor(),
                        book.getBorrowedDate() != null ? book.getBorrowedDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A",
                        returnByDate != null ? returnByDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A",
                        book.getCurrentBorrowingId() != null ? book.getCurrentBorrowingId() : "N/A"
                };
                tableModel.addRow(rowData);
            }
            JScrollPane scrollPane = new JScrollPane(booksTable);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setOpaque(true);
            scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT));
            mainContentWrapperPanel.add(scrollPane, BorderLayout.CENTER);
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15,0,0,0));
        JButton backButton = new JButton("Back to Dashboard");
        styleSecondaryButton(backButton, false);
        backButton.setForeground(COLOR_TEXT_SECONDARY);
        backButton.setBorder(BorderFactory.createLineBorder(COLOR_TEXT_SECONDARY));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "StudentDashboard"));
        bottomPanel.add(backButton);
        mainContentWrapperPanel.add(bottomPanel, BorderLayout.SOUTH);

        myBooksPanel.add(mainContentWrapperPanel, BorderLayout.CENTER);
        myBooksPanel.revalidate(); myBooksPanel.repaint();
    }

    private void showBookFormDialog(Book bookToEdit) {
        boolean isEditMode = bookToEdit != null;
        JPanel formInnerPanel = new JPanel(new GridBagLayout());
        formInnerPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Title:", "Author:", "Category:", "Subject:",
                           "Location Row:", "Location Section:", "Location Block:", "Image:"};
        JTextField[] textFields = new JTextField[labels.length - 1]; 
        JLabel selectedImagePathLabel = new JLabel("No image selected");
        styleLabel(selectedImagePathLabel, false);
        selectedImagePathLabel.setPreferredSize(new Dimension(180, 25)); 
        selectedImagePathLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT), new EmptyBorder(5,5,5,5)));


        for(int i=0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.anchor = GridBagConstraints.EAST;
            JLabel currentLabel = new JLabel(labels[i]); styleLabel(currentLabel, false);
            formInnerPanel.add(currentLabel, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            if (i < labels.length - 1) { 
                textFields[i] = new JTextField(25); styleTextField(textFields[i]);
                if (i < 4) { textFields[i].setToolTipText("Mandatory field"); } 
                else { textFields[i].setToolTipText("Optional field"); }
                formInnerPanel.add(textFields[i], gbc);
            } else { 
                JPanel imageChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                imageChooserPanel.setOpaque(false);
                JButton chooseImageButton = new JButton("Choose...");
                styleSecondaryButton(chooseImageButton, false); 
                chooseImageButton.setPreferredSize(new Dimension(100, 28));

                imageChooserPanel.add(chooseImageButton);
                imageChooserPanel.add(selectedImagePathLabel); 
                formInnerPanel.add(imageChooserPanel, gbc);

                chooseImageButton.addActionListener(ae -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select Book Cover Image");
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
                    fileChooser.addChoosableFileFilter(filter);
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); 

                    int returnValue = fileChooser.showOpenDialog(this);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        selectedImagePathLabel.setText(selectedFile.getName()); 
                        selectedImagePathLabel.setToolTipText(selectedFile.getAbsolutePath()); 
                    }
                });
            }
        }

        if (isEditMode) {
            textFields[0].setText(bookToEdit.getTitle());
            textFields[1].setText(bookToEdit.getAuthor());
            textFields[2].setText(bookToEdit.getCategory());
            textFields[3].setText(bookToEdit.getSubject());
            textFields[4].setText(bookToEdit.getLocationRow());
            textFields[5].setText(bookToEdit.getLocationSection());
            textFields[6].setText(bookToEdit.getLocationBlock());
            if (bookToEdit.getImagePath() != null && !bookToEdit.getImagePath().isEmpty()) {
                String path = bookToEdit.getImagePath();
                if (path.startsWith("/") || !(new File(path).isAbsolute())) {
                     selectedImagePathLabel.setText(path.substring(path.lastIndexOf('/') + 1));
                } else {
                     File f = new File(path);
                     selectedImagePathLabel.setText(f.getName());
                }
                selectedImagePathLabel.setToolTipText(path);
            }
        }

        JScrollPane formScrollPane = new JScrollPane(formInnerPanel);
        formScrollPane.setBackground(COLOR_BACKGROUND_SECONDARY);
        formScrollPane.getViewport().setBackground(COLOR_BACKGROUND_SECONDARY);
        formScrollPane.setBorder(null); 
        formScrollPane.setPreferredSize(new Dimension(470, 380));


        JPanel dialogPanel = new JPanel(new BorderLayout(10,10)); 
        dialogPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
        dialogPanel.setBorder(new EmptyBorder(20,20,20,20)); 

        String dialogTitleText = isEditMode ? "Edit Book Details" : "Add New Book";
        JLabel dialogTitle = new JLabel(dialogTitleText);
        styleLabel(dialogTitle, true);
        dialogTitle.setHorizontalAlignment(SwingConstants.CENTER);
        dialogPanel.add(dialogTitle, BorderLayout.NORTH);
        dialogPanel.add(formScrollPane, BorderLayout.CENTER); 

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, dialogTitleText,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            String imagePath = selectedImagePathLabel.getToolTipText(); 
            if ("No image selected".equals(selectedImagePathLabel.getText()) || imagePath == null) {
                imagePath = ""; 
            }

            String outcomeMessage;
            if(isEditMode) {
                outcomeMessage = library.editBook(bookToEdit.getTitle(), 
                        textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(),
                        textFields[4].getText(), textFields[5].getText(), textFields[6].getText(), imagePath);
            } else {
                outcomeMessage = library.addBook(textFields[0].getText(), textFields[1].getText(),
                    textFields[2].getText(), textFields[3].getText(),
                    textFields[4].getText(), textFields[5].getText(), textFields[6].getText(), imagePath);
            }
            JOptionPane.showMessageDialog(this, outcomeMessage);
        }
    }

    private void showChangeLibrarianCredentialsDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Change Librarian Credentials");
        styleLabel(titleLabel, true);
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(0,0,20,0);
        panel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(8,8,8,8);

        String[] labels = {"Current Username:", "Current Password:", "New Username:", "New Password:", "Confirm New Password:", "Access Key:"};
        JTextField currentUsernameField = new JTextField(20); styleTextField(currentUsernameField);
        JPasswordField currentPasswordField = new JPasswordField(20); stylePasswordField(currentPasswordField);
        JTextField newUsernameField = new JTextField(20); styleTextField(newUsernameField);
        JPasswordField newPasswordField = new JPasswordField(20); stylePasswordField(newPasswordField);
        newPasswordField.setToolTipText("8-16 chars, 1 upper, 1 lower, 1 digit, 1 special");
        JPasswordField confirmNewPasswordField = new JPasswordField(20); stylePasswordField(confirmNewPasswordField);
        JPasswordField accessKeyField = new JPasswordField(20); stylePasswordField(accessKeyField);

        JPasswordField[] passwordFields = {currentPasswordField, newPasswordField, confirmNewPasswordField, accessKeyField};

        Component[] inputFields = {
            currentUsernameField, currentPasswordField, newUsernameField,
            newPasswordField, confirmNewPasswordField, accessKeyField
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.anchor = GridBagConstraints.EAST;
            JLabel currentLabel = new JLabel(labels[i]); styleLabel(currentLabel, false);
            panel.add(currentLabel, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            
            boolean isPasswordField = false;
            for(JPasswordField pf : passwordFields) {
                if (inputFields[i] == pf) {
                    isPasswordField = true;
                    break;
                }
            }

            if(isPasswordField) {
                JCheckBox showPassCheck = new JCheckBox("Show");
                stylePasswordShowCheckbox(showPassCheck);
                showPassCheck.setForeground(COLOR_TEXT_SECONDARY); // Darker color for better visibility on white bg
                final JPasswordField pf = (JPasswordField) inputFields[i];
                final char echo = pf.getEchoChar();
                showPassCheck.addActionListener(evt -> pf.setEchoChar(showPassCheck.isSelected() ? (char)0 : echo));
                
                JPanel passWrapper = new JPanel(new BorderLayout(5, 0));
                passWrapper.setOpaque(false);
                passWrapper.add(pf, BorderLayout.CENTER);
                passWrapper.add(showPassCheck, BorderLayout.EAST);
                panel.add(passWrapper, gbc);
            } else {
                 panel.add(inputFields[i], gbc);
            }
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Credentials",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String currentUsername = currentUsernameField.getText();
            String currentPassword = new String(currentPasswordField.getPassword());
            String newUsername = newUsernameField.getText();
            String newPass = new String(newPasswordField.getPassword());
            String confirmNewPass = new String(confirmNewPasswordField.getPassword());
            String accessKey = new String(accessKeyField.getPassword());

            if (newUsername.trim().isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "(!) New username and password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmNewPass)) {
                JOptionPane.showMessageDialog(this, "(!) New passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String outcome = library.updateLibrarianCredentials(currentUsername, currentPassword, newUsername, newPass, accessKey);
             if (outcome.startsWith("(!) New password validation failed:")) {
                 JTextArea textArea = new JTextArea(outcome);
                 textArea.setEditable(false); textArea.setWrapStyleWord(true); textArea.setLineWrap(true);
                 textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                 JScrollPane scrollPane = new JScrollPane(textArea);
                 scrollPane.setPreferredSize(new Dimension(350, 120));
                 JOptionPane.showMessageDialog(this, scrollPane, "Credential Update Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, outcome, "Credential Update Status",
                                          outcome.startsWith("(+)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }


            if (outcome.startsWith("(+)")) {
                 JOptionPane.showMessageDialog(this, "Please log out and log in with your new credentials.", "Action Required", JOptionPane.INFORMATION_MESSAGE);
                 cardLayout.show(mainPanel, "LibrarianLogin"); 
            }
        }
    }
    
    private void showChangeStudentCredentialsDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dialogTitleLabel = new JLabel("Change Student Credentials");
        styleLabel(dialogTitleLabel, true);
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(0,0,20,0);
        panel.add(dialogTitleLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(8,8,8,8);


        String[] labels = {"Current Student Username:", "New Student Username (optional):", "New Student Password:", "Confirm New Password:"};
        JTextField currentStudentUsernameField = new JTextField(20); styleTextField(currentStudentUsernameField);
        JTextField newStudentUsernameField = new JTextField(20); styleTextField(newStudentUsernameField);
        JPasswordField newStudentPasswordField = new JPasswordField(20); stylePasswordField(newStudentPasswordField);
        newStudentPasswordField.setToolTipText("8-16 chars, 1 upper, 1 lower, 1 digit, 1 special");
        JPasswordField confirmNewStudentPasswordField = new JPasswordField(20); stylePasswordField(confirmNewStudentPasswordField);

        Component[] inputFields = {
            currentStudentUsernameField, newStudentUsernameField,
            newStudentPasswordField, confirmNewStudentPasswordField
        };
        JPasswordField[] passwordFields = {newStudentPasswordField, confirmNewStudentPasswordField};


        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.anchor = GridBagConstraints.EAST;
            JLabel currentLabel = new JLabel(labels[i]); styleLabel(currentLabel, false);
            panel.add(currentLabel, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            boolean isPasswordField = false;
            for(JPasswordField pf : passwordFields) { if (inputFields[i] == pf) { isPasswordField = true; break; } }

            if(isPasswordField) {
                JCheckBox showPassCheck = new JCheckBox("Show");
                stylePasswordShowCheckbox(showPassCheck);
                showPassCheck.setForeground(COLOR_TEXT_SECONDARY);
                final JPasswordField pf = (JPasswordField) inputFields[i];
                final char echo = pf.getEchoChar();
                showPassCheck.addActionListener(evt -> pf.setEchoChar(showPassCheck.isSelected() ? (char)0 : echo));
                
                JPanel passWrapper = new JPanel(new BorderLayout(5, 0));
                passWrapper.setOpaque(false);
                passWrapper.add(pf, BorderLayout.CENTER);
                passWrapper.add(showPassCheck, BorderLayout.EAST);
                panel.add(passWrapper, gbc);
            } else {
                panel.add(inputFields[i], gbc);
            }
        }
        
        newStudentUsernameField.setToolTipText("Leave blank to keep current username.");

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Student Credentials",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String currentStudentUsername = currentStudentUsernameField.getText().trim();
            String newStudentUsernameInput = newStudentUsernameField.getText().trim();
            String newStudentPass = new String(newStudentPasswordField.getPassword());
            String confirmNewStudentPass = new String(confirmNewStudentPasswordField.getPassword());

            if (currentStudentUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "(!) Current student username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean isUsernameChanging = !newStudentUsernameInput.isEmpty() && !newStudentUsernameInput.equals(currentStudentUsername);
            boolean isPasswordChanging = !newStudentPass.isEmpty();

            if (!isUsernameChanging && !isPasswordChanging) {
                JOptionPane.showMessageDialog(this, "(i) No changes to credentials were specified.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (isPasswordChanging) {
                if (!newStudentPass.equals(confirmNewStudentPass)) {
                    JOptionPane.showMessageDialog(this, "(!) New student passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            String outcome = library.updateStudentCredentials(currentStudentUsername, newStudentUsernameInput, newStudentPass);
            if (outcome.startsWith("(!) New password validation failed:")) {
                 JTextArea textArea = new JTextArea(outcome);
                 textArea.setEditable(false); textArea.setWrapStyleWord(true); textArea.setLineWrap(true);
                 textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                 JScrollPane scrollPane = new JScrollPane(textArea);
                 scrollPane.setPreferredSize(new Dimension(350, 120));
                 JOptionPane.showMessageDialog(this, scrollPane, "Credential Update Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, outcome, "Student Credential Update",
                                          outcome.startsWith("(+)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDeleteStudentDialog() {
       String studentUsername = JOptionPane.showInputDialog(this,
                "Enter the username of the student account to delete:",
                "Delete Student Account", JOptionPane.WARNING_MESSAGE);

        if (studentUsername != null && !studentUsername.trim().isEmpty()) {
            studentUsername = studentUsername.trim();
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete the account for student '" + studentUsername + "'?\n" +
                    "This action cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmation == JOptionPane.YES_OPTION) {
                String result = library.deleteStudent(studentUsername);
                JOptionPane.showMessageDialog(this, result, "Delete Student Status",
                                              result.startsWith("(-)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }
        } else if (studentUsername != null) { 
            JOptionPane.showMessageDialog(this, "Student username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStudentForgotPasswordDialog() {
        JPanel verificationPanel = new JPanel(new GridBagLayout());
        verificationPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
        verificationPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbcVerification = new GridBagConstraints();
        gbcVerification.insets = new Insets(8, 8, 8, 8);
        gbcVerification.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Forgot Password - Student Verification");
        styleLabel(titleLabel, true);
        gbcVerification.gridwidth = 2; gbcVerification.anchor = GridBagConstraints.CENTER; gbcVerification.insets = new Insets(0,0,20,0);
        verificationPanel.add(titleLabel, gbcVerification);
        gbcVerification.gridwidth = 1; gbcVerification.anchor = GridBagConstraints.WEST; gbcVerification.insets = new Insets(8,8,8,8);

        String[] verificationLabels = {"Username:", "Aadhaar Number (12 digits):", "Phone Number (10 digits):"};
        JTextField usernameField = new JTextField(20); styleTextField(usernameField);
        JTextField aadhaarField = new JTextField(20); styleTextField(aadhaarField);
        applyDigitLimitFilter(aadhaarField, 12);
        JTextField phoneField = new JTextField(20); styleTextField(phoneField);
        applyDigitLimitFilter(phoneField, 10);


        Component[] verificationFields = {usernameField, aadhaarField, phoneField};

        for (int i = 0; i < verificationLabels.length; i++) {
            gbcVerification.gridx = 0; gbcVerification.gridy = i + 1; gbcVerification.anchor = GridBagConstraints.EAST;
            JLabel currentLabel = new JLabel(verificationLabels[i]); styleLabel(currentLabel, false);
            verificationPanel.add(currentLabel, gbcVerification);

            gbcVerification.gridx = 1; gbcVerification.anchor = GridBagConstraints.WEST; gbcVerification.fill = GridBagConstraints.HORIZONTAL;
            verificationPanel.add(verificationFields[i], gbcVerification);
        }

        int verificationResult = JOptionPane.showConfirmDialog(this, verificationPanel, "Verify Student Identity",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (verificationResult == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String aadhaar = aadhaarField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty() || aadhaar.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "(!) All verification fields are mandatory.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!aadhaar.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this, "(!) Aadhaar number must be 12 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "(!) Phone number must be 10 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User verifiedUser = library.verifyStudentDetailsForPasswordReset(username, aadhaar, phone);

            if (verifiedUser != null) {
                JPanel newPasswordPanel = new JPanel(new GridBagLayout());
                newPasswordPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
                newPasswordPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
                GridBagConstraints gbcPassword = new GridBagConstraints();
                gbcPassword.insets = new Insets(8, 8, 8, 8);
                gbcPassword.anchor = GridBagConstraints.WEST;

                JLabel newPasswordTitleLabel = new JLabel("Reset Password for " + username);
                styleLabel(newPasswordTitleLabel, true);
                gbcPassword.gridwidth = 2; gbcPassword.anchor = GridBagConstraints.CENTER; gbcPassword.insets = new Insets(0,0,20,0);
                newPasswordPanel.add(newPasswordTitleLabel, gbcPassword);
                gbcPassword.gridwidth = 1; gbcPassword.anchor = GridBagConstraints.WEST; gbcPassword.insets = new Insets(8,8,8,8);


                String[] passwordLabels = {"New Password:", "Confirm New Password:"};
                JPasswordField newPasswordField = new JPasswordField(20); stylePasswordField(newPasswordField);
                newPasswordField.setToolTipText("8-16 chars, 1 upper, 1 lower, 1 digit, 1 special");
                JPasswordField confirmPasswordField = new JPasswordField(20); stylePasswordField(confirmPasswordField);

                Component[] passwordFields = {newPasswordField, confirmPasswordField};

                for (int i = 0; i < passwordLabels.length; i++) {
                    gbcPassword.gridx = 0; gbcPassword.gridy = i + 1; gbcPassword.anchor = GridBagConstraints.EAST;
                    JLabel currentLabel = new JLabel(passwordLabels[i]); styleLabel(currentLabel, false);
                    newPasswordPanel.add(currentLabel, gbcPassword);

                    gbcPassword.gridx = 1; gbcPassword.anchor = GridBagConstraints.WEST; gbcPassword.fill = GridBagConstraints.HORIZONTAL;
                    
                    JCheckBox showPassCheck = new JCheckBox("Show");
                    stylePasswordShowCheckbox(showPassCheck);
                    showPassCheck.setForeground(COLOR_TEXT_SECONDARY);
                    final JPasswordField pf = (JPasswordField) passwordFields[i];
                    final char echo = pf.getEchoChar();
                    showPassCheck.addActionListener(evt -> pf.setEchoChar(showPassCheck.isSelected() ? (char)0 : echo));
                    
                    JPanel passWrapper = new JPanel(new BorderLayout(5, 0));
                    passWrapper.setOpaque(false);
                    passWrapper.add(pf, BorderLayout.CENTER);
                    passWrapper.add(showPassCheck, BorderLayout.EAST);
                    newPasswordPanel.add(passWrapper, gbcPassword);
                }

                int passwordResult = JOptionPane.showConfirmDialog(this, newPasswordPanel, "Set New Student Password",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (passwordResult == JOptionPane.OK_OPTION) {
                    String newPass = new String(newPasswordField.getPassword());
                    String confirmPass = new String(confirmPasswordField.getPassword());

                    if (newPass.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "(!) New password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        JOptionPane.showMessageDialog(this, "(!) Passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String resetOutcome = library.resetStudentPassword(username, newPass);
                    if (resetOutcome.startsWith("(!) New password validation failed:")) {
                         JTextArea textArea = new JTextArea(resetOutcome);
                         textArea.setEditable(false); textArea.setWrapStyleWord(true); textArea.setLineWrap(true);
                         textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                         JScrollPane scrollPane = new JScrollPane(textArea);
                         scrollPane.setPreferredSize(new Dimension(350, 120));
                         JOptionPane.showMessageDialog(this, scrollPane, "Password Reset Failed", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, resetOutcome, "Password Reset Status",
                            resetOutcome.startsWith("(+)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "(!) Verification failed. Details do not match any registered user or are incorrect.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showLibrarianForgotPasswordDialog() {
        JPanel accessKeyPanel = new JPanel(new GridBagLayout());
        accessKeyPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
        accessKeyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbcAccess = new GridBagConstraints();
        gbcAccess.insets = new Insets(8, 8, 8, 8);
        gbcAccess.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Librarian Credential Reset - Step 1");
        styleLabel(titleLabel, true);
        gbcAccess.gridwidth = 2; gbcAccess.anchor = GridBagConstraints.CENTER; gbcAccess.insets = new Insets(0,0,20,0);
        accessKeyPanel.add(titleLabel, gbcAccess);
        gbcAccess.gridwidth = 1; gbcAccess.anchor = GridBagConstraints.WEST; gbcAccess.insets = new Insets(8,8,8,8);

        JLabel accessKeyLabel = new JLabel("Enter Master Access Key:");
        styleLabel(accessKeyLabel, false);
        gbcAccess.gridx = 0; gbcAccess.gridy = 1; gbcAccess.anchor = GridBagConstraints.EAST;
        accessKeyPanel.add(accessKeyLabel, gbcAccess);

        JPasswordField accessKeyField = new JPasswordField(20);
        stylePasswordField(accessKeyField);
        gbcAccess.gridx = 1; gbcAccess.gridy = 1; gbcAccess.anchor = GridBagConstraints.WEST; gbcAccess.fill = GridBagConstraints.HORIZONTAL;

        JCheckBox showAccessKeyCheck = new JCheckBox("Show");
        stylePasswordShowCheckbox(showAccessKeyCheck);
        showAccessKeyCheck.setForeground(COLOR_TEXT_SECONDARY);
        final char echo = accessKeyField.getEchoChar();
        showAccessKeyCheck.addActionListener(evt -> accessKeyField.setEchoChar(showAccessKeyCheck.isSelected() ? (char)0 : echo));
        
        JPanel keyWrapper = new JPanel(new BorderLayout(5, 0));
        keyWrapper.setOpaque(false);
        keyWrapper.add(accessKeyField, BorderLayout.CENTER);
        keyWrapper.add(showAccessKeyCheck, BorderLayout.EAST);
        accessKeyPanel.add(keyWrapper, gbcAccess);

        int accessKeyResult = JOptionPane.showConfirmDialog(this, accessKeyPanel, "Verify Access Key",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (accessKeyResult == JOptionPane.OK_OPTION) {
            String enteredAccessKey = new String(accessKeyField.getPassword());
            if (enteredAccessKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "(!) Access Key cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JPanel newCredentialsPanel = new JPanel(new GridBagLayout());
            newCredentialsPanel.setBackground(COLOR_BACKGROUND_SECONDARY);
            newCredentialsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbcNewCreds = new GridBagConstraints();
            gbcNewCreds.insets = new Insets(8, 8, 8, 8);
            gbcNewCreds.anchor = GridBagConstraints.WEST;

            JLabel newCredsTitleLabel = new JLabel("Librarian Credential Reset - Step 2");
            styleLabel(newCredsTitleLabel, true);
            gbcNewCreds.gridwidth = 2; gbcNewCreds.anchor = GridBagConstraints.CENTER; gbcNewCreds.insets = new Insets(0,0,20,0);
            newCredentialsPanel.add(newCredsTitleLabel, gbcNewCreds);
            gbcNewCreds.gridwidth = 1; gbcNewCreds.anchor = GridBagConstraints.WEST; gbcNewCreds.insets = new Insets(8,8,8,8);

            String[] credLabels = {"New Librarian Username:", "New Librarian Password:", "Confirm New Password:"};
            JTextField newUsernameField = new JTextField(20); styleTextField(newUsernameField);
            JPasswordField newPasswordField = new JPasswordField(20); stylePasswordField(newPasswordField);
            newPasswordField.setToolTipText("8-16 chars, 1 upper, 1 lower, 1 digit, 1 special");
            JPasswordField confirmPasswordField = new JPasswordField(20); stylePasswordField(confirmPasswordField);

            Component[] credFields = {newUsernameField, newPasswordField, confirmPasswordField};
            JPasswordField[] passwordFields = {newPasswordField, confirmPasswordField};

            for (int i = 0; i < credLabels.length; i++) {
                gbcNewCreds.gridx = 0; gbcNewCreds.gridy = i + 1; gbcNewCreds.anchor = GridBagConstraints.EAST;
                JLabel currentLabel = new JLabel(credLabels[i]); styleLabel(currentLabel, false);
                newCredentialsPanel.add(currentLabel, gbcNewCreds);

                gbcNewCreds.gridx = 1; gbcNewCreds.anchor = GridBagConstraints.WEST; gbcNewCreds.fill = GridBagConstraints.HORIZONTAL;

                boolean isPasswordField = false;
                for(JPasswordField pf : passwordFields) { if (credFields[i] == pf) { isPasswordField = true; break; } }
                
                if (isPasswordField) {
                    JCheckBox showPassCheck = new JCheckBox("Show");
                    stylePasswordShowCheckbox(showPassCheck);
                    showPassCheck.setForeground(COLOR_TEXT_SECONDARY);
                    final JPasswordField pf = (JPasswordField) credFields[i];
                    final char echoChar = pf.getEchoChar();
                    showPassCheck.addActionListener(evt -> pf.setEchoChar(showPassCheck.isSelected() ? (char)0 : echoChar));
                    
                    JPanel passWrapper = new JPanel(new BorderLayout(5, 0));
                    passWrapper.setOpaque(false);
                    passWrapper.add(pf, BorderLayout.CENTER);
                    passWrapper.add(showPassCheck, BorderLayout.EAST);
                    newCredentialsPanel.add(passWrapper, gbcNewCreds);
                } else {
                    newCredentialsPanel.add(credFields[i], gbcNewCreds);
                }
            }

            int newCredentialsResult = JOptionPane.showConfirmDialog(this, newCredentialsPanel, "Set New Librarian Credentials",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (newCredentialsResult == JOptionPane.OK_OPTION) {
                String newUsername = newUsernameField.getText().trim();
                String newPass = new String(newPasswordField.getPassword());
                String confirmPass = new String(confirmPasswordField.getPassword());

                if (newUsername.isEmpty() || newPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "(!) New username and password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    JOptionPane.showMessageDialog(this, "(!) New passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String resetOutcome = library.resetLibrarianCredentialsWithAccessKey(enteredAccessKey, newUsername, newPass);
                if (resetOutcome.startsWith("(!) New password validation failed:")) {
                     JTextArea textArea = new JTextArea(resetOutcome);
                     textArea.setEditable(false); textArea.setWrapStyleWord(true); textArea.setLineWrap(true);
                     textArea.setOpaque(false); textArea.setForeground(COLOR_TEXT_PRIMARY);
                     JScrollPane scrollPane = new JScrollPane(textArea);
                     scrollPane.setPreferredSize(new Dimension(350, 120));
                     JOptionPane.showMessageDialog(this, scrollPane, "Credential Reset Failed", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, resetOutcome, "Librarian Credential Reset Status",
                        resetOutcome.startsWith("(+)") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                }

                 if (resetOutcome.startsWith("(+)")) {
                    cardLayout.show(mainPanel, "LibrarianLogin");
                }
            }
        }
    }


    private void showBookDetailsDialog(Book book) {
        JDialog detailsDialog = new JDialog(this, "Book Details: " + book.getTitle(), true);
        detailsDialog.setSize(650, 450); 
        detailsDialog.setLocationRelativeTo(this); 
        detailsDialog.getContentPane().setBackground(COLOR_BACKGROUND_SECONDARY);
        detailsDialog.setLayout(new BorderLayout(15, 15)); 
        detailsDialog.getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15)); 

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false); 
        imagePanel.setPreferredSize(new Dimension(200, 280)); 
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        if (book.getImagePath() != null && !book.getImagePath().trim().isEmpty()) {
            String path = book.getImagePath().trim();
            ImageIcon icon = null;
            File imgFile = new File(path);

            if (imgFile.isAbsolute() && imgFile.exists() && !imgFile.isDirectory()) { // Check absolute path first
                 try {
                    icon = new ImageIcon(imgFile.toURI().toURL());
                 } catch (Exception ex) {
                     System.err.println("Error loading book cover from absolute file path: " + path + " - " + ex.getMessage());
                 }
            } else if (path.startsWith("/")) { // Then check classpath resource
                URL imgUrl = getClass().getResource(path);
                if (imgUrl != null) {
                    icon = new ImageIcon(imgUrl);
                } else {
                     System.err.println("Book cover resource not found: " + path);
                }
            } else { // Could be a relative file path (not starting with /) or just a name for classpath
                 URL imgUrl = getClass().getResource("/" + path); // Try adding / for classpath
                 if (imgUrl != null) {
                    icon = new ImageIcon(imgUrl);
                 } else {
                    System.err.println("Book cover not found (tried as relative/classpath): " + path);
                 }
            }


            if (icon != null && icon.getIconWidth() > 0) {
                Image image = icon.getImage();
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();
                int boundWidth = 180; int boundHeight = 260; 
                double scaleFactor = Math.min(1d, Math.min((double)boundWidth / imgWidth, (double)boundHeight / imgHeight));
                int scaledWidth = (int) (imgWidth * scaleFactor); int scaledHeight = (int) (imgHeight * scaleFactor);
                Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setText("Image not found/invalid"); imageLabel.setForeground(COLOR_TEXT_SECONDARY);
            }
        } else {
            imageLabel.setText("No image provided"); imageLabel.setForeground(COLOR_TEXT_SECONDARY);
        }
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT)); 
        detailsDialog.add(imagePanel, BorderLayout.WEST); 

        JPanel textDetailsPanel = new JPanel(new GridBagLayout());
        textDetailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE; 
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(4, 0, 4, 10);  

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy"); 

        BiConsumer<String, String> addDetail = (labelText, valueText) -> {
            JLabel label = new JLabel("<html><b>" + labelText + ":</b></html>"); 
            label.setFont(FONT_LABEL); label.setForeground(COLOR_TEXT_SECONDARY);
            textDetailsPanel.add(label, gbc);

            gbc.gridx = 1; 
            JLabel value = new JLabel(valueText != null && !valueText.isEmpty() ? valueText : "N/A");
            styleLabel(value, false);
            textDetailsPanel.add(value, gbc);
            gbc.gridx = 0; 
        };

        JLabel bookTitleLabel = new JLabel(book.getTitle());
        bookTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        bookTitleLabel.setForeground(COLOR_TEXT_PRIMARY);
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(0,0,15,0);
        textDetailsPanel.add(bookTitleLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(4,0,4,10);


        addDetail.accept("Author", book.getAuthor());
        addDetail.accept("Category", book.getCategory());
        addDetail.accept("Subject", book.getSubject());
        addDetail.accept("Location Row", book.getLocationRow());
        addDetail.accept("Location Section", book.getLocationSection());
        addDetail.accept("Location Block", book.getLocationBlock());
        addDetail.accept("Status", book.isAvailable() ? "Available" : "Borrowed");

        if (!book.isAvailable()) {
            if (book.getBorrowedByUsername() != null) addDetail.accept("Borrowed By", book.getBorrowedByUsername());
            if (book.getBorrowedDate() != null) {
                addDetail.accept("Borrowed On", book.getBorrowedDate().format(formatter));
                addDetail.accept("Due By", book.getBorrowedDate().plusDays(Library.BORROWING_PERIOD_DAYS).format(formatter));
            }
            if (book.getCurrentBorrowingId() != null) addDetail.accept("Borrowing ID", book.getCurrentBorrowingId());
        }
        
        gbc.gridy++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
        textDetailsPanel.add(new JLabel(" "), gbc); // Spacer


        JScrollPane textScrollPane = new JScrollPane(textDetailsPanel);
        textScrollPane.setOpaque(false); textScrollPane.getViewport().setOpaque(false); 
        textScrollPane.setBorder(null);
        detailsDialog.add(textScrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        stylePrimaryButton(closeButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        closeButton.addActionListener(e -> detailsDialog.dispose()); 

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH); 

        detailsDialog.setVisible(true); 
    }

    private void showListAllUsersDialog() {
        JDialog usersDialog = new JDialog(this, "Registered Library Users", true);
        usersDialog.setSize(950, 550);
        usersDialog.setLocationRelativeTo(this);
        usersDialog.setLayout(new BorderLayout());
        usersDialog.getContentPane().setBackground(COLOR_BACKGROUND_PRIMARY);

        String[] columnNames = {"Username", "Full Name", "Gender", "Aadhaar", "Phone", "Address", "Fees Paid?", "Borrowed Books"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; } 
        };
        JTable usersTable = new JTable(tableModel);
        usersTable.setBackground(Color.WHITE);
        usersTable.setForeground(COLOR_TEXT_PRIMARY);
        usersTable.setGridColor(COLOR_TABLE_GRID);
        usersTable.getTableHeader().setBackground(COLOR_LEFT_PANE_DECORATIVE);
        usersTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
        usersTable.getTableHeader().setFont(FONT_BUTTON_PRIMARY);
        usersTable.setFont(FONT_LABEL);
        usersTable.setRowHeight(28);
        usersTable.setFillsViewportHeight(true); 

        List<User> allUsers = library.getAllUsers();
        if (allUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users are registered yet.", "User List", JOptionPane.INFORMATION_MESSAGE);
            usersDialog.dispose(); return;
        }
        for (User user : allUsers) {
            String borrowedBooksStr = user.getBorrowedBooks().stream()
                .map(Book::getTitle)
                .collect(Collectors.joining("; "));
            if(borrowedBooksStr.isEmpty()) borrowedBooksStr = "None";

            Object[] rowData = {
                user.getUsername(), user.getFullName(), user.getGender(), user.getAadhaarNumber(),
                user.getPhoneNumber(), user.getAddress(), user.hasPaidFees() ? "Yes" : "No",
                borrowedBooksStr
            };
            tableModel.addRow(rowData);
        }

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.getViewport().setBackground(Color.WHITE); 
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT));
        usersDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BACKGROUND_PRIMARY);
        buttonPanel.setBorder(new EmptyBorder(10,10,10,10)); 
        JButton closeButton = new JButton("Close");
        stylePrimaryButton(closeButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        closeButton.addActionListener(e -> usersDialog.dispose());
        buttonPanel.add(closeButton);
        usersDialog.add(buttonPanel, BorderLayout.SOUTH);

        usersDialog.setVisible(true);
    }

    private void showStudentActivityLogDialog() {
        String studentUsername = JOptionPane.showInputDialog(this,
                "Enter student username to view their activity log:",
                "View Student Activity", JOptionPane.PLAIN_MESSAGE);

        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            if (studentUsername != null) {
                 JOptionPane.showMessageDialog(this, "Student username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        studentUsername = studentUsername.trim();
        User student = library.findUserByUsername(studentUsername);
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Student with username '" + studentUsername + "' not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<TransactionRecord> activities = library.getTransactionsForUser(studentUsername);

        JDialog activityDialog = new JDialog(this, "Activity Log for " + student.getFullName() + " (" + studentUsername + ")", true);
        activityDialog.setSize(900, 600);
        activityDialog.setLocationRelativeTo(this);
        activityDialog.setLayout(new BorderLayout(10,10));
        activityDialog.getContentPane().setBackground(COLOR_BACKGROUND_PRIMARY);
        activityDialog.getRootPane().setBorder(new EmptyBorder(10,10,10,10));

        if (activities.isEmpty()) {
            JLabel noActivityLabel = new JLabel("No activity found for this student.", SwingConstants.CENTER);
            styleLabel(noActivityLabel, true);
            activityDialog.add(noActivityLabel, BorderLayout.CENTER);
        } else {
            String[] columnNames = {"Trans. ID", "Date", "Type", "Book Title", "Details", "Amount (₹)"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable activityTable = new JTable(tableModel);
            activityTable.setBackground(Color.WHITE);
            activityTable.setForeground(COLOR_TEXT_PRIMARY);
            activityTable.setGridColor(COLOR_TABLE_GRID);
            activityTable.getTableHeader().setBackground(COLOR_LEFT_PANE_DECORATIVE);
            activityTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
            activityTable.getTableHeader().setFont(FONT_BUTTON_PRIMARY);
            activityTable.setFont(FONT_LABEL);
            activityTable.setRowHeight(28);
            activityTable.setFillsViewportHeight(true);
            
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
            activityTable.setRowSorter(sorter);


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            for (TransactionRecord record : activities) {
                Object[] rowData = {
                        record.getTransactionId(),
                        record.getDate().format(formatter),
                        record.getType().getDisplayName(),
                        record.getBookTitle(),
                        record.getDetails(),
                        String.format("%.2f", record.getAmount())
                };
                tableModel.addRow(rowData);
            }
            JScrollPane scrollPane = new JScrollPane(activityTable);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT));
            activityDialog.add(scrollPane, BorderLayout.CENTER);
        }

        JButton closeButton = new JButton("Close");
        stylePrimaryButton(closeButton, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_HOVER);
        closeButton.addActionListener(e -> activityDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BACKGROUND_PRIMARY);
        buttonPanel.add(closeButton);
        activityDialog.add(buttonPanel, BorderLayout.SOUTH);

        activityDialog.setVisible(true);
    }


    public static void main(String[] args) {
        try {
            boolean nimbusFound = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusFound = true; break;
                }
            }
            if (!nimbusFound) UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { System.err.println("Could not set Look and Feel: " + e.getMessage()); }
        
        // Check DB connection on startup
        try (Connection conn = DatabaseUtil.getConnection()) {
            if (conn == null || conn.isClosed()) {
                 JOptionPane.showMessageDialog(null, "Failed to connect to the database. Please check your connection settings in DatabaseUtil.java and ensure the database server is running.", "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (SQLException e) {
             JOptionPane.showMessageDialog(null, "A critical database error occurred on startup: " + e.getMessage() + "\nPlease check your connection settings in DatabaseUtil.java and ensure the database server is running.", "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXT_PRIMARY);
        UIManager.put("Button.background", COLOR_ACCENT_PRIMARY); 
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", FONT_BUTTON_PRIMARY);
        UIManager.put("Button.focus", new Color(0,0,0,0)); 
        UIManager.put("OptionPane.messageFont", FONT_LABEL);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON_PRIMARY);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", COLOR_TEXT_PRIMARY);
        UIManager.put("FileChooser.background", COLOR_BACKGROUND_PRIMARY);
        UIManager.put("FileChooser.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("TextField.background", Color.WHITE); 
        UIManager.put("TextField.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("Label.foreground", COLOR_TEXT_PRIMARY); 
        UIManager.put("List.background", Color.WHITE); 
        UIManager.put("List.foreground", COLOR_TEXT_PRIMARY);

        SwingUtilities.invokeLater(LibraryManagementGUI::new);
    }
}
