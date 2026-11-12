package collegeadmin.course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import collegeadmin.database.DatabaseConnection;

public class CoursePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField searchField;
    private JTextField courseIdField;
    private JTextField courseNameField;
    private JTextField creditsField;

    private JTable courseTable;
    private DefaultTableModel model;

    public CoursePanel() {

        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(
                new String[]{"Course ID", "Course Name", "Credits"}, 0
        );

        courseTable = new JTable(model);

        searchField = new JTextField(20);
        courseIdField = new JTextField(15);
        courseNameField = new JTextField(15);
        creditsField = new JTextField(15);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));

        formPanel.add(new JLabel("Course ID:"));
        formPanel.add(courseIdField);

        formPanel.add(new JLabel("Course Name:"));
        formPanel.add(courseNameField);

        formPanel.add(new JLabel("Credits:"));
        formPanel.add(creditsField);

        JButton addButton = new JButton("Add Course");
        JButton clearButton = new JButton("Clear");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        bottomPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCourse());
        clearButton.addActionListener(e -> clearFields());

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                load(searchField.getText().trim());
            }
        });

        loadAll();
    }

    // Load all courses
    private void loadAll() {

        model.setRowCount(0);

        String sql = "SELECT course_id, course_name, credits FROM courses";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                });
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // Search courses
    private void load(String search) {

        model.setRowCount(0);

        String sql = "SELECT course_id, course_name, credits FROM courses "
                   + "WHERE CAST(course_id AS CHAR) LIKE ? "
                   + "OR course_name LIKE ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            rs.getInt("credits")
                    });
                }
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // Add course
    private void addCourse() {

        String name = courseNameField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (name.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter course name and credits.");
            return;
        }

        int credits;

        try {
            credits = Integer.parseInt(creditsText);

            if (credits <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid positive credit value.");
            return;
        }

        String sql = "INSERT INTO courses (course_name, credits) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, credits);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Course added successfully.");

            clearFields();
            loadAll();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // Clear form
    private void clearFields() {
        courseIdField.setText("");
        courseNameField.setText("");
        creditsField.setText("");
        courseTable.clearSelection();
    }

    // Display database error
    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}