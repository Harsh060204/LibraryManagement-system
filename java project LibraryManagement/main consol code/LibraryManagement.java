import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Represents a book in the library.
 */
class Book {
    private final String title;
    private final String author;
    private final String category;
    private final String subject;
    private boolean isAvailable;
    private User borrowedBy;
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

    // --- Getters ---
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public boolean isAvailable() { return isAvailable; }
    public User getBorrowedBy() { return borrowedBy; }
    public LocalDate getBorrowedDate() { return borrowedDate; }

    // --- Setters ---
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setBorrowedBy(User user) { this.borrowedBy = user; }
    public void setBorrowedDate(LocalDate date) { this.borrowedDate = date; }

    @Override
    public String toString() {
        String status = isAvailable ? "Available" : "Borrowed by " + (borrowedBy != null ? borrowedBy.getUsername() : "N/A");
        return String.format("%-35s | %-25s | %-15s | %-15s | %s",
                title, author, category, subject, status);
    }
}

/**
 * Represents a user (student) of the library.
 */
class User {
    private final String username;
    private final String password;
    private final String fullName;
    private final String gender;
    private final String aadhaarNumber;
    private final String phoneNumber;
    private final String address;
    private final List<Book> borrowedBooks;
    private boolean hasPaidFees;

    public User(String username, String password, String fullName, String gender, String aadhaarNumber,
                String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.gender = gender;
        this.aadhaarNumber = aadhaarNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.borrowedBooks = new ArrayList<>();
        this.hasPaidFees = false; // Initial fee status
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getGender() { return gender; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public List<Book> getBorrowedBooks() { return borrowedBooks; }
    public boolean hasPaidFees() { return hasPaidFees; }

    // --- Actions ---
    public void payFees() { hasPaidFees = true; }
    public void borrowBook(Book book) { borrowedBooks.add(book); }
    public void returnBook(Book book) { borrowedBooks.remove(book); }
}

/**
 * Represents the library, managing books, users, and operations.
 */
class Library {
    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final String librarianUsername = "admin";
    private final String librarianPassword = "admin123";
    private int income = 0;
    private static final int FINE_PER_DAY = 1; // ₹1 fine per day
    private static final int BORROWING_PERIOD_DAYS = 15;
    private static final int MAX_BORROWED_BOOKS = 3;
    private static final int REGISTRATION_FEE = 100;


    public void addBook(String title, String author, String category, String subject) {
        if (books.containsKey(title)) {
            System.out.println("(!) A book with this title already exists.");
        } else {
            books.put(title, new Book(title, author, category, subject));
            System.out.println("(+) Book added successfully: " + title);
        }
    }

    public void removeBook(String title) {
        if (books.containsKey(title)) {
            if (!books.get(title).isAvailable()) {
                System.out.println("(!) Cannot remove a book that is currently borrowed.");
            } else {
                books.remove(title);
                System.out.println("(-) Book removed successfully: " + title);
            }
        } else {
            System.out.println("(!) Book not found.");
        }
    }

    public boolean registerUser(String username, String password, String fullName, String gender,
                                String aadhaarNumber, String phoneNumber, String address) {
        // --- Input Validations ---
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || aadhaarNumber.isEmpty() ||
                phoneNumber.isEmpty() || address.isEmpty()) {
            System.out.println("(!) Error: All fields are mandatory. Please fill all details.");
            return false;
        }
        List<String> validGenders = Arrays.asList("Male", "Female", "Trans");
        if (!validGenders.contains(gender)) {
            System.out.println("(!) Error: Invalid gender. Must be 'Male', 'Female', or 'Trans'.");
            return false;
        }
        if (!aadhaarNumber.matches("\\d{12}")) {
            System.out.println("(!) Error: Aadhaar number must be exactly 12 digits long.");
            return false;
        }
        if (!phoneNumber.matches("\\d{10}")) {
            System.out.println("(!) Error: Phone number must be exactly 10 digits long.");
            return false;
        }
        if (users.containsKey(username)) {
            System.out.println("(!) Error: Username '" + username + "' is already taken. Please choose another.");
            return false;
        }

        users.put(username, new User(username, password, fullName, gender, aadhaarNumber, phoneNumber, address));
        System.out.println("(+) User '" + username + "' registered successfully!");
        System.out.println("    Please note: A one-time registration fee of ₹" + REGISTRATION_FEE + " is required before borrowing books.");
        return true;
    }

    public User authenticateUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean authenticateLibrarian(String username, String password) {
        return username.equals(librarianUsername) && password.equals(librarianPassword);
    }

    public Book searchBook(String title) {
        return books.get(title);
    }

    public void searchAndFilterBooks(String filter, String value) {
        boolean found = false;
        printBookListHeader();
        for (Book book : books.values()) {
            boolean match = false;
            switch (filter.toLowerCase()) {
                case "all":
                    match = true;
                    break;
                case "available":
                    if (book.isAvailable()) match = true;
                    break;
                case "borrowed":
                    if (!book.isAvailable()) match = true;
                    break;
                case "category":
                    if (book.getCategory().equalsIgnoreCase(value)) match = true;
                    break;
                case "subject":
                    if (book.getSubject().equalsIgnoreCase(value)) match = true;
                    break;
            }
            if (match) {
                System.out.println(book);
                found = true;
            }
        }
        if (!found) {
            System.out.println("   No books found matching your criteria.");
        }
        printSeparator('-');
    }

    public void borrowBook(User user, String title) {
        if (!user.hasPaidFees()) {
            System.out.println("(!) You must pay the registration fee before borrowing books.");
            return;
        }
        if (user.getBorrowedBooks().size() >= MAX_BORROWED_BOOKS) {
            System.out.println("(!) You have reached the borrowing limit (" + MAX_BORROWED_BOOKS + " books). Please return a book first.");
            return;
        }

        Book book = books.get(title);
        if (book != null && book.isAvailable()) {
            book.setAvailable(false);
            book.setBorrowedBy(user);
            book.setBorrowedDate(LocalDate.now());
            user.borrowBook(book);
            System.out.println("(>) Book '" + title + "' borrowed successfully. Return by: " + LocalDate.now().plusDays(BORROWING_PERIOD_DAYS));
        } else if (book != null && !book.isAvailable()) {
            System.out.println("(!) Book '" + title + "' is currently borrowed by " + book.getBorrowedBy().getUsername() + ".");
        } else {
            System.out.println("(!) Book '" + title + "' not found.");
        }
    }

    public void returnBook(User user, String title) {
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

            if (daysBetween > BORROWING_PERIOD_DAYS) {
                long daysOverdue = daysBetween - BORROWING_PERIOD_DAYS;
                int fine = (int) daysOverdue * FINE_PER_DAY;
                income += fine;
                System.out.println("(!) Book returned " + daysOverdue + " days late. Fine imposed: ₹" + fine);
            }

            user.returnBook(bookToReturn);
            bookToReturn.setAvailable(true);
            bookToReturn.setBorrowedBy(null);
            bookToReturn.setBorrowedDate(null);
            System.out.println("(<) Book '" + title + "' returned successfully.");
        } else {
            System.out.println("(!) You haven't borrowed a book with the title '" + title + "' or it was not found.");
        }
    }

