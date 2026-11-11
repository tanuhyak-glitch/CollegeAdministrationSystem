package collegeadmin.course;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class CoursePanel extends JPanel {

    private JTextField txtId = new JTextField(5);
    private JTextField txtName = new JTextField(16);
    private JTextField txtCredits = new JTextField(4);
    private JTextField txtFacultyId = new JTextField(5);
    private JTextField txtStudentId = new JTextField(10);
    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"Course ID", "Course Name", "Credits", "Faculty", "Students"}, 0);
    private JTable table = new JTable(model);

    public CoursePanel() {
        setLayout(new BorderLayout());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Course ID")); row1.add(txtId);
        row1.add(new JLabel("Name")); row1.add(txtName);
        row1.add(new JLabel("Credits")); row1.add(txtCredits);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Faculty ID")); row2.add(txtFacultyId);
        JButton btnAssign = new JButton("Assign Faculty");
        row2.add(btnAssign);
        row2.add(new JLabel("Student ID")); row2.add(txtStudentId);
        JButton btnRegister = new JButton("Register Student");
        JButton btnView = new JButton("View Registered");
        row2.add(btnRegister); row2.add(btnView);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnAll = new JButton("View All");
        JPanel row3 = new JPanel();
        row3.add(btnAdd); row3.add(btnUpdate); row3.add(btnDelete);
        row3.add(btnClear); row3.add(btnAll);

        JPanel top = new JPanel(new GridLayout(3, 1));
        top.add(row1); top.add(row2); top.add(row3);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action listeners
        btnAdd.addActionListener(e -> addCourse());
        btnUpdate.addActionListener(e -> updateCourse());
        btnDelete.addActionListener(e -> deleteCourse());
        btnClear.addActionListener(e -> clearFields());
        btnAll.addActionListener(e -> loadAll());
        btnAssign.addActionListener(e -> assignFaculty());
        btnRegister.addActionListener(e -> registerStudent());
        btnView.addActionListener(e -> viewRegistered());

        // Mouse listener: click a row to fill the fields
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    txtId.setText(cell(r, 0));
                    txtName.setText(cell(r, 1));
                    txtCredits.setText(cell(r, 2));
                }
            }
        });

        // Key listener: live filter by course name
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                load(txtName.getText().trim());
            }
        });

        // Key listener: digits only for numeric fields
        KeyAdapter digitsOnly = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        };
        txtId.addKeyListener(digitsOnly);
        txtCredits.addKeyListener(digitsOnly);
        txtFacultyId.addKeyListener(digitsOnly);

        loadAll();
    }

    private String cell(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void addCourse() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course name is required.");
            return;
        }
        String sql = "INSERT INTO courses (course_name, credits) VALUES (?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, Integer.parseInt(txtCredits.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course added.");
            clearFields();
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Credits must be a number.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void updateCourse() {
        String sql = "UPDATE courses SET course_name=?, credits=? WHERE course_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, Integer.parseInt(txtCredits.getText().trim()));
            ps.setInt(3, Integer.parseInt(txtId.getText().trim()));
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Course updated." : "No course with that ID.");
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Course ID and Credits must be numbers.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void deleteCourse() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (JOptionPane.showConfirmDialog(this,
                    "Delete course " + id + " and its registrations, marks and attendance?",
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try (Connection con = DatabaseConnection.getConnection()) {
                for (String t : new String[]{"course_registration", "marks", "attendance"}) {
                    try (PreparedStatement p = con.prepareStatement(
                            "DELETE FROM " + t + " WHERE course_id=?")) {
                        p.setInt(1, id);
                        p.executeUpdate();
                    }
                }
                try (PreparedStatement p = con.prepareStatement(
                        "DELETE FROM courses WHERE course_id=?")) {
                    p.setInt(1, id);
                    int n = p.executeUpdate();
                    JOptionPane.showMessageDialog(this, n > 0 ? "Course deleted." : "No course with that ID.");
                }
            }
            clearFields();
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid Course ID.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void assignFaculty() {
        String sql = "UPDATE courses SET faculty_id=? WHERE course_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtFacultyId.getText().trim()));
            ps.setInt(2, Integer.parseInt(txtId.getText().trim()));
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Faculty assigned." : "No course with that ID.");
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter Course ID and Faculty ID as numbers.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not assign. Check that the Faculty ID exists.");
        }
    }

    private void registerStudent() {
        try {
            int cid = Integer.parseInt(txtId.getText().trim());
            String roll = txtStudentId.getText().trim();
            if (roll.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the student's roll number.");
                return;
            }
            try (Connection con = DatabaseConnection.getConnection()) {
                int sid;
                try (PreparedStatement find = con.prepareStatement(
                        "SELECT student_id FROM students WHERE roll_no=?")) {
                    find.setString(1, roll);
                    ResultSet rs = find.executeQuery();
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this, "No student with that roll number.");
                        return;
                    }
                    sid = rs.getInt(1);
                }
                try (PreparedStatement chk = con.prepareStatement(
                        "SELECT COUNT(*) FROM course_registration WHERE student_id=? AND course_id=?")) {
                    chk.setInt(1, sid); chk.setInt(2, cid);
                    ResultSet rs = chk.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Student is already registered.");
                        return;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO course_registration (student_id, course_id) VALUES (?,?)")) {
                    ps.setInt(1, sid); ps.setInt(2, cid);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student registered.");
                }
            }
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Select a course or enter a valid Course ID.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not register. Check that the Course ID exists.");
        }
    }

    private void viewRegistered() {
        try {
            int cid = Integer.parseInt(txtId.getText().trim());
            String sql = "SELECT s.roll_no, s.name FROM course_registration r "
                       + "JOIN students s ON s.student_id = r.student_id "
                       + "WHERE r.course_id=? ORDER BY s.roll_no";
            StringBuilder sb = new StringBuilder();
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, cid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("  ")
                      .append(rs.getString(2)).append("\n");
                }
            }
            JTextArea area = new JTextArea(sb.length() == 0 ? "No students registered." : sb.toString(), 15, 40);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Registered Students", JOptionPane.PLAIN_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Select a course or enter a Course ID.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void loadAll() {
        load("");
    }

    private void load(String nameFilter) {
        String sql = "SELECT c.course_id, c.course_name, c.credits, COALESCE(f.name,'-'), "
                   + "(SELECT COUNT(*) FROM course_registration r WHERE r.course_id = c.course_id) "
                   + "FROM courses c LEFT JOIN faculty f ON f.faculty_id = c.faculty_id "
                   + "WHERE c.course_name LIKE ? ORDER BY c.course_id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nameFilter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                        rs.getInt(3), rs.getString(4), rs.getInt(5)});
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void clearFields() {
        txtId.setText(""); txtName.setText(""); txtCredits.setText("");
        txtFacultyId.setText(""); txtStudentId.setText("");
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
}