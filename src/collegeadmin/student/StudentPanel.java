package collegeadmin.student;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class StudentPanel extends JPanel {

    private JTextField txtId = new JTextField(10);
    private JTextField txtName = new JTextField(12);
    private JTextField txtEmail = new JTextField(12);
    private JTextField txtPhone = new JTextField(10);
    private JTextField txtDept = new JTextField(10);
    private JTextField txtSem = new JTextField(4);
    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Email", "Phone", "Department", "Semester"}, 0);
    private JTable table = new JTable(model);

    public StudentPanel() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("ID")); form.add(txtId);
        form.add(new JLabel("Name")); form.add(txtName);
        form.add(new JLabel("Email")); form.add(txtEmail);
        form.add(new JLabel("Phone")); form.add(txtPhone);
        form.add(new JLabel("Dept")); form.add(txtDept);
        form.add(new JLabel("Sem")); form.add(txtSem);

        JButton btnAdd = new JButton("Add");
        JButton btnSearch = new JButton("Search");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnAll = new JButton("Show All");

        JPanel buttons = new JPanel();
        buttons.add(btnAdd); buttons.add(btnSearch); buttons.add(btnUpdate);
        buttons.add(btnDelete); buttons.add(btnClear); buttons.add(btnAll);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action listeners
        btnAdd.addActionListener(e -> addStudent());
        btnSearch.addActionListener(e -> searchStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearFields());
        btnAll.addActionListener(e -> loadAll());

        // Mouse listener: click a row to fill the fields
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    txtId.setText(cell(r, 0));
                    txtName.setText(cell(r, 1));
                    txtEmail.setText(cell(r, 2));
                    txtPhone.setText(cell(r, 3));
                    txtDept.setText(cell(r, 4));
                    txtSem.setText(cell(r, 5));
                }
            }
        });

        // Key listener: live filter by name, Enter = full search
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchStudent();
                } else {
                    filterByName(txtName.getText().trim());
                }
            }
        });

        // Key listener: Semester accepts digits only
        txtSem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        loadAll();
    }

    private String cell(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void filterByName(String text) {
        if (text.isEmpty()) {
            loadAll();
            return;
        }
        String sql = "SELECT * FROM students WHERE name LIKE ? ORDER BY roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + text + "%");
            fillTable(ps.executeQuery());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addStudent() {
        if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID (roll number) and Name are required.");
            return;
        }
        String sql = "INSERT INTO students (roll_no, name, email, phone, department, semester) VALUES (?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtId.getText().trim());
            ps.setString(2, txtName.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.setString(4, txtPhone.getText().trim());
            ps.setString(5, txtDept.getText().trim());
            ps.setInt(6, Integer.parseInt(txtSem.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added.");
            clearFields();
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Semester must be a number.");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void searchStudent() {
        String sql = "SELECT * FROM students WHERE name LIKE ? OR roll_no = ? ORDER BY roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + txtName.getText().trim() + "%");
            ps.setString(2, txtId.getText().trim());
            fillTable(ps.executeQuery());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateStudent() {
        String sql = "UPDATE students SET name=?, email=?, phone=?, department=?, semester=? WHERE roll_no=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtDept.getText().trim());
            ps.setInt(5, Integer.parseInt(txtSem.getText().trim()));
            ps.setString(6, txtId.getText().trim());
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Student updated." : "No student with that ID.");
            loadAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Semester must be a number.");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteStudent() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a valid ID.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete student " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE roll_no=?")) {
            ps.setString(1, id);
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, n > 0 ? "Student deleted." : "No student with that ID.");
            clearFields();
            loadAll();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadAll() {
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            fillTable(st.executeQuery("SELECT * FROM students ORDER BY roll_no"));
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void fillTable(ResultSet rs) throws SQLException {
        model.setRowCount(0);
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("roll_no"), rs.getString("name"), rs.getString("email"),
                rs.getString("phone"), rs.getString("department"), rs.getInt("semester")});
        }
    }

    private void clearFields() {
        txtId.setText(""); txtName.setText(""); txtEmail.setText("");
        txtPhone.setText(""); txtDept.setText(""); txtSem.setText("");
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
}