    public void listAllBooks() {
        System.out.println("\n--- Library Book Collection ---");
        if (books.isEmpty()) {
            System.out.println("   The library currently has no books.");
            return;
        }
        printBookListHeader();
        for (Book book : books.values()) {
            System.out.println(book);
        }
        printSeparator('-');
    }

    public void listAllUsers() {
        System.out.println("\n--- Registered Library Users ---");
        if (users.isEmpty()) {
            System.out.println("   No users are registered yet.");
            return;
        }
        System.out.printf("%-15s | %-25s | %-6s | %-12s | %-10s | %-20s | %s%n",
                "Username", "Full Name", "Gender", "Aadhaar", "Phone", "Address", "Fees Paid?");
        printSeparator('=');
        for (User user : users.values()) {
            System.out.printf("%-15s | %-25s | %-6s | %-12s | %-10s | %-20s | %s%n",
                    user.getUsername(), user.getFullName(), user.getGender(),
                    user.getAadhaarNumber(), user.getPhoneNumber(), user.getAddress(),
                    user.hasPaidFees() ? "Yes" : "No");

            if (!user.getBorrowedBooks().isEmpty()) {
                System.out.println("    Borrowed Books:");
                for (Book book : user.getBorrowedBooks()) {
                    System.out.println("      - " + book.getTitle() + " (Borrowed on: " + book.getBorrowedDate() + ")");
                }
            }
            printSeparator('-');
        }
    }

