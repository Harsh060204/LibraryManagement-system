
Library Management System - Documentation
1. Overview
This document provides a comprehensive overview of the Library Management System, a Java-based desktop application designed to streamline and automate the operations of a library. The system uses a graphical user interface (GUI) built with Java Swing for user interaction and a MySQL database for persistent data storage.

The application serves two primary types of users: Students and Librarians, each with a dedicated portal and a distinct set of functionalities tailored to their roles. It aims to provide a robust, user-friendly, and efficient platform for managing books, user accounts, and library transactions.

2. Core Features
The system is packed with features to cover all essential library operations.

2.1. Dual User Roles & Authentication
Student Portal: A dedicated interface for students to manage their library activities.

Librarian Portal: A comprehensive administrative panel for librarians to manage the entire library system.

Secure Login: Separate and secure login mechanisms for both students and librarians.

Password Security: Includes password validation rules (length, character types) and "show password" toggles for better usability.

2.2. Student Features
Self-Registration: New students can create their own accounts with necessary personal details.

Fee Management: The system handles a one-time registration fee. Students are prompted for payment upon their first login and cannot borrow books until the fee is paid.

Book Search & Discovery: Students can search the entire book catalog using various filters like Title, Author, Category, Subject, Availability.

Book Borrowing: Students can borrow available books, up to a predefined limit (currently 3 books).

Book Returning: Students can return borrowed books. The system automatically calculates and imposes fines for late returns.

View Borrowed Books: A dedicated panel allows students to see a list of all the books they have currently borrowed, along with their due dates.

Password Reset: A secure "Forgot Password" feature allows students to reset their password after verifying their identity using their Aadhaar and phone number.

2.3. Librarian Features (Administrative)
Book Management (CRUD):

Add: Librarians can add new books to the catalog with details like title, author, category, subject, location, and an optional cover image.

Edit: Existing book details can be modified.

Remove: Books can be removed from the library, but only if they are not currently borrowed.

User Management:

List All Users: View a detailed table of all registered users (students and librarians) with their information and borrowing status.

Change Student Credentials: Librarians can update a student's username or reset their password.

Delete Student Account: Librarians can permanently delete a student's account, provided they have no outstanding borrowed books.

Transaction & Activity Monitoring:

View Student Activity Log: Pull up a complete history of any student's transactions, including books borrowed, returned, fees paid, and fines.

Find Loan by ID: Look up the details of a specific borrowing transaction using its unique ID.

Financial Management:

View Total Income: Get an overview of the total income generated from registration fees and late fines.

Librarian Account Security:

Change Credentials: Librarians can change their own login credentials.

Forgot Credentials: A master access key system allows for resetting the librarian's password in case it's forgotten.

2.4. Modern GUI & User Experience
CardLayout Navigation: The application uses a CardLayout to seamlessly switch between different panels (Home, Login, Dashboard, etc.).

Visually Appealing Design: A modern, clean color palette and consistent font usage create a pleasant user experience.

Interactive Image Backgrounds: Decorative panels with background images enhance the visual appeal of login and dashboard screens.

Real-time Validation: The registration form provides instant feedback, for example, by checking if a username is already taken.

Dialogs & Pop-ups: The system uses JOptionPane for informative messages, confirmations, and data input, ensuring clear communication with the user.

Sortable Tables: All data tables (books, users, etc.) can be sorted by clicking on the column headers.

3. Technical Architecture
3.1. Technology Stack
Language: Java

GUI Framework: Java Swing

Database: MySQL (connected via JDBC)

Build Tool: Can be compiled and run with any standard Java IDE (like IntelliJ, Eclipse) or via the command line with the JDK.

3.2. Project Structure (Key Classes)
LibraryManagementGUI.java: The main class that initializes the Swing application. It builds all the UI panels, manages the CardLayout, and handles all user interactions and event listeners.

Library.java: The core logic or "engine" of the system. It acts as an intermediary between the GUI and the database. It contains all the business logic for adding books, registering users, processing transactions, etc.

DatabaseUtil.java: A utility class responsible for managing the connection to the MySQL database. This is the primary file to configure for your specific database setup.

User.java: A model class representing a user (student or librarian).

Book.java: A model class representing a book.

TransactionRecord.java: A model class representing a single transaction or activity log entry.

ActivityType.java: An enum that defines the different types of transactions that can be logged (e.g., BOOK_BORROWED, FEE_PAID).

4. Setup & Configuration
To run this application, you need to have Java Development Kit (JDK) and a MySQL server installed.

4.1. Database Setup
Create a Database: In your MySQL server, create a new database. The application defaults to the name library_db.

CREATE DATABASE library_db;

Create Tables: Execute the following SQL script to create the necessary tables (users, books, transactions) within your library_db database.

USE library_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    aadhaar_number VARCHAR(12) NOT NULL UNIQUE,
    phone_number VARCHAR(10) NOT NULL,
    address TEXT NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'student',
    has_paid_fees BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    borrowed_by_username VARCHAR(50),
    borrowed_date DATE,
    current_borrowing_id VARCHAR(20) UNIQUE,
    location_row VARCHAR(50),
    location_section VARCHAR(50),
    location_block VARCHAR(50),
    image_path VARCHAR(255),
    FOREIGN KEY (borrowed_by_username) REFERENCES users(username) ON DELETE SET NULL
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(50),
    book_title VARCHAR(255),
    activity_type VARCHAR(50) NOT NULL,
    details TEXT,
    amount DOUBLE NOT NULL DEFAULT 0,
    transaction_date DATE NOT NULL,
    FOREIGN KEY (student_username) REFERENCES users(username) ON DELETE CASCADE
);

-- IMPORTANT: Create an initial librarian account
INSERT INTO users (username, password, full_name, gender, aadhaar_number, phone_number, address, role, has_paid_fees)
VALUES ('admin', 'Admin@123', 'Library Admin', 'N/A', '000000000000', '0000000000', 'Library Office', 'librarian', TRUE);


Note: The default librarian account is created with username: admin and password: Admin@123. It is highly recommended to change these credentials after the first login.

4.2. Java Application Configuration
JDBC Driver: Make sure the MySQL JDBC driver JAR file (e.g., mysql-connector-j-X.X.X.jar) is included in your project's classpath.

Configure DatabaseUtil.java: Open the DatabaseUtil.java file and update the following constants with your MySQL database credentials:

--
private static final String DB_URL = "jdbc:mysql://localhost:3306/
// --- IMPORTANT: CONFIGURE YOUR DATABASE CONNECTION HERE -library_db";
private static final String DB_USER = "your_mysql_username"; // e.g., "root"
private static final String DB_PASSWORD = "your_mysql_password"; // e.g., "password"

4.3. Running the Application
Once the database and Java configuration are complete, compile and run the LibraryManagementGUI.java file. This will launch the application, starting from the home screen.



Library Management System - User Manual
1. Introduction
Welcome to the Library Management System! This desktop application provides a comprehensive platform for managing a library's collection and its members. It offers two distinct portals: one for students to browse, borrow, and return books, and another for librarians to manage the entire library inventory and user base.

This manual will guide you through the setup, features, and usage of the application for both user roles.

2. Core Features
Dual User Portals: Separate, secure interfaces for Students and Librarians with role-specific functionalities.

Comprehensive Book Management: Librarians can add, edit, and remove books, including details like location and cover images.

Student Self-Service: Students can register, search the catalog, view book details, borrow items, and see their borrowing history.

Full User Administration: Librarians have complete control to manage the student user base, including changing credentials and deleting accounts.

Automated Fee & Fine Calculation: The system automatically calculates and logs late return fines and manages one-time registration fees.

Transaction Logging: Every significant action, from borrowing a book to paying a fine, is recorded for accountability and history tracking.

Secure Credential Management: Includes features for password recovery for both students and librarians, with an added layer of security for admin recovery via a master key.

3. System Requirements
To run this application, you will need the following software installed on your computer:

Java Development Kit (JDK): Version 8 or newer.

An IDE (Optional but Recommended): An Integrated Development Environment like IntelliJ IDEA, Eclipse, or NetBeans to compile and run the Java code.

MySQL Server: The application uses a MySQL database to store all its data.

MySQL Connector/J: The official MySQL JDBC driver. This .jar file must be included in your project's classpath.

4. First-Time Setup
Before you can run the application, you need to set up the database.

Start MySQL Server: Ensure your local or remote MySQL server is running.

Create the Database: Using a tool like MySQL Workbench, DBeaver, or the command line, run the provided SQL script (library_mysql_schema.sql). This will create the necessary database (library_db) and all required tables (users, books, transactions, librarian).

Configure the Java Application:

Open the LibraryManagementGUI.java file in a text editor or your IDE.

Navigate to the DatabaseManager class within the file.

Update the DB_URL, USER, and PASS constants with your specific MySQL database connection details.

Add JDBC Driver to Project:

In your IDE, add the downloaded mysql-connector-j-x.x.x.jar file to your project's build path or library dependencies.

Compile and Run: Compile and run the LibraryManagementGUI.java file from your IDE or the command line.

5. Getting Started: Main Window
Upon launching the application, you are greeted with the main window, which presents two main options:

Student Portal: For library members to access their accounts.

Librarian Portal: For library staff to manage the system.

6. Student Portal Guide
6.1. Registering a New Account
If you are a new student, you must first register.

From the main window, click Student Portal.

On the Student Login screen, click the "New User? Register" link.

Fill out the registration form with all the required details:

Username: A unique username. The system will check if it's already taken.

Password: Must be 8-16 characters and include at least one uppercase letter, one lowercase letter, one number, and one special character.

Full Name, Gender, Aadhaar, Phone, Address: Your personal details.

Click the Register button. You will receive a confirmation message upon successful registration.

6.2. Logging In
From the main window, click Student Portal.

Enter your registered Username and Password.

Click the Login button.

6.3. Student Dashboard
After logging in, you will see the Student Dashboard, which is your main hub for all activities.

Search & View Books: Opens the book catalog.

Borrow a Book: Allows you to borrow a book by entering its exact title.

Return a Book: Allows you to return a book you have borrowed by entering its title.

View My Borrowed Books: Shows a list of all books currently checked out under your account.

Pay Registration Fee: If you haven't paid the one-time registration fee, this button will be visible. You must pay this fee before you can borrow books.

Logout: Logs you out of the system.

6.4. Forgot Password
On the Student Login screen, click the "Forgot Password?" link.

You will be asked to verify your identity by providing your Username, Aadhaar Number, and Phone Number.

If the details match, you will be prompted to enter and confirm a new password.

7. Librarian Portal Guide
7.1. Logging In
The default librarian credentials are:

Username: admin

Password: admin123

From the main window, click Librarian Portal.

Enter the librarian credentials and click Login.

7.2. Librarian Dashboard
The Librarian Dashboard provides complete control over the library's resources.

7.3. Managing Books
Click the "Add/Edit Book" button to open the Book Management screen.

Viewing and Searching: The table displays all books. You can use the filter options to search for specific books by category, subject, or availability. Double-click any book to see its full details.

Adding a New Book:

Click the "Add New Book" button.

Fill in the book's details in the form that appears. Title, Author, Category, and Subject are mandatory.

You can optionally add an image by clicking "Choose..." and selecting an image file from your computer.

Click OK to add the book to the database.

Editing a Book:

Select a book from the table.

Click the "Edit Selected" button.

Modify the details in the form.

Click OK to save the changes.

Removing a Book:

Click the "Remove Book" button on the dashboard.

Enter the exact title of the book you wish to remove.

Click OK. Note: A book cannot be removed if it is currently borrowed by a student.

7.4. Managing Students
List All Users: Displays a table of all registered students, their fee status, and currently borrowed books.

View Student Activity Log: Prompts for a student's username and then displays a complete history of that student's transactions (borrows, returns, payments).

Change Student Credentials: Allows a librarian to update a student's username and/or reset their password.

Delete Student Account: Permanently removes a student's account. This is only possible if the student has no outstanding books.

7.5. Financials & Administration
View Library Income: Displays the total income generated from registration fees and late fines.

Change My Credentials: Allows the logged-in librarian to change their own username and password. This requires the Master Access Key (SECRET123 by default).

Forgot Credentials: If the librarian password is lost, it can be reset from the Librarian Login screen using the Master Access Key.

8. Troubleshooting
Cannot connect to database:

Ensure your MySQL server is running.

Verify that the database connection details (DB_URL, USER, PASS) in the DatabaseManager class are correct.

Check if the MySQL Connector/J .jar file is correctly added to your project's classpath.

Images not displaying:

For background images, ensure the absolute file paths in the LibraryManagementGUI.java file are correct and point to existing images.

For book covers, ensure the path saved in the database (either an absolute path or a classpath resource path like /images/cover.png) is valid.

Application does not start:

Make sure you have a compatible version of the Java JDK installed and configured correctly in your system's environment variables.
