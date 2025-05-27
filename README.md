# LibraryManagement-system
**Library Management System** - A Java console app with MySQL backend for tracking books, users, and loans. Features book borrowing/returns, late fines, user registration, and admin controls. Uses JDBC for database operations with tables for books, users, and transactions. Ideal for small libraries.
Here's a detailed breakdown of the **full features** of this Library Management System code:

---

### **Core Features**
1. **User Management**
   - **Student Registration**:  
     - Validates all fields (username, password, full name, etc.).  
     - Ensures unique username and valid Aadhaar (12 digits)/phone (10 digits).  
     - Supports gender options: Male/Female/Trans.  
   - **Student Login**: Secure authentication with username/password.  
   - **Fee Payment**: One-time ₹100 registration fee required for borrowing books.  

2. **Book Management**  
   - **Add/Remove Books**: Librarians can manage the library catalog.  
   - **Book Status Tracking**: Tracks availability, borrower details, and due dates.  
   - **Search & Filter**:  
     - View all/available/borrowed books.  
     - Search by category (e.g., "Sci-Fi") or subject (e.g., "Romance").  

3. **Borrowing System**  
   - **Borrow Limits**: Max 3 books per user.  
   - **Due Dates**: 15-day borrowing period.  
   - **Late Fines**: ₹1 per day overdue.  

4. **Librarian Features**  
   - Admin login (`admin/admin123`).  
   - View all users with their borrowed books.  
   - Track library income (registration fees + fines).  

---

### **Technical Features**
1. **Data Structures**  
   - Uses `HashMap` for storing books and users for O(1) lookups.  
   - `List<Book>` to track borrowed books per user.  

2. **Input Validation**  
   - Ensures non-empty fields for registration.  
   - Validates Aadhaar (12 digits) and phone numbers (10 digits).  
   - Restricts gender input to Male/Female/Trans.  

3. **Date Handling**  
   - Uses `java.time.LocalDate` to track borrowing/return dates.  
   - Calculates overdue days with `ChronoUnit.DAYS.between()`.  

4. **Console UI**  
   - Clean menu-driven interface with separators (`=====`) for readability.  
   - Error messages for invalid inputs (e.g., `(!) Book not found`).  

---

### **Sample Workflows**
1. **Student Workflow**  
   ```
   1. Register → 2. Login → 3. Pay fee → 4. Borrow books → 5. Return books
   ```

2. **Librarian Workflow**  
   ```
   1. Login → 2. Add/Remove books → 3. View users → 4. Check income
   ```

---

### **Example Outputs**
1. **Book List**  
   ```
   Title                            | Author                   | Category       | Subject        | Status
   ----------------------------------------------------------------------------------------------------
   The Lord of the Rings            | J.R.R. Tolkien           | Fantasy        | Adventure      | Available
   ```

2. **User List**  
   ```
   Username     | Full Name               | Gender | Aadhaar      | Phone      | Address           | Fees Paid?
   ----------------------------------------------------------------------------------------------------
   john_doe     | John Doe                | Male   | 123456789012 | 9876543210 | 123 Main St       | Yes
   ```

3. **Late Return**  
   ```
   (!) Book returned 5 days late. Fine imposed: ₹5
   (<) Book '1984' returned successfully.
   ```

---

### **Limitations & Improvements**
1. **Data Persistence**:  
   - All data is stored in memory (lost on program exit).  
   - **Fix**: Integrate a database (e.g., SQLite/MySQL).  

2. **Security**:  
   - Passwords stored in plain text.  
   - **Fix**: Use encryption (e.g., BCrypt).  

3. **UI**:  
   - Console-based (no GUI).  
   - **Fix**: Build a Swing/JavaFX interface.  

4. **Scalability**:  
   - No support for multiple librarians.  
   - **Fix**: Add role-based access control.  

---

### **How to Run**
1. Compile:  
   ```bash
   javac LibraryManagement.java
   ```
2. Execute:  
   ```bash
   java LibraryManagement
   ```

This code provides a **foundation** for a library system. Extend it by adding features like email notifications, book reservations, or a GUI! 🚀