    public void showIncome() {
        System.out.println("\n--- Library Income Report ---");
        System.out.println("   Total Library Income (from fines & fees): ₹" + income);
        printSeparator('-');
    }
    public void collectFees(User user) {
        if (!user.hasPaidFees()) {
            income += REGISTRATION_FEE;
            user.payFees();
            System.out.println("(+) Registration fee of ₹" + REGISTRATION_FEE + " paid successfully for " + user.getUsername() + ".");
        } else {
            System.out.println("(!) Fees already paid for " + user.getUsername() + ".");
        }
    }

    // --- Helpers ---
    private void printBookListHeader() {
        printSeparator('=');
        System.out.printf("%-35s | %-25s | %-15s | %-15s | %s%n",
                "Title", "Author", "Category", "Subject", "Status");
        printSeparator('-');
    }

    public static void printSeparator(char c) {
        for (int i = 0; i < 110; i++) {
            System.out.print(c);
        }
        System.out.println();
    }
}

/**
 * Main class to run the Library Management System.
 */
public class LibraryManagement {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        // Add some sample data
        library.addBook("The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Sci-Fi", "Humor");
        library.addBook("Pride and Prejudice", "Jane Austen", "Classic", "Romance");
        library.addBook("To Kill a Mockingbird", "Harper Lee", "Fiction", "Classic");
        library.addBook("1984", "George Orwell", "Dystopian", "Political");
        library.addBook("The Lord of the Rings", "J.R.R. Tolkien", "Fantasy", "Adventure");
        System.out.println();

        mainLoop:
        while (true) {
            printHeader("Library Management System - Main Menu");
            System.out.println("1. Student Portal");
            System.out.println("2. Librarian Portal");
            System.out.println("3. Exit");
            library.printSeparator('-');
            System.out.print(">>> Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    studentMenu();
                    break;
                case 2:
                    librarianMenu();
                    break;
                case 3:
                    System.out.println("\n(+) Thank you for using the Library System. Goodbye!");
                    break mainLoop;
                default:
                    System.out.println("(!) Invalid choice. Please try again.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine(); // Consume leftover newline & wait for user
        }

        scanner.close();
    }

    // --- UI Helpers ---
    private static void printHeader(String title) {
        System.out.println();
        Library.printSeparator('=');
        System.out.println("  " + title);
        Library.printSeparator('=');
    }

    private static int getIntInput() {
        int choice = -1;
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.print("(!) Input cannot be empty. Please enter a number: ");
                    continue;
                }
                choice = Integer.parseInt(line);
                break; // Exit loop if successful
            } catch (NumberFormatException e) {
                System.out.print("(!) Invalid input. Please enter a number: ");
            }
        }
        return choice;
    }

    private static String getStringInput(String prompt) {
        String input = "";
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                break;
            } else {
                System.out.println("(!) Input cannot be empty. Please try again.");
            }
        }
        return input;
    }


    // --- Student Menus ---
    private static void studentMenu() {
        while (true) {
            printHeader("Student Portal");
            System.out.println("1. Register New Student");
            System.out.println("2. Student Login");
            System.out.println("3. Back to Main Menu");
            library.printSeparator('-');
            System.out.print(">>> Choose an option: ");
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
                    return; // Go back
                default:
                    System.out.println("(!) Invalid choice. Try again.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void registerStudent() {
        printHeader("New Student Registration");
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
            System.out.println("(!) Invalid input. Please enter Male, Female, or Trans.");
        }

        String aadhaarNumber = getStringInput("Enter Aadhaar number (12 digits): ");
        String phoneNumber = getStringInput("Enter phone number (10 digits): ");
        String address = getStringInput("Enter address: ");

        library.registerUser(username, password, fullName, gender, aadhaarNumber, phoneNumber, address);
    }

    private static User loginStudent() {
        printHeader("Student Login");
        String username = getStringInput("Enter username: ");
        String password = getStringInput("Enter password: ");

        User user = library.authenticateUser(username, password);

        if (user == null) {
            System.out.println("(!) Invalid username or password.");
            return null;
        }

        System.out.println("(+) Login successful. Welcome " + user.getFullName() + "!");

        // Handle fee payment upon login if not already paid
        if (!user.hasPaidFees()) {
            System.out.println("\n(!) A one-time registration fee of ₹100 is pending.");
            System.out.print("    Do you want to pay now? (yes/no): ");
            String payChoice = scanner.nextLine().trim();
            if (payChoice.equalsIgnoreCase("yes")) {
                library.collectFees(user);
            } else {
                System.out.println("(!) You must pay the fee to borrow books. You can log in again later to pay.");
                return null; // Don't proceed to logged-in menu if fee isn't paid
            }
        }
        return user;
    }

    private static void studentLoggedInMenu(User user) {
        while (true) {
            printHeader("Welcome, " + user.getUsername() + "!");
            System.out.println("1. Search & View Books");
            System.out.println("2. Borrow a Book");
            System.out.println("3. Return a Book");
            System.out.println("4. View My Borrowed Books");
            System.out.println("5. Logout");
            library.printSeparator('-');
            System.out.print(">>> Choose an option: ");
            int option = getIntInput();

            switch (option) {
                case 1:
                    searchBookMenu();
                    break;
                case 2:
                    String borrowTitle = getStringInput("Enter the exact title of the book to borrow: ");
                    library.borrowBook(user, borrowTitle);
                    break;
                case 3:
                    String returnTitle = getStringInput("Enter the exact title of the book to return: ");
                    library.returnBook(user, returnTitle);
                    break;
                case 4:
                    viewMyBooks(user);
                    break;
                case 5:
                    System.out.println("(+) Logging out.");
                    return; // Go back to student main menu
                default:
                    System.out.println("(!) Invalid choice.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void viewMyBooks(User user) {
        printHeader("My Borrowed Books");
        List<Book> myBooks = user.getBorrowedBooks();
        if (myBooks.isEmpty()) {
            System.out.println("   You currently have no borrowed books.");
        } else {
            System.out.printf("%-35s | %-25s | %s%n", "Title", "Author", "Borrowed Date");
            Library.printSeparator('-');
            for (Book b : myBooks) {
                System.out.printf("%-35s | %-25s | %s%n", b.getTitle(), b.getAuthor(), b.getBorrowedDate());
            }
        }
        Library.printSeparator('-');
    }


    private static void searchBookMenu() {
        while (true) {
            printHeader("Search Books");
            System.out.println("1. View All Books");
            System.out.println("2. View Available Books");
            System.out.println("3. View Borrowed Books");
            System.out.println("4. Search by Category");
            System.out.println("5. Search by Subject");
            System.out.println("6. Back to Student Menu");
            library.printSeparator('-');
            System.out.print(">>> Choose an option: ");
            int filterOption = getIntInput();

            switch (filterOption) {
                case 1:
                    library.searchAndFilterBooks("all", "");
                    break;
                case 2:
                    library.searchAndFilterBooks("available", "");
                    break;
                case 3:
                    library.searchAndFilterBooks("borrowed", "");
                    break;
                case 4:
                    String category = getStringInput("Enter category: ");
                    library.searchAndFilterBooks("category", category);
                    break;
                case 5:
                    String subject = getStringInput("Enter subject: ");
                    library.searchAndFilterBooks("subject", subject);
                    break;
                case 6:
                    return; // go back
                default:
                    System.out.println("(!) Invalid choice.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    // --- Librarian Menus ---
    private static void librarianMenu() {
        printHeader("Librarian Login");
        String libUser = getStringInput("Enter librarian username: ");
        String libPass = getStringInput("Enter password: ");

        if (!library.authenticateLibrarian(libUser, libPass)) {
            System.out.println("(!) Invalid librarian credentials.");
            return;
        }

        System.out.println("(+) Librarian login successful.");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();

        while (true) {
            printHeader("Librarian Menu");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. List All Books");
            System.out.println("4. List All Users");
            System.out.println("5. View Library Income");
            System.out.println("6. Logout");
            library.printSeparator('-');
            System.out.print(">>> Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    printHeader("Add New Book");
                    String title = getStringInput("Enter book title: ");
                    String author = getStringInput("Enter author: ");
                    String category = getStringInput("Enter category: ");
                    String subject = getStringInput("Enter subject: ");
                    library.addBook(title, author, category, subject);
                    break;
                case 2:
                    printHeader("Remove Book");
                    String removeTitle = getStringInput("Enter title of the book to remove: ");
                    library.removeBook(removeTitle);
                    break;
                case 3:
                    library.listAllBooks();
                    break;
                case 4:
                    library.listAllUsers();
                    break;
                case 5:
                    library.showIncome();
                    break;
                case 6:
                    System.out.println("(+) Logging out.");
                    return; // Go back to main menu
                default:
                    System.out.println("(!) Invalid choice.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
}