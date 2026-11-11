package collegeadmin.faculty;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class FacultyPanel extends JPanel {

    private JTextField txtId = new JTextField(5);
    private JTextField txtName = new JTextField(14);
    private JTextField txtEmail = new JTextField(16);
    private JTextField txtDept = new JTextField(10);
    private JTextField txtCourseId = new JTextField(5);
    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Email", "Department", "Courses Assigned"}, 0);
    private JTable table = new JTable(model);

    public FacultyPanel() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("ID")); form.add(txtId);
        form.add(new JLabel("Name")); form.add(txtName);
        form.add(new JLabel("Email")); form.add(txtEmail);
        form.add(new JLabel("Dept")); form.add(txtDept);

        JPanel assign = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assign.add(new JLabel("Course ID")); assign.add(txtCourseId);
        JButton btnAssign = new JButton("Assign Course to Faculty ID");
        assign.add(btnAssign);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnAll = new JButton("View All");
        JPanel buttons = new JPanel();
        buttons.add(btnAdd); buttons.add(btnUpdate); buttons.add(btnDelete);
        buttons.add(btnClear); buttons.add(btnAll);

        JPanel top = new JPanel(new GridLayout(3, 1));
        top.add(form); top.add(assign); top.add(buttons);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action listeners
        btnAdd.addActionListener(e -> addFaculty());
        btnUpdate.addActionListener(e -> updateFaculty());
        btnDelete.addActionListener(e -> deleteFaculty());
        btnClear.addActionListener(e -> clearFields());
        btnAll.addActionListener(e -> loadAll());
        btnAssign.addActionListener(e -> assignCourse());

        // Mouse listener: click a row to fill the fields
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    txtId.setText(cell(r, 0));
                    txtName.setText(cell(r, 1));
                    txtEmail.setText(cell(r, 2));
                    txtDept.setText(cell(r, 3));
                }
            }
        });

        // Key listener: live filter by name (Enter does the same)
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                load(txtName.getText().trim());
            }
        });

        // Key listener: digits only for ID fields
        KeyAdapter digitsOnly = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        };
        txtId.addKeyListener(digitsOnly);
        txtCourseId.addKeyListener(digitsOnly);

        loadAll();
    }

    private String cell(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void addFaculty() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return;
        }
        String sql = "INSERT INTO faculty (name, email, department) VALUES (?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtDept.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Faculty added.");
            clearFields();
            loadAll();
        } catch (SQLException ex) { showError(ex); }
    }

    private void updateFaculty() {
        String sql = "UPDATE faculty SET name=?, email=?, department=? WHERE faculty_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtDept.getText().trim());
            ps.setInt(4, Integer.parseInt(txtId.getText().trim()));
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Faculty updated." : "No faculty with that ID.");
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid ID.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void deleteFaculty() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (JOptionPane.showConfirmDialog(this, "Delete faculty " + id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement p1 = con.prepareStatement(
                        "UPDATE courses SET faculty_id=NULL WHERE faculty_id=?")) {
                    p1.setInt(1, id);
                    p1.executeUpdate();
                }
                try (PreparedStatement p2 = con.prepareStatement(
                        "DELETE FROM faculty WHERE faculty_id=?")) {
                    p2.setInt(1, id);
                    int n = p2.executeUpdate();
                    JOptionPane.showMessageDialog(this, n > 0 ? "Faculty deleted." : "No faculty with that ID.");
                }
            }
            clearFields();
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid ID.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void assignCourse() {
        String sql = "UPDATE courses SET faculty_id=? WHERE course_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
            ps.setInt(2, Integer.parseInt(txtCourseId.getText().trim()));
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Course assigned." : "No course with that ID.");
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter Faculty ID and Course ID as numbers.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void loadAll() {
        load("");
    }

    private void load(String nameFilter) {
        String sql = "SELECT f.faculty_id, f.name, f.email, f.department, "
                   + "COALESCE(GROUP_CONCAT(c.course_name SEPARATOR ', '), '-') AS courses "
                   + "FROM faculty f LEFT JOIN courses c ON c.faculty_id = f.faculty_id "
                   + "WHERE f.name LIKE ? "
                   + "GROUP BY f.faculty_id, f.name, f.email, f.department "
                   + "ORDER BY f.faculty_id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nameFilter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getString(5)});
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void clearFields() {
        txtId.setText(""); txtName.setText(""); txtEmail.setText("");
        txtDept.setText(""); txtCourseId.setText("");
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
}