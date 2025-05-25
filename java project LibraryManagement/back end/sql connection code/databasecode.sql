CREATE DATABASE library_management;

USE library_management;

CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('Male', 'Female', 'Trans') NOT NULL,
    aadhaar_number CHAR(12) NOT NULL,
    phone_number CHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    has_paid_fees BOOLEAN DEFAULT FALSE
);

CREATE TABLE books (
    title VARCHAR(100) PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(50) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    borrowed_by VARCHAR(50),
    borrowed_date DATE,
    FOREIGN KEY (borrowed_by) REFERENCES users(username)
);