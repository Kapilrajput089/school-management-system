// ick nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.time.Year;
import static javax.swing.SwingConstants.CENTER;

/**
 * Fee and Salary Management System - Main Application Class
 * This class contains the entire colorful and functional Management System
 * with modules for both Student Fees and Teacher Salaries, connected to a MySQL database.
 * To run the project, you must:
 * 1. Have a MySQL server running.
 * 2. Create a database (e.g., 'school_management1').
 * 3. Add the MySQL Connector/J JAR to your project's build path.
 * 4. Update the DB_USER and DB_PASS in the DatabaseConnection class.
 */
public class FeeManagementSystem {

    // --- Main Entry Point ---
    public static void main(String[] args) {
        // Initialize the database and create tables if they don't exist
        DatabaseConnection.initializeDatabase();
        
        // Run the GUI creation on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            // Set a modern Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}

// --- Database Connection Class for MySQL ---
class DatabaseConnection {
    // Connection details for MySQL Server
    private static final String DB_URL = "jdbc:mysql://localhost:3306/school_management"; // <-- CHANGE if your database name is different
    private static final String DB_USER = "root"; // <-- CHANGE to your MySQL username
    private static final String DB_PASS = "1234"; // <-- CHANGE to your MySQL password

    public static Connection connect() {
        Connection conn = null;
        try {
            // Ensure the MySQL JDBC driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Database connection failed. Please check connection details and ensure MySQL server is running.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }

    public static void initializeDatabase() {
        // Admins table
        String adminTableSql = "CREATE TABLE IF NOT EXISTS admins (\n"
                + " email VARCHAR(255) PRIMARY KEY,\n"
                + " password VARCHAR(255) NOT NULL,\n"
                + " school_code VARCHAR(255) NOT NULL UNIQUE\n"
                + ");";

        String studentTableSql = "CREATE TABLE IF NOT EXISTS students (\n"
                + " id VARCHAR(255) PRIMARY KEY,\n"
                + " name VARCHAR(255) NOT NULL,\n"
                + " father_name VARCHAR(255),\n"
                + " mother_name VARCHAR(255),\n"
                + " class_name VARCHAR(255),\n"
                + " total_fees DOUBLE,\n"
                + " paid_fees DOUBLE DEFAULT 0\n"
                + ");";

        String teacherTableSql = "CREATE TABLE IF NOT EXISTS teachers (\n"
                + " id VARCHAR(255) PRIMARY KEY,\n"
                + " name VARCHAR(255) NOT NULL,\n"
                + " subject VARCHAR(255),\n"
                + " total_salary DOUBLE,\n"
                + " paid_salary DOUBLE DEFAULT 0\n"
                + ");";
        
        String paymentTableSql = "CREATE TABLE IF NOT EXISTS payments (\n"
                + " receipt_number VARCHAR(255) PRIMARY KEY,\n"
                + " person_id VARCHAR(255) NOT NULL, \n"
                + " amount DOUBLE, \n"
                + " payment_date DATETIME, \n"
                + " payment_mode VARCHAR(255) \n"
                + ");";
        
        // Student History Table for archiving records
        String studentHistorySql = "CREATE TABLE IF NOT EXISTS student_history (\n"
                + " history_id INT AUTO_INCREMENT PRIMARY KEY, \n"
                + " student_id VARCHAR(255), \n"
                + " name VARCHAR(255), \n"
                + " father_name VARCHAR(255), \n"
                + " mother_name VARCHAR(255), \n"
                + " class_name VARCHAR(255), \n"
                + " academic_year VARCHAR(255), \n"
                + " total_fees DOUBLE, \n"
                + " paid_fees DOUBLE, \n"
                + " archive_date DATETIME, \n"
                + " reason VARCHAR(255) \n"
                + ");";

        // Payment History Table for archiving payments
        String paymentHistorySql = "CREATE TABLE IF NOT EXISTS payment_history (\n"
                + " history_payment_id INT AUTO_INCREMENT PRIMARY KEY, \n"
                + " student_id VARCHAR(255), \n"
                + " receipt_number VARCHAR(255), \n"
                + " amount DOUBLE, \n"
                + " payment_date DATETIME, \n"
                + " payment_mode VARCHAR(255), \n"
                + " class_at_payment VARCHAR(255), \n"
                + " academic_year VARCHAR(255) \n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(adminTableSql); 
            stmt.execute(studentTableSql);
            stmt.execute(teacherTableSql);
            stmt.execute(paymentTableSql);
            stmt.execute(studentHistorySql);
            stmt.execute(paymentHistorySql);
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }
}


// --- Custom Color Palette ---
class CustomColors {
    static final Color PRIMARY_DARK = new Color(0, 21, 36);
    static final Color SECONDARY_DARK = new Color(1, 43, 67);
    static final Color ACCENT_COLOR = new Color(0, 123, 255);
    static final Color ACCENT_SUCCESS = new Color(0, 180, 150);
    static final Color ACCENT_DANGER = new Color(255, 80, 100);
    static final Color TEXT_LIGHT = new Color(240, 245, 249);
    static final Color BORDER_COLOR = new Color(70, 80, 90);
    static final Color BUTTON_BACKGROUND = Color.WHITE; 
    static final Color BUTTON_TEXT_BLUE = new Color(13, 108, 210); 
}

// --- Data Model for a Payment ---
class Payment {
    private final Date date;
    private final double amount;
    private final String receiptNumber;
    private final String paymentMode;

    public Payment(double amount, String receiptNumber, String paymentMode, Date date) {
        this.date = date;
        this.amount = amount;
        this.receiptNumber = receiptNumber;
        this.paymentMode = paymentMode;
    }

    public Date getDate() { return date; }
    public double getAmount() { return amount; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getPaymentMode() { return paymentMode; }
}


// --- Data Model for a Student ---
class Student {
    private final String id;
    private String name;
    private String fatherName;
    private String motherName;
    private String className;
    private double totalFees;
    private double paidFees;
    private final List<Payment> paymentHistory;

    public Student(String id, String name, String fatherName, String motherName, String className, double totalFees) {
        this.id = id;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.className = className;
        this.totalFees = totalFees;
        this.paidFees = 0;
        this.paymentHistory = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public double getTotalFees() { return totalFees; }
    public void setTotalFees(double totalFees) { this.totalFees = totalFees; }
    public double getPaidFees() { return paidFees; }
    public void setPaidFees(double paidFees) { this.paidFees = paidFees; }
    public double getDueFees() { return totalFees - paidFees; }
    public List<Payment> getPaymentHistory() { return paymentHistory; }
    
    public String getStatus() {
        if (getDueFees() <= 0) return "Paid";
        if (paidFees > 0) return "Partial";
        return "Unpaid";
    }
}

// --- Data Model for Historical Student Record ---
class StudentHistoryRecord {
    private final String studentId;
    private final String name;
    private final String fatherName;
    private final String motherName;
    private final String className;
    private final String academicYear;
    private final double totalFees;
    private final double paidFees;
    private final String reason;

    public StudentHistoryRecord(String studentId, String name, String fatherName, String motherName, String className, String academicYear, double totalFees, double paidFees, String reason) {
        this.studentId = studentId;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.className = className;
        this.academicYear = academicYear;
        this.totalFees = totalFees;
        this.paidFees = paidFees;
        this.reason = reason;
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getFatherName() { return fatherName; }
    public String getMotherName() { return motherName; }
    public String getClassName() { return className; }
    public String getAcademicYear() { return academicYear; }
    public double getTotalFees() { return totalFees; }
    public double getPaidFees() { return paidFees; }
    public String getReason() { return reason; }
    public double getDueFees() { return totalFees - paidFees; }
}


// --- Data Model for a Teacher ---
class Teacher {
    private final String id;
    private String name;
    private String subject;
    private double totalSalary;
    private double paidSalary;
    private final List<Payment> paymentHistory;

    public Teacher(String id, String name, String subject, double totalSalary) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.totalSalary = totalSalary;
        this.paidSalary = 0;
        this.paymentHistory = new ArrayList<>();
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }
    public double getPaidSalary() { return paidSalary; }
    public void setPaidSalary(double paidSalary) { this.paidSalary = paidSalary; }
    public double getDueSalary() { return totalSalary - paidSalary; }
    public List<Payment> getPaymentHistory() { return paymentHistory; }
    
    public String getStatus() {
        if (getDueSalary() <= 0) return "Paid";
        if (paidSalary > 0) return "Partial";
        return "Unpaid";
    }
}


// --- DataManager Class for Database Interaction ---
class DataManager {
    
    // --- Admin Methods ---
    public static int registerAdmin(String schoolCode, String email, String password) {
        String checkEmailSql = "SELECT email FROM admins WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(checkEmailSql)) {
            pstmt.setString(1, email);
            if (pstmt.executeQuery().next()) {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking admin email: " + e.getMessage());
            return -2;
        }

        String checkCodeSql = "SELECT school_code FROM admins WHERE school_code = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(checkCodeSql)) {
            pstmt.setString(1, schoolCode);
            if (pstmt.executeQuery().next()) {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error checking school code: " + e.getMessage());
            return -2;
        }

        String insertSql = "INSERT INTO admins(school_code, email, password) VALUES(?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, schoolCode);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println("Error registering admin: " + e.getMessage());
            return -2;
        }
    }

    public static boolean validateLogin(String email, String password) {
        String sql = "SELECT * FROM admins WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error validating login: " + e.getMessage());
            return false;
        }
    }


    // --- Student Methods ---
    public static List<Student> getAllStudents() {
        String sql = "SELECT * FROM students";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("father_name"),
                        rs.getString("mother_name"),
                        rs.getString("class_name"),
                        rs.getDouble("total_fees")
                );
                student.setPaidFees(rs.getDouble("paid_fees"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching students: " + e.getMessage());
        }
        return students;
    }

    public static void addStudent(Student student) {
        String sql = "INSERT INTO students(id, name, father_name, mother_name, class_name, total_fees, paid_fees) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getFatherName());
            pstmt.setString(4, student.getMotherName());
            pstmt.setString(5, student.getClassName());
            pstmt.setDouble(6, student.getTotalFees());
            pstmt.setDouble(7, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }
    
    public static void updateStudent(String id, String newName, String newFatherName, String newMotherName, String newClass, double newTotalFees) {
        String sql = "UPDATE students SET name = ?, father_name = ?, mother_name = ?, class_name = ?, total_fees = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newFatherName);
            pstmt.setString(3, newMotherName);
            pstmt.setString(4, newClass);
            pstmt.setDouble(5, newTotalFees);
            pstmt.setString(6, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating student: " + e.getMessage());
        }
    }

    public static void deleteStudent(String id) {
        if (!archiveStudent(id, "Deleted")) {
            System.out.println("Archiving failed. Deletion aborted.");
            return;
        }

        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            deletePaymentHistory(id);
        } catch (SQLException e) {
            System.out.println("Error deleting student: " + e.getMessage());
        }
    }
    
    public static void promoteStudent(String studentId, String newClassName, double newTotalFees) {
        if (!archiveStudent(studentId, "Promoted")) {
            System.out.println("Archiving failed. Promotion aborted.");
            return;
        }
        
        String sql = "UPDATE students SET class_name = ?, total_fees = ?, paid_fees = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newClassName);
            pstmt.setDouble(2, newTotalFees);
            pstmt.setString(3, studentId);
            pstmt.executeUpdate();
            deletePaymentHistory(studentId);
        } catch (SQLException e) {
            System.out.println("Error promoting student: " + e.getMessage());
        }
    }

    private static boolean archiveStudent(String studentId, String reason) {
        Student student = findStudentById(studentId);
        if (student == null) return false;

        String academicYear = String.valueOf(Year.now().getValue());

        String archiveStudentSql = "INSERT INTO student_history (student_id, name, father_name, mother_name, class_name, academic_year, total_fees, paid_fees, archive_date, reason) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(archiveStudentSql)) {
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getFatherName());
            pstmt.setString(4, student.getMotherName());
            pstmt.setString(5, student.getClassName());
            pstmt.setString(6, academicYear);
            pstmt.setDouble(7, student.getTotalFees());
            pstmt.setDouble(8, student.getPaidFees());
            pstmt.setTimestamp(9, new java.sql.Timestamp(new Date().getTime()));
            pstmt.setString(10, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error archiving student record: " + e.getMessage());
            return false;
        }

        List<Payment> payments = getPaymentHistoryForPerson(studentId);
        String archivePaymentSql = "INSERT INTO payment_history (student_id, receipt_number, amount, payment_date, payment_mode, class_at_payment, academic_year) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(archivePaymentSql)) {
            for (Payment p : payments) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, p.getReceiptNumber());
                pstmt.setDouble(3, p.getAmount());
                pstmt.setTimestamp(4, new java.sql.Timestamp(p.getDate().getTime()));
                pstmt.setString(5, p.getPaymentMode());
                pstmt.setString(6, student.getClassName());
                pstmt.setString(7, academicYear);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error archiving payment history: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static void makeStudentPayment(String studentId, double amount, String mode) {
        Student student = findStudentById(studentId);
        if (student != null) {
            double newPaidAmount = student.getPaidFees() + amount;
            String updateSql = "UPDATE students SET paid_fees = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, newPaidAmount);
                pstmt.setString(2, studentId);
                pstmt.executeUpdate();
                
                String receiptNum = getNextTransactionId("RCPT-");
                addPayment(new Payment(amount, receiptNum, mode, new Date()), studentId);

            } catch (SQLException e) {
                System.out.println("Error making payment: " + e.getMessage());
            }
        }
    }

    public static Student findStudentById(String id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            ResultSet rs  = pstmt.executeQuery();
            if (rs.next()) {
                 Student student = new Student(
                         rs.getString("id"),
                         rs.getString("name"),
                         rs.getString("father_name"),
                         rs.getString("mother_name"),
                         rs.getString("class_name"),
                         rs.getDouble("total_fees")
                 );
                 student.setPaidFees(rs.getDouble("paid_fees"));
                 return student;
            }
        } catch (SQLException e) {
            System.out.println("Error finding student: " + e.getMessage());
        }
        return null;
    }
    
    // --- Teacher Methods ---
    public static List<Teacher> getAllTeachers() {
        String sql = "SELECT * FROM teachers";
        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
                Teacher teacher = new Teacher(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("subject"),
                        rs.getDouble("total_salary")
                );
                teacher.setPaidSalary(rs.getDouble("paid_salary"));
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching teachers: " + e.getMessage());
        }
        return teachers;
    }

    public static void addTeacher(Teacher teacher) {
         String sql = "INSERT INTO teachers(id, name, subject, total_salary, paid_salary) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getId());
            pstmt.setString(2, teacher.getName());
            pstmt.setString(3, teacher.getSubject());
            pstmt.setDouble(4, teacher.getTotalSalary());
            pstmt.setDouble(5, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding teacher: " + e.getMessage());
        }
    }
    
    public static void updateTeacher(String id, String newName, String newSubject, double newSalary) {
        String sql = "UPDATE teachers SET name = ?, subject = ?, total_salary = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newSubject);
            pstmt.setDouble(3, newSalary);
            pstmt.setString(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating teacher: " + e.getMessage());
        }
    }
    
    public static void deleteTeacher(String id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            deletePaymentHistory(id);
        } catch (SQLException e) {
            System.out.println("Error deleting teacher: " + e.getMessage());
        }
    }
    
    public static void payTeacherSalary(String teacherId, double amount, String mode) {
        Teacher teacher = findTeacherById(teacherId);
        if (teacher != null) {
            double newPaidAmount = teacher.getPaidSalary() + amount;
            String updateSql = "UPDATE teachers SET paid_salary = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, newPaidAmount);
                pstmt.setString(2, teacherId);
                pstmt.executeUpdate();
                
                String receiptNum = getNextTransactionId("SAL-");
                addPayment(new Payment(amount, receiptNum, mode, new Date()), teacherId);

            } catch (SQLException e) {
                System.out.println("Error paying salary: " + e.getMessage());
            }
        }
    }

    public static Teacher findTeacherById(String id) {
        String sql = "SELECT * FROM teachers WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            ResultSet rs  = pstmt.executeQuery();
            if (rs.next()) {
                Teacher teacher = new Teacher(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("subject"),
                        rs.getDouble("total_salary")
                );
                teacher.setPaidSalary(rs.getDouble("paid_salary"));
                return teacher;
            }
        } catch (SQLException e) {
            System.out.println("Error finding teacher: " + e.getMessage());
        }
        return null;
    }

    // --- Payment History Methods ---
    private static void addPayment(Payment payment, String personId) {
        String sql = "INSERT INTO payments(receipt_number, person_id, amount, payment_date, payment_mode) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, payment.getReceiptNumber());
            pstmt.setString(2, personId);
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setTimestamp(4, new java.sql.Timestamp(payment.getDate().getTime()));
            pstmt.setString(5, payment.getPaymentMode());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding payment: " + e.getMessage());
        }
    }

    public static List<Payment> getPaymentHistoryForPerson(String personId) {
        String sql = "SELECT * FROM payments WHERE person_id = ? ORDER BY payment_date DESC";
        List<Payment> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, personId);
            ResultSet rs  = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new Payment(
                        rs.getDouble("amount"),
                        rs.getString("receipt_number"),
                        rs.getString("payment_mode"),
                        rs.getTimestamp("payment_date")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching payment history: " + e.getMessage());
        }
        return history;
    }

    private static void deletePaymentHistory(String personId) {
        String sql = "DELETE FROM payments WHERE person_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, personId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting payment history: " + e.getMessage());
        }
    }

    private static String getNextTransactionId(String prefix) {
        String sql = "SELECT COUNT(*) FROM payments WHERE receipt_number LIKE ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return prefix + String.format("%06d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            System.out.println("Error getting next transaction ID: " + e.getMessage());
        }
        return prefix + "000001";
    }

    // --- Methods for fetching archived data ---
    public static List<String> getDistinctAcademicYears() {
        String sql = "SELECT DISTINCT academic_year FROM student_history ORDER BY academic_year DESC";
        List<String> years = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                years.add(rs.getString("academic_year"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching academic years: " + e.getMessage());
        }
        return years;
    }

    public static List<StudentHistoryRecord> getStudentHistoryByYear(String year) {
        String sql = "SELECT * FROM student_history WHERE academic_year = ?";
        List<StudentHistoryRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, year);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new StudentHistoryRecord(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("father_name"),
                        rs.getString("mother_name"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getDouble("total_fees"),
                        rs.getDouble("paid_fees"),
                        rs.getString("reason")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student history by year: " + e.getMessage());
        }
        return records;
    }


    public static String getNextStudentId() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            if (rs.next()) {
                return "SID" + String.format("%03d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "SID001";
    }

    public static String getNextTeacherId() {
        String sql = "SELECT COUNT(*) FROM teachers";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            if (rs.next()) {
                return "TID" + String.format("%03d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "TID001";
    }
}


// --- Login Frame ---
class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Fee Management - Login");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.PRIMARY_DARK);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CustomColors.PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel creatorLabel = new JLabel("Created by: Kapil Verma");
        creatorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        creatorLabel.setForeground(Color.LIGHT_GRAY);
        creatorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(creatorLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("School Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(CustomColors.TEXT_LIGHT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.PRIMARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Email");
        userLabel.setForeground(CustomColors.TEXT_LIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        JTextField userField = createStyledTextField("Enter your email");
        gbc.gridx = 1;
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(CustomColors.TEXT_LIGHT);
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passLabel, gbc);

        JPasswordField passField = createStyledPasswordField("Enter your password");
        gbc.gridx = 1;
        formPanel.add(passField, gbc);

        JButton loginButton = createStyledButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 5, 10, 5);
        formPanel.add(loginButton, gbc);

        JLabel registerLabel = new JLabel("<html>Don't have an account? <font color='#0096C7'>Register</font></html>");
        registerLabel.setForeground(CustomColors.TEXT_LIGHT);
        registerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 3;
        formPanel.add(registerLabel, gbc);

        add(formPanel, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(CustomColors.PRIMARY_DARK);
        footerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        JButton exitButton = createSmallDangerButton("Exit");
        footerPanel.add(exitButton);
        add(footerPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        userField.addActionListener(e -> passField.requestFocusInWindow());
        passField.addActionListener(e -> loginButton.doClick());

        loginButton.addActionListener(e -> {
            String email = userField.getText();
            String password = new String(passField.getPassword());

            if (email.isEmpty() || password.isEmpty() || email.equals("Enter your email")) {
                JOptionPane.showMessageDialog(this, "Email and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataManager.validateLogin(email, password)) {
                dispose();
                new DashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(LoginFrame.this,
                    "Are you sure you want to exit the application?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame(LoginFrame.this).setVisible(true);
            }
        });
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textField.setBackground(CustomColors.SECONDARY_DARK);
        textField.setForeground(CustomColors.TEXT_LIGHT);
        textField.setCaretColor(CustomColors.ACCENT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, CustomColors.BORDER_COLOR),
                new EmptyBorder(5, 5, 5, 5)
        ));
        addPlaceholderStyle(textField, placeholder);
        return textField;
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passField.setBackground(CustomColors.SECONDARY_DARK);
        passField.setForeground(CustomColors.TEXT_LIGHT);
        passField.setCaretColor(CustomColors.ACCENT_COLOR);
        passField.setEchoChar((char) 0);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, CustomColors.BORDER_COLOR),
                new EmptyBorder(5, 5, 5, 5)
        ));
        addPlaceholderStyle(passField, placeholder);
        return passField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(Color.white);
        button.setForeground(CustomColors.BUTTON_TEXT_BLUE); 
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
               button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
               button.setBackground(Color.WHITE);
               button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
        return button;
    }
    
    private JButton createSmallDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(CustomColors.BUTTON_BACKGROUND);
        button.setForeground(CustomColors.ACCENT_DANGER);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_DANGER);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(CustomColors.BUTTON_BACKGROUND);
                button.setForeground(CustomColors.ACCENT_DANGER);
            }
        });
        return button;
    }

    private void addPlaceholderStyle(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(CustomColors.TEXT_LIGHT);
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar('●');
                    }
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar((char) 0);
                    }
                }
            }
        });
    }
}

// --- Register Frame ---
class RegisterFrame extends JDialog {
    public RegisterFrame(Frame owner) {
        super(owner, "Admin Registration", true);
        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField schoolCodeField = createFormField("School Code");
        JTextField emailField = createFormField("Email Address");
        JPasswordField passwordField = createPasswordField("Password");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("School Code:"), gbc);
        gbc.gridx = 1; formPanel.add(schoolCodeField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; formPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(registerButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);

        add(new JLabel("Create New Admin Account"){{
            setFont(new Font("Segoe UI", Font.BOLD, 24));
            setForeground(CustomColors.TEXT_LIGHT);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(20,0,10,0));
        }}, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Enter Key Navigation ---
        schoolCodeField.addActionListener(e -> emailField.requestFocusInWindow());
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> registerButton.doClick());

        registerButton.addActionListener(e -> {
            String schoolCode = schoolCodeField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (schoolCode.isEmpty() || email.isEmpty() || password.isEmpty() || schoolCode.equals("School Code")) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int result = DataManager.registerAdmin(schoolCode, email, password);
            switch (result) {
                case 1:
                    JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    break;
                case 0:
                    JOptionPane.showMessageDialog(this, "Registration failed. Email is already in use.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case -1:
                    JOptionPane.showMessageDialog(this, "Registration failed. School Code is already registered.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "An unexpected database error occurred.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setEchoChar((char)0);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('●');
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char)0);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(CustomColors.BUTTON_TEXT_BLUE);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}


// --- Main Dashboard Frame ---
class DashboardFrame extends JFrame {
    private DefaultTableModel studentTableModel;
    private JTable studentTable;
    private TableRowSorter<DefaultTableModel> studentSorter;
    private JLabel totalStudentsLabel, feesCollectedLabel, duesLabel;
    private JButton editStudentBtn, deleteStudentBtn, printReceiptBtn, viewHistoryBtn, promoteStudentBtn;
    private JTextField studentSearchField;

    private DefaultTableModel teacherTableModel;
    private JTable teacherTable;
    private TableRowSorter<DefaultTableModel> teacherSorter;
    private JLabel totalTeachersLabel, salaryPaidLabel, salaryDueLabel;
    private JButton addTeacherBtn, editTeacherBtn, deleteTeacherBtn, paySalaryBtn, viewTeacherHistoryBtn;
    private JTextField teacherSearchField;

    public DashboardFrame() {
        setTitle("School Management Dashboard");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.PRIMARY_DARK);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CustomColors.SECONDARY_DARK);
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel titleLabel = new JLabel("       Welcome To P.N Bal Mandir Junior High School. ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(CustomColors.TEXT_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(CustomColors.BUTTON_TEXT_BLUE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                logoutButton.setForeground(Color.WHITE);
               logoutButton.setBackground(CustomColors.ACCENT_COLOR);
            }
            public void mouseExited(MouseEvent evt) {
                logoutButton.setBackground(Color.WHITE);
                   logoutButton.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(DashboardFrame.this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabbedPane.addTab("<html><body style='padding: 5px 15px;'>Student Fee Management</body></html>", createStudentPanel());
        tabbedPane.addTab("<html><body style='padding: 5px 15px;'>Teacher Salary Management</body></html>", createTeacherPanel());
        add(tabbedPane, BorderLayout.CENTER);
        
        refreshStudentData();
        refreshTeacherData();
    }

    private JPanel createStudentPanel() {
        JPanel studentPanel = new JPanel(new BorderLayout(10, 10));
        studentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        studentPanel.setBackground(CustomColors.PRIMARY_DARK);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(CustomColors.PRIMARY_DARK);
        totalStudentsLabel = new JLabel();
        feesCollectedLabel = new JLabel();
        duesLabel = new JLabel();
        statsPanel.add(createStatCard("Total Students", totalStudentsLabel, CustomColors.ACCENT_COLOR));
        statsPanel.add(createStatCard("Total Fees Collected", feesCollectedLabel, CustomColors.ACCENT_SUCCESS));
        statsPanel.add(createStatCard("Total Dues Remaining", duesLabel, CustomColors.ACCENT_DANGER));
        studentPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(CustomColors.SECONDARY_DARK);
        tableContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10), "Active Student Records", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Segoe UI", Font.BOLD, 16), CustomColors.TEXT_LIGHT));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(CustomColors.SECONDARY_DARK);
        studentSearchField = new JTextField(30);
        studentSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton searchBtn = createStyledMiniButton("🔍 Search");
        JButton clearBtn = createStyledMiniButton("Clear");

        searchPanel.add(new JLabel("Search by Name or ID: "){{setForeground(CustomColors.TEXT_LIGHT);}});
        searchPanel.add(studentSearchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        tableContainer.add(searchPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"ID", "Name", "Father's Name", "Class", "Total Fees", "Paid Fees", "Due Fees", "Status"};
        studentTableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        studentTable = new JTable(studentTableModel);
        studentSorter = new TableRowSorter<>(studentTableModel);
        studentTable.setRowSorter(studentSorter);
        styleTable(studentTable);
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(CustomColors.SECONDARY_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        studentPanel.add(tableContainer, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionsPanel.setBackground(CustomColors.PRIMARY_DARK);
        actionsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton addStudentBtn = createActionButton("Add Student");
        editStudentBtn = createActionButton("Edit Student");
        promoteStudentBtn = createActionButton("Promote Student");
        deleteStudentBtn = createActionButton("Delete Student");
        JButton makePaymentBtn = createActionButton("Make Payment");
        viewHistoryBtn = createActionButton("View History");
        printReceiptBtn = createActionButton("Print Receipt");
        JButton viewAllRecordsBtn = createActionButton("View Archived Records");
        JButton refreshBtn = createActionButton("Refresh");

        editStudentBtn.setEnabled(false);
        deleteStudentBtn.setEnabled(false);
        printReceiptBtn.setEnabled(false);
        viewHistoryBtn.setEnabled(false);
        promoteStudentBtn.setEnabled(false);

        actionsPanel.add(addStudentBtn);
        actionsPanel.add(editStudentBtn);
        actionsPanel.add(promoteStudentBtn);
        actionsPanel.add(deleteStudentBtn);
        actionsPanel.add(makePaymentBtn);
        actionsPanel.add(viewHistoryBtn);
        actionsPanel.add(printReceiptBtn);
        actionsPanel.add(viewAllRecordsBtn);
        actionsPanel.add(refreshBtn);
        
        studentPanel.add(actionsPanel, BorderLayout.SOUTH);

        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = studentTable.getSelectedRow() != -1;
                editStudentBtn.setEnabled(isSelected);
                deleteStudentBtn.setEnabled(isSelected);
                printReceiptBtn.setEnabled(isSelected);
                viewHistoryBtn.setEnabled(isSelected);
                promoteStudentBtn.setEnabled(isSelected);
            }
        });
        
        searchBtn.addActionListener(e -> {
            String text = studentSearchField.getText();
            if (text.trim().length() == 0) studentSorter.setRowFilter(null);
            else studentSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1, 2));
        });
        
        clearBtn.addActionListener(e -> {
            studentSearchField.setText("");
            studentSorter.setRowFilter(null);
        });
        
        addStudentBtn.addActionListener(e -> {
            new AddStudentDialog(this).setVisible(true);
            refreshStudentData();
        });
        
        editStudentBtn.addActionListener(e -> {
            int selectedRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
            String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
            Student student = DataManager.findStudentById(studentId);
            if (student != null) {
                new EditStudentDialog(this, student).setVisible(true);
                refreshStudentData();
            }
        });
        
        deleteStudentBtn.addActionListener(e -> {
            int selectedRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
            String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
            String studentName = (String) studentTableModel.getValueAt(selectedRow, 1);
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete student '" + studentName + "'?\nTheir record will be archived.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                DataManager.deleteStudent(studentId);
                refreshStudentData();
            }
        });
        
        makePaymentBtn.addActionListener(e -> {
            new MakePaymentDialog(this).setVisible(true);
            refreshStudentData();
        });
        
        viewHistoryBtn.addActionListener(e -> {
            int selectedRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
            String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
            Student student = DataManager.findStudentById(studentId);
            if (student != null) new PaymentHistoryDialog(this, student).setVisible(true);
        });
        
        printReceiptBtn.addActionListener(e -> {
            int selectedRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
            String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
            Student student = DataManager.findStudentById(studentId);
            if (student != null) new ReceiptDialog(this, student).setVisible(true);
        });

        promoteStudentBtn.addActionListener(e -> {
            int selectedRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
            String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
            Student student = DataManager.findStudentById(studentId);
            if (student != null) {
                new PromoteStudentDialog(this, student).setVisible(true);
                refreshStudentData();
            }
        });

        viewAllRecordsBtn.addActionListener(e -> {
            new AllRecordsDialog(this).setVisible(true);
        });

        refreshBtn.addActionListener(e -> refreshStudentData());

        return studentPanel;
    }
    
    private JPanel createTeacherPanel() {
        JPanel teacherPanel = new JPanel(new BorderLayout(10, 10));
        teacherPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        teacherPanel.setBackground(CustomColors.PRIMARY_DARK);
    
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(CustomColors.PRIMARY_DARK);
        totalTeachersLabel = new JLabel();
        salaryPaidLabel = new JLabel();
        salaryDueLabel = new JLabel();
        statsPanel.add(createStatCard("Total Teachers", totalTeachersLabel, CustomColors.ACCENT_COLOR));
        statsPanel.add(createStatCard("Total Salary Paid", salaryPaidLabel, CustomColors.ACCENT_SUCCESS));
        statsPanel.add(createStatCard("Total Salary Due", salaryDueLabel, CustomColors.ACCENT_DANGER));
        teacherPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(CustomColors.SECONDARY_DARK);
        tableContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10), "Teacher Records",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 16), CustomColors.TEXT_LIGHT));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(CustomColors.SECONDARY_DARK);
        teacherSearchField = new JTextField(30);
        teacherSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton searchBtn = createStyledMiniButton("🔍 Search");
        JButton clearBtn = createStyledMiniButton("Clear");
        searchPanel.add(new JLabel("Search by Name or ID: "){{setForeground(CustomColors.TEXT_LIGHT);}});
        searchPanel.add(teacherSearchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        tableContainer.add(searchPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Name", "Subject", "Total Salary", "Paid Salary", "Due Salary", "Status"};
        teacherTableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        teacherTable = new JTable(teacherTableModel);
        teacherSorter = new TableRowSorter<>(teacherTableModel);
        teacherTable.setRowSorter(teacherSorter);
        styleTable(teacherTable);

        JScrollPane scrollPane = new JScrollPane(teacherTable);
        scrollPane.getViewport().setBackground(CustomColors.SECONDARY_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        teacherPanel.add(tableContainer, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionsPanel.setBackground(CustomColors.PRIMARY_DARK);
        actionsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        addTeacherBtn = createActionButton("Add Teacher");
        editTeacherBtn = createActionButton("Edit Teacher");
        deleteTeacherBtn = createActionButton("Delete Teacher");
        paySalaryBtn = createActionButton("Pay Salary");
        viewTeacherHistoryBtn = createActionButton("View History");
        JButton refreshBtn = createActionButton("Refresh");
        editTeacherBtn.setEnabled(false);
        deleteTeacherBtn.setEnabled(false);
        viewTeacherHistoryBtn.setEnabled(false);
        actionsPanel.add(addTeacherBtn);
        actionsPanel.add(editTeacherBtn);
        actionsPanel.add(deleteTeacherBtn);
        actionsPanel.add(paySalaryBtn);
        actionsPanel.add(viewTeacherHistoryBtn);
        actionsPanel.add(refreshBtn);
        teacherPanel.add(actionsPanel, BorderLayout.SOUTH);

        teacherTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = teacherTable.getSelectedRow() != -1;
                editTeacherBtn.setEnabled(isSelected);
                deleteTeacherBtn.setEnabled(isSelected);
                viewTeacherHistoryBtn.setEnabled(isSelected);
            }
        });
        searchBtn.addActionListener(e -> {
            String text = teacherSearchField.getText();
            if (text.trim().length() == 0) teacherSorter.setRowFilter(null);
            else teacherSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
        });
        clearBtn.addActionListener(e -> {
            teacherSearchField.setText("");
            teacherSorter.setRowFilter(null);
        });
        addTeacherBtn.addActionListener(e -> {
            new AddTeacherDialog(this).setVisible(true);
            refreshTeacherData();
        });
        editTeacherBtn.addActionListener(e -> {
            int selectedRow = teacherTable.convertRowIndexToModel(teacherTable.getSelectedRow());
            String teacherId = (String) teacherTableModel.getValueAt(selectedRow, 0);
            Teacher teacher = DataManager.findTeacherById(teacherId);
            if (teacher != null) {
                new EditTeacherDialog(this, teacher).setVisible(true);
                refreshTeacherData();
            }
        });
        deleteTeacherBtn.addActionListener(e -> {
            int selectedRow = teacherTable.convertRowIndexToModel(teacherTable.getSelectedRow());
            String teacherId = (String) teacherTableModel.getValueAt(selectedRow, 0);
            String teacherName = (String) teacherTableModel.getValueAt(selectedRow, 1);
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete teacher '" + teacherName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                DataManager.deleteTeacher(teacherId);
                refreshTeacherData();
            }
        });
        paySalaryBtn.addActionListener(e -> {
            new PaySalaryDialog(this).setVisible(true);
            refreshTeacherData();
        });
        viewTeacherHistoryBtn.addActionListener(e -> {
            int selectedRow = teacherTable.convertRowIndexToModel(teacherTable.getSelectedRow());
            String teacherId = (String) teacherTableModel.getValueAt(selectedRow, 0);
            Teacher teacher = DataManager.findTeacherById(teacherId);
            if (teacher != null) new SalaryHistoryDialog(this, teacher).setVisible(true);
        });
        refreshBtn.addActionListener(e -> refreshTeacherData());

        return teacherPanel;
    }
    
    private void styleTable(JTable table) {
        table.setBackground(CustomColors.SECONDARY_DARK);
        table.setForeground(CustomColors.TEXT_LIGHT);
        table.setGridColor(CustomColors.BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(CustomColors.ACCENT_COLOR);
        table.setSelectionForeground(CustomColors.TEXT_LIGHT);

        JTableHeader header = table.getTableHeader();
        header.setBackground(CustomColors.PRIMARY_DARK);
        header.setForeground(CustomColors.ACCENT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBorder(BorderFactory.createLineBorder(CustomColors.BORDER_COLOR));

        if (table == studentTable || table == teacherTable) {
             table.getColumn("Status").setCellRenderer(new StatusCellRenderer());
        }
    }
    
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 12, 10, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(CustomColors.ACCENT_COLOR);
                    button.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(MouseEvent evt) {
                 if (button.isEnabled()) {
                    button.setBackground(Color.WHITE);
                    button.setForeground(CustomColors.ACCENT_COLOR);
                 }
            }
        });
        return button;
    }

    private JButton createStyledMiniButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
        return button;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CustomColors.SECONDARY_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(CustomColors.TEXT_LIGHT);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void refreshStudentData() {
        int selectedRow = -1;
        if (studentTable != null && studentTable.getSelectedRow() != -1) {
            selectedRow = studentTable.getSelectedRow();
        }
        
        studentTableModel.setRowCount(0);

        List<Student> students = DataManager.getAllStudents();
        double totalCollected = 0;
        double totalDues = 0;

        for (Student s : students) {
            Vector<Object> row = new Vector<>();
            row.add(s.getId());
            row.add(s.getName());
            row.add(s.getFatherName());
            row.add(s.getClassName());
            row.add(String.format("₹%.2f", s.getTotalFees()));
            row.add(String.format("₹%.2f", s.getPaidFees()));
            row.add(String.format("₹%.2f", s.getDueFees()));
            row.add(s.getStatus());
            studentTableModel.addRow(row);
            
            totalCollected += s.getPaidFees();
            totalDues += s.getDueFees();
        }

        totalStudentsLabel.setText(String.valueOf(students.size()));
        feesCollectedLabel.setText(String.format("₹%,.2f", totalCollected));
        duesLabel.setText(String.format("₹%,.2f", totalDues));
        
        if (selectedRow != -1 && selectedRow < studentTable.getRowCount()) {
            studentTable.setRowSelectionInterval(selectedRow, selectedRow);
        } else if (editStudentBtn != null) {
            editStudentBtn.setEnabled(false);
            deleteStudentBtn.setEnabled(false);
            printReceiptBtn.setEnabled(false);
            viewHistoryBtn.setEnabled(false);
            promoteStudentBtn.setEnabled(false);
        }
    }
    
    private void refreshTeacherData() {
        int selectedRow = -1;
        if (teacherTable != null && teacherTable.getSelectedRow() != -1) {
            selectedRow = teacherTable.getSelectedRow();
        }
        
        teacherTableModel.setRowCount(0);

        List<Teacher> teachers = DataManager.getAllTeachers();
        double totalPaid = 0;
        double totalDue = 0;

        for (Teacher t : teachers) {
            Vector<Object> row = new Vector<>();
            row.add(t.getId());
            row.add(t.getName());
            row.add(t.getSubject());
            row.add(String.format("₹%.2f", t.getTotalSalary()));
            row.add(String.format("₹%.2f", t.getPaidSalary()));
            row.add(String.format("₹%.2f", t.getDueSalary()));
            row.add(t.getStatus());
            teacherTableModel.addRow(row);
            
            totalPaid += t.getPaidSalary();
            totalDue += t.getDueSalary();
        }

        totalTeachersLabel.setText(String.valueOf(teachers.size()));
        salaryPaidLabel.setText(String.format("₹%,.2f", totalPaid));
        salaryDueLabel.setText(String.format("₹%,.2f", totalDue));
        
        if (selectedRow != -1 && selectedRow < teacherTable.getRowCount()) {
            teacherTable.setRowSelectionInterval(selectedRow, selectedRow);
        } else if (editTeacherBtn != null) {
            editTeacherBtn.setEnabled(false);
            deleteTeacherBtn.setEnabled(false);
            viewTeacherHistoryBtn.setEnabled(false);
        }
    }
}

// --- Status Cell Renderer ---
class StatusCellRenderer extends JLabel implements TableCellRenderer {
    public StatusCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setBorder(new EmptyBorder(2, 8, 2, 8));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        
        switch (value.toString()) {
            case "Paid":
                setBackground(CustomColors.ACCENT_SUCCESS.darker());
                setForeground(Color.WHITE);
                break;
            case "Partial":
                setBackground(new Color(255, 165, 0));
                setForeground(Color.WHITE);
                break;
            case "Unpaid":
                setBackground(CustomColors.ACCENT_DANGER.darker());
                setForeground(Color.WHITE);
                break;
            default:
                setBackground(table.getBackground());
                setForeground(table.getForeground());
        }
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        }

        return this;
    }
}

// --- Add Student Dialog ---
class AddStudentDialog extends JDialog {
    public AddStudentDialog(Frame owner) {
        super(owner, "Add New Student", true);
        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = createFormField("Student Name");
        JTextField fatherNameField = createFormField("Father's Name");
        JTextField motherNameField = createFormField("Mother's Name");
        JTextField classField = createFormField("Class (e.g., 10th Grade)");
        JTextField feesField = createFormField("Total Fees (e.g., 50000)");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Father's Name:"), gbc);
        gbc.gridx = 1; formPanel.add(fatherNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Mother's Name:"), gbc);
        gbc.gridx = 1; formPanel.add(motherNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Class:"), gbc);
        gbc.gridx = 1; formPanel.add(classField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Total Fees:"), gbc);
        gbc.gridx = 1; formPanel.add(feesField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton saveButton = new JButton("Save Student");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(saveButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Enter Key Navigation ---
        nameField.addActionListener(e -> fatherNameField.requestFocusInWindow());
        fatherNameField.addActionListener(e -> motherNameField.requestFocusInWindow());
        motherNameField.addActionListener(e -> classField.requestFocusInWindow());
        classField.addActionListener(e -> feesField.requestFocusInWindow());
        feesField.addActionListener(e -> saveButton.doClick());

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String fatherName = fatherNameField.getText();
                String motherName = motherNameField.getText();
                String className = classField.getText();
                
                if (name.isEmpty() || className.isEmpty() || fatherName.isEmpty() || motherName.isEmpty() || name.equals("Student Name")) {
                    throw new IllegalArgumentException("All fields are required.");
                }

                double totalFees = Double.parseDouble(feesField.getText());
                
                String newId = DataManager.getNextStudentId();
                DataManager.addStudent(new Student(newId, name, fatherName, motherName, className, totalFees));
                JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                feesField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for fees.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- Edit Student Dialog ---
class EditStudentDialog extends JDialog {
    public EditStudentDialog(Frame owner, Student student) {
        super(owner, "Edit Student: " + student.getName(), true);
        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = createFormField(student.getName());
        JTextField fatherNameField = createFormField(student.getFatherName());
        JTextField motherNameField = createFormField(student.getMotherName());
        JTextField classField = createFormField(student.getClassName());
        JTextField feesField = createFormField(String.valueOf(student.getTotalFees()));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Father's Name:"), gbc);
        gbc.gridx = 1; formPanel.add(fatherNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Mother's Name:"), gbc);
        gbc.gridx = 1; formPanel.add(motherNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Class:"), gbc);
        gbc.gridx = 1; formPanel.add(classField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Total Fees:"), gbc);
        gbc.gridx = 1; formPanel.add(feesField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(saveButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Enter Key Navigation ---
        nameField.addActionListener(e -> fatherNameField.requestFocusInWindow());
        fatherNameField.addActionListener(e -> motherNameField.requestFocusInWindow());
        motherNameField.addActionListener(e -> classField.requestFocusInWindow());
        classField.addActionListener(e -> feesField.requestFocusInWindow());
        feesField.addActionListener(e -> saveButton.doClick());

        saveButton.addActionListener(e -> {
            try {
                String newName = nameField.getText();
                String newFatherName = fatherNameField.getText();
                String newMotherName = motherNameField.getText();
                String newClass = classField.getText();
                
                if (newName.isEmpty() || newClass.isEmpty() || newFatherName.isEmpty() || newMotherName.isEmpty()) {
                    throw new IllegalArgumentException("Fields cannot be empty.");
                }

                double newTotalFees = Double.parseDouble(feesField.getText());
                
                DataManager.updateStudent(student.getId(), newName, newFatherName, newMotherName, newClass, newTotalFees);
                JOptionPane.showMessageDialog(this, "Student updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                feesField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for fees.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String value) {
        JTextField field = new JTextField(value, 20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(CustomColors.TEXT_LIGHT);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}


// --- Make Payment Dialog ---
class MakePaymentDialog extends JDialog {
    public MakePaymentDialog(Frame owner) {
        super(owner, "Make a Payment", true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = createFormField("Enter Student ID (e.g., SID0001)");
        JTextField amountField = createFormField("Enter Amount");
        JComboBox<String> modeComboBox = new JComboBox<>(new String[]{"Cash", "Online"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; formPanel.add(amountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 1; formPanel.add(modeComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton payButton = new JButton("Process Payment");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(payButton);
        styleDialogButton(cancelButton);

        buttonPanel.add(cancelButton);
        buttonPanel.add(payButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Enter Key Navigation ---
        idField.addActionListener(e -> amountField.requestFocusInWindow());
        amountField.addActionListener(e -> payButton.doClick());

        payButton.addActionListener(e -> {
            try {
                String studentId = idField.getText();
                if (studentId.isEmpty() || studentId.equals("Enter Student ID (e.g., SID0001)")) {
                    idField.requestFocusInWindow();
                    throw new IllegalArgumentException("Student ID is required.");
                }

                double amount = Double.parseDouble(amountField.getText());
                String mode = (String) modeComboBox.getSelectedItem();
                
                Student student = DataManager.findStudentById(studentId);
                if (student == null) {
                    throw new IllegalArgumentException("Student with ID '" + studentId + "' not found.");
                }
                if (amount <= 0) {
                    throw new IllegalArgumentException("Payment amount must be positive.");
                }
                if (amount > student.getDueFees()) {
                    throw new IllegalArgumentException(String.format("Payment amount (₹%.2f) exceeds due amount (₹%.2f).", amount, student.getDueFees()));
                }
                
                DataManager.makeStudentPayment(studentId, amount, mode);
                JOptionPane.showMessageDialog(this, "Payment successful for " + student.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                amountField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- Payment History Dialog ---
class PaymentHistoryDialog extends JDialog {
    public PaymentHistoryDialog(Frame owner, Student student) {
        super(owner, "Payment History for " + student.getName(), true);
        setSize(550, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        String[] columnNames = {"Date", "Receipt No.", "Amount Paid (₹)", "Mode"};
        DefaultTableModel historyModel = new DefaultTableModel(columnNames, 0);
        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);

        List<Payment> history = DataManager.getPaymentHistoryForPerson(student.getId());

        if (history.isEmpty()) {
            historyModel.addRow(new Object[]{"No payments made yet for this class session.", "", "", ""});
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            for (Payment p : history) {
                historyModel.addRow(new Object[]{
                    sdf.format(p.getDate()),
                    p.getReceiptNumber(),
                    String.format("%.2f", p.getAmount()),
                    p.getPaymentMode()
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(CustomColors.SECONDARY_DARK);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleDialogButton(closeButton);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dispose());
    }

    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }

    private void styleTable(JTable table) {
        table.setBackground(CustomColors.SECONDARY_DARK);
        table.setForeground(CustomColors.TEXT_LIGHT);
        table.setGridColor(CustomColors.BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setBackground(CustomColors.PRIMARY_DARK);
        header.setForeground(CustomColors.ACCENT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    }
}


// --- Receipt Dialog ---
class ReceiptDialog extends JDialog {
    private final JPanel receiptPanel;

    public ReceiptDialog(Frame owner, Student student) {
        super(owner, "Fee Receipt", true);
        setSize(660, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        receiptPanel = new JPanel();
        receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
        receiptPanel.setBackground(Color.WHITE);
        receiptPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Data Fetching ---
        List<Payment> history = DataManager.getPaymentHistoryForPerson(student.getId());
        Payment latestPayment = history.isEmpty() ? null : history.get(0);

        if (latestPayment == null) {
            JOptionPane.showMessageDialog(this, "No payment history found for this student.", "Error", JOptionPane.ERROR_MESSAGE);
            // Close the dialog if there's nothing to show
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        double amountReceivedNow = latestPayment.getAmount();

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel schoolTitle = new JLabel("<html>P.N Bal Mandir Junior High School<br><span style='font-size:10pt;'>Bhojpur Nauti Atrauli (Aligarh)</span></html>");
        schoolTitle.setFont(new Font("Serif", Font.BOLD, 24));
        schoolTitle.setForeground(CustomColors.PRIMARY_DARK);

        JPanel rightHeaderPanel = new JPanel();
        rightHeaderPanel.setBackground(Color.WHITE);
        rightHeaderPanel.setLayout(new BoxLayout(rightHeaderPanel, BoxLayout.Y_AXIS));
        
        JLabel receiptTitle = new JLabel("FEE RECEIPT");
        receiptTitle.setFont(new Font("Serif", Font.BOLD, 20));
        receiptTitle.setForeground(CustomColors.ACCENT_COLOR);
        receiptTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);

        int currentYear = Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        String sessionYear;
        if (currentMonth >= 4) {
            sessionYear = currentYear + " - " + (currentYear + 1);
        } else {
            sessionYear = (currentYear - 1) + " - " + currentYear;
        }
        JLabel sessionLabel = new JLabel("Session: " + sessionYear);
        sessionLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        sessionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightHeaderPanel.add(receiptTitle);
        rightHeaderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightHeaderPanel.add(sessionLabel);

        header.add(schoolTitle, BorderLayout.WEST);
        header.add(rightHeaderPanel, BorderLayout.EAST);
        receiptPanel.add(header);

        receiptPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Meta Info (Date, Receipt No) ---
        JPanel metaPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        metaPanel.setBackground(Color.WHITE);
        metaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(createReceiptLabel("Date: "));
        datePanel.add(createReceiptValue(new SimpleDateFormat("dd-MMM-yyyy").format(latestPayment.getDate())));

        JPanel receiptNoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        receiptNoPanel.setBackground(Color.WHITE);
        
        // --- LOGIC TO DISPLAY CLEAN RECEIPT NUMBER ---
        String fullReceiptNo = latestPayment.getReceiptNumber();
        String displayNumber = fullReceiptNo; // Default to full number
        if (fullReceiptNo.contains("-")) {
            try {
                String numericString = fullReceiptNo.substring(fullReceiptNo.indexOf('-') + 1);
                int receiptInt = Integer.parseInt(numericString);
                displayNumber = String.valueOf(receiptInt);
            } catch (Exception e) {
                // If parsing fails for any reason, just display the full number
                displayNumber = fullReceiptNo;
            }
        }
        
        receiptNoPanel.add(createReceiptLabel("Receipt No: "));
        receiptNoPanel.add(createReceiptValue(displayNumber));
        
        metaPanel.add(datePanel);
        metaPanel.add(receiptNoPanel);
        receiptPanel.add(metaPanel);

        receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Student Info ---
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 2)); 
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Student Information"));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        infoPanel.add(createReceiptLabel("Student ID:"));
        infoPanel.add(createReceiptValue(student.getId()));
        infoPanel.add(createReceiptLabel("Student Name:"));
        infoPanel.add(createReceiptValue(student.getName()));
        infoPanel.add(createReceiptLabel("Father's Name:"));
        infoPanel.add(createReceiptValue(student.getFatherName()));
        infoPanel.add(createReceiptLabel("Mother's Name:"));
        infoPanel.add(createReceiptValue(student.getMotherName()));
        infoPanel.add(createReceiptLabel("Class:"));
        infoPanel.add(createReceiptValue(student.getClassName()));
        receiptPanel.add(infoPanel);

        receiptPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // --- Fee Summary Table (Transaction Based) ---
        JPanel feeSummaryPanel = new JPanel(new BorderLayout());
        feeSummaryPanel.setBackground(Color.WHITE);
        feeSummaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Fee Details"));
        String[] columnNames = {"Description", "Amount (₹)"};
        DefaultTableModel feeModel = new DefaultTableModel(columnNames, 0);
        JTable feeTable = new JTable(feeModel);
        
        feeModel.addRow(new Object[]{"Total Annual Fee", String.format("%.2f", student.getTotalFees())});
        feeModel.addRow(new Object[]{"Amount Paid", String.format("%.2f", amountReceivedNow)});
        feeModel.addRow(new Object[]{"Balance Due", String.format("%.2f", student.getDueFees())});

        styleReceiptTable(feeTable);
        feeSummaryPanel.add(feeTable.getTableHeader(), BorderLayout.NORTH);
        feeSummaryPanel.add(feeTable, BorderLayout.CENTER);
        receiptPanel.add(feeSummaryPanel);

        receiptPanel.add(Box.createVerticalGlue());

        // --- Footer (In Words, Payment Mode, Signature) ---
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        footerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel centerFooterPanel = new JPanel();
        centerFooterPanel.setLayout(new BoxLayout(centerFooterPanel, BoxLayout.Y_AXIS));
        centerFooterPanel.setBackground(Color.WHITE);
        
        String inWordsText = NumberToWordsConverter.convert((long) amountReceivedNow) + " Rupees Only";
        JLabel inWordsLabel = new JLabel("<html><b>In Words:</b> " + inWordsText + "</html>");
        inWordsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inWordsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel paymentModeLabel = new JLabel("<html><b>Payment Mode:</b> " + latestPayment.getPaymentMode() + "</html>");
        paymentModeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        paymentModeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerFooterPanel.add(inWordsLabel);
        centerFooterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerFooterPanel.add(paymentModeLabel);

        JLabel signatureLabel = new JLabel("____________________");
        JLabel signatureTitle = new JLabel("Authorized Signatory");
        signatureTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JPanel signatureBox = new JPanel();
        signatureBox.setLayout(new BoxLayout(signatureBox, BoxLayout.Y_AXIS));
        signatureBox.setBackground(Color.WHITE);
        signatureBox.add(signatureLabel);
        signatureBox.add(signatureTitle);
        
        footerPanel.add(centerFooterPanel, BorderLayout.CENTER);
        footerPanel.add(signatureBox, BorderLayout.EAST);
        receiptPanel.add(footerPanel);
        
        add(new JScrollPane(receiptPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton printButton = new JButton("🖨️ Print");
        JButton closeButton = new JButton("Close");
        
        styleDialogButton(printButton);
        styleDialogButton(closeButton);

        printButton.addActionListener(e -> printReceipt());
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        buttonPanel.add(printButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }

    private JLabel createReceiptLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JLabel createReceiptValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }
    
    private void styleReceiptTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(CustomColors.PRIMARY_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Fee Receipt");
    
        PageFormat pageFormat = job.defaultPage();
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pf, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
    
            double scaleX = pf.getImageableWidth() / receiptPanel.getWidth();
            double scaleY = pf.getImageableHeight() / receiptPanel.getHeight();
            double scale = Math.min(scaleX, scaleY); 
            
            double panelWidth = receiptPanel.getWidth() *scale;
            double panelHeight = receiptPanel.getHeight() * scale;
            double x_offset = (pf.getImageableWidth() - panelWidth) / 2;
            double y_offset = (pf.getImageableHeight() - panelHeight) / 2;
            g2d.translate(x_offset, y_offset);

            g2d.scale(scale, scale);

            receiptPanel.printAll(g2d);
            return Printable.PAGE_EXISTS;
        }, pageFormat);
    
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// --- Add Teacher Dialog ---
class AddTeacherDialog extends JDialog {
    public AddTeacherDialog(Frame owner) {
        super(owner, "Add New Teacher", true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = createFormField("Teacher Name");
        JTextField subjectField = createFormField("Subject");
        JTextField salaryField = createFormField("Total Annual Salary");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; formPanel.add(subjectField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Total Salary:"), gbc);
        gbc.gridx = 1; formPanel.add(salaryField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton saveButton = new JButton("Save Teacher");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(saveButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Enter Key Navigation ---
        nameField.addActionListener(e -> subjectField.requestFocusInWindow());
        subjectField.addActionListener(e -> salaryField.requestFocusInWindow());
        salaryField.addActionListener(e -> saveButton.doClick());

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String subject = subjectField.getText();

                if (name.isEmpty() || subject.isEmpty() || name.equals("Teacher Name")) {
                    throw new IllegalArgumentException("Name and Subject are required.");
                }

                double salary = Double.parseDouble(salaryField.getText());
                
                String newId = DataManager.getNextTeacherId();
                DataManager.addTeacher(new Teacher(newId, name, subject, salary));
                JOptionPane.showMessageDialog(this, "Teacher added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                salaryField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for salary.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
               button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- Edit Teacher Dialog ---
class EditTeacherDialog extends JDialog {
    public EditTeacherDialog(Frame owner, Teacher teacher) {
        super(owner, "Edit Teacher: " + teacher.getName(), true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = createFormField(teacher.getName());
        JTextField subjectField = createFormField(teacher.getSubject());
        JTextField salaryField = createFormField(String.valueOf(teacher.getTotalSalary()));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; formPanel.add(subjectField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Total Salary:"), gbc);
        gbc.gridx = 1; formPanel.add(salaryField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(saveButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Enter Key Navigation ---
        nameField.addActionListener(e -> subjectField.requestFocusInWindow());
        subjectField.addActionListener(e -> salaryField.requestFocusInWindow());
        salaryField.addActionListener(e -> saveButton.doClick());

        saveButton.addActionListener(e -> {
            try {
                String newName = nameField.getText();
                String newSubject = subjectField.getText();

                if (newName.isEmpty() || newSubject.isEmpty()) {
                    throw new IllegalArgumentException("Fields cannot be empty.");
                }

                double newSalary = Double.parseDouble(salaryField.getText());
                
                DataManager.updateTeacher(teacher.getId(), newName, newSubject, newSalary);
                JOptionPane.showMessageDialog(this, "Teacher updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                salaryField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for salary.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String value) {
        JTextField field = new JTextField(value, 20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(CustomColors.TEXT_LIGHT);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
               button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- Pay Salary Dialog ---
class PaySalaryDialog extends JDialog {
    public PaySalaryDialog(Frame owner) {
        super(owner, "Pay Salary", true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = createFormField("Enter Teacher ID (e.g., TID0001)");
        JTextField amountField = createFormField("Enter Amount");
        JComboBox<String> modeComboBox = new JComboBox<>(new String[]{"Bank Transfer", "Cheque", "Cash"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Teacher ID:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; formPanel.add(amountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 1; formPanel.add(modeComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton payButton = new JButton("Process Payment");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(payButton);
        styleDialogButton(cancelButton);

        buttonPanel.add(cancelButton);
        buttonPanel.add(payButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Enter Key Navigation ---
        idField.addActionListener(e -> amountField.requestFocusInWindow());
        amountField.addActionListener(e -> payButton.doClick());

        payButton.addActionListener(e -> {
            try {
                String teacherId = idField.getText();
                if (teacherId.isEmpty() || teacherId.equals("Enter Teacher ID (e.g., TID0001)")) {
                    idField.requestFocusInWindow();
                    throw new IllegalArgumentException("Teacher ID is required.");
                }

                double amount = Double.parseDouble(amountField.getText());
                String mode = (String) modeComboBox.getSelectedItem();
                
                Teacher teacher = DataManager.findTeacherById(teacherId);
                if (teacher == null) {
                    throw new IllegalArgumentException("Teacher with ID '" + teacherId + "' not found.");
                }
                if (amount <= 0) {
                    throw new IllegalArgumentException("Payment amount must be positive.");
                }
                if (amount > teacher.getDueSalary()) {
                    throw new IllegalArgumentException(String.format("Payment amount (₹%.2f) exceeds due salary (₹%.2f).", amount, teacher.getDueSalary()));
                }
                
                DataManager.payTeacherSalary(teacherId, amount, mode);
                JOptionPane.showMessageDialog(this, "Salary paid successfully to " + teacher.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (NumberFormatException ex) {
                amountField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- Salary History Dialog ---
class SalaryHistoryDialog extends JDialog {
    public SalaryHistoryDialog(Frame owner, Teacher teacher) {
        super(owner, "Salary History for " + teacher.getName(), true);
        setSize(550, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        String[] columnNames = {"Date", "Transaction ID", "Amount Paid (₹)", "Mode"};
        DefaultTableModel historyModel = new DefaultTableModel(columnNames, 0);
        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);
        
        List<Payment> history = DataManager.getPaymentHistoryForPerson(teacher.getId());

        if (history.isEmpty()) {
            historyModel.addRow(new Object[]{"No salary payments made yet.", "", "", ""});
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            for (Payment p : history) {
                historyModel.addRow(new Object[]{
                    sdf.format(p.getDate()),
                    p.getReceiptNumber(),
                    String.format("%.2f", p.getAmount()),
                    p.getPaymentMode()
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(CustomColors.SECONDARY_DARK);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleDialogButton(closeButton);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dispose());
    }

    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }

    private void styleTable(JTable table) {
        table.setBackground(CustomColors.SECONDARY_DARK);
        table.setForeground(CustomColors.TEXT_LIGHT);
        table.setGridColor(CustomColors.BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setBackground(CustomColors.PRIMARY_DARK);
        header.setForeground(CustomColors.ACCENT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    }
}


// --- Number to Words Converter ---
class NumberToWordsConverter {
    private static final String[] units = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(final long n) {
        if (n < 0) return "Minus " + convert(-n);
       //  if (n == 0) return "Zero";
        if (n < 20) return units[(int) n];
        if (n < 100) return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " : "") + units[(int) (n % 10)];
        if (n < 1000) return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " and " + convert(n % 100) : "");
        if (n < 100000) return convert(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + convert(n % 1000);
        if (n < 10000000) return convert(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convert(n % 100000);
        return convert(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convert(n % 10000000);
    }
}


// --- Promote Student Dialog ---
class PromoteStudentDialog extends JDialog {
    public PromoteStudentDialog(Frame owner, Student student) {
        super(owner, "Promote Student: " + student.getName(), true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomColors.SECONDARY_DARK);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomColors.SECONDARY_DARK);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel currentClassLabel = new JLabel("Current Class: " + student.getClassName());
        JTextField newClassField = createFormField("Enter New Class");
        JTextField newFeesField = createFormField("Enter New Total Fees");
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth=2; formPanel.add(currentClassLabel, gbc);
        gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("New Class:"), gbc);
        gbc.gridx = 1; formPanel.add(newClassField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("New Total Fees:"), gbc);
        gbc.gridx = 1; formPanel.add(newFeesField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CustomColors.SECONDARY_DARK);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        JButton saveButton = new JButton("Promote & Archive");
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(saveButton);
        styleDialogButton(cancelButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Enter Key Navigation ---
        newClassField.addActionListener(e -> newFeesField.requestFocusInWindow());
        newFeesField.addActionListener(e -> saveButton.doClick());

        saveButton.addActionListener(e -> {
            try {
                String newClass = newClassField.getText();
                if (newClass.isEmpty() || newClass.equals("Enter New Class")) {
                    newClassField.requestFocusInWindow();
                    throw new IllegalArgumentException("New class name is required.");
                }

                double newFees = Double.parseDouble(newFeesField.getText());

                int choice = JOptionPane.showConfirmDialog(this, 
                    "This will archive the student's current record for class '" + student.getClassName() + "'\n" +
                    "and create a new record for class '" + newClass + "' with fees reset.\n" +
                    "Are you sure you want to continue?", 
                    "Confirm Promotion", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    DataManager.promoteStudent(student.getId(), newClass, newFees);
                    JOptionPane.showMessageDialog(this, "Student promoted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }

            } catch (NumberFormatException ex) {
                newFeesField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, "Please enter a valid number for fees.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        
        for(Component c : formPanel.getComponents()){
            if(c instanceof JLabel){
                c.setForeground(CustomColors.TEXT_LIGHT);
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
    
    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CustomColors.PRIMARY_DARK);
        field.setForeground(Color.GRAY);
        field.setCaretColor(CustomColors.ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomColors.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CustomColors.TEXT_LIGHT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
    
    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(CustomColors.ACCENT_COLOR);
        button.setBackground(Color.WHITE);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(CustomColors.ACCENT_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(CustomColors.ACCENT_COLOR);
            }
        });
    }
}

// --- All Archived Records Dialog ---
class AllRecordsDialog extends JDialog {
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JComboBox<String> yearComboBox;

    public AllRecordsDialog(Frame owner) {
        super(owner, "View Archived Student Records", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(CustomColors.PRIMARY_DARK);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(CustomColors.PRIMARY_DARK);
        topPanel.add(new JLabel("Select Academic Year:") {{ setForeground(CustomColors.TEXT_LIGHT); }});
        
        yearComboBox = new JComboBox<>();
        List<String> years = DataManager.getDistinctAcademicYears();
        if (years.isEmpty()) {
            yearComboBox.addItem("No archived records found");
        } else {
            for (String year : years) {
                yearComboBox.addItem(year);
            }
        }
        topPanel.add(yearComboBox);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Student ID", "Name", "Father's Name", "Mother's Name", "Class", "Total Fees", "Paid Fees", "Due Fees", "Reason"};
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(CustomColors.SECONDARY_DARK);
        add(scrollPane, BorderLayout.CENTER);

        yearComboBox.addActionListener(e -> refreshTableData());

        refreshTableData();
    }

    private void refreshTableData() {
        historyTableModel.setRowCount(0);
        String selectedYear = (String) yearComboBox.getSelectedItem();

        if (selectedYear != null && !selectedYear.equals("No archived records found")) {
            List<StudentHistoryRecord> records = DataManager.getStudentHistoryByYear(selectedYear);
            for (StudentHistoryRecord r : records) {
                Vector<Object> row = new Vector<>();
                row.add(r.getStudentId());
                row.add(r.getName());
                row.add(r.getFatherName());
                row.add(r.getMotherName());
                row.add(r.getClassName());
                row.add(String.format("₹%.2f", r.getTotalFees()));
                row.add(String.format("₹%.2f", r.getPaidFees()));
                row.add(String.format("₹%.2f", r.getDueFees()));
                row.add(r.getReason());
                historyTableModel.addRow(row);
            }
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(CustomColors.SECONDARY_DARK);
        table.setForeground(CustomColors.TEXT_LIGHT);
        table.setGridColor(CustomColors.BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setBackground(CustomColors.PRIMARY_DARK);
        header.setForeground(CustomColors.ACCENT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    }
}