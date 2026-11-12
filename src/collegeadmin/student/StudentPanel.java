package collegeadmin.student;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class StudentPanel extends JPanel {
	private static final long serialVersionUID = 1L;

    private JTextField txtRollNo = new JTextField(15);
    private JTextField txtName = new JTextField(15);
    private JTextField txtEmail = new JTextField(15);
    private JTextField txtPhone = new JTextField(15);
    private JTextField txtDepartment = new JTextField(15);
    private JTextField txtSemester = new JTextField(15);

    private JTextField txtSearch = new JTextField(20);

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    // Pleasant light colours
    private final Color BACKGROUND = new Color(242, 247, 252);
    private final Color WHITE = Color.WHITE;
    private final Color PRIMARY = new Color(75, 115, 155);
    private final Color TEXT = new Color(45, 60, 75);
    private final Color BORDER = new Color(215, 225, 235);

    public StudentPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // HEADING
        JLabel heading = new JLabel("Student Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
            "Add, search, update and manage student records"
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new BorderLayout(5, 5));
        header.setOpaque(false);
        header.add(heading, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(12, 15));
        content.setOpaque(false);

        // FORM PANEL
        JPanel formPanel = new JPanel(new BorderLayout(10, 15));
        formPanel.setBackground(WHITE);
        formPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 3, 18, 15));
        fieldsPanel.setBackground(WHITE);

        fieldsPanel.add(makeField("Roll Number", txtRollNo));
        fieldsPanel.add(makeField("Name", txtName));
        fieldsPanel.add(makeField("Email", txtEmail));

        fieldsPanel.add(makeField("Phone", txtPhone));
        fieldsPanel.add(makeField("Department", txtDepartment));
        fieldsPanel.add(makeField("Semester", txtSemester));

        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 12, 5)
        );
        buttonPanel.setBackground(WHITE);

        JButton btnAdd = makeButton("Add");
        JButton btnSearch = makeButton("Search");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");
        JButton btnShowAll = makeButton("Show All");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnShowAll);

        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        content.add(formPanel, BorderLayout.NORTH);

        // TABLE HEADING
        JLabel tableHeading = new JLabel("Student Records");
        tableHeading.setFont(new Font("Arial", Font.BOLD, 18));
        tableHeading.setForeground(TEXT);

        // TABLE
        model = new DefaultTableModel(
            new String[]{
                "ID", "Name", "Email", "Phone", "Department", "Semester"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(27);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(215, 232, 248));
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );
        table.getTableHeader().setBackground(
            new Color(225, 236, 247)
        );
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER, 1));

        // TABLE AREA
        JPanel tablePanel = new JPanel(new BorderLayout(8, 10));
        tablePanel.setOpaque(false);

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setOpaque(false);
        tableTop.add(tableHeading, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Filter:");
        searchLabel.setForeground(TEXT);

        txtSearch.setPreferredSize(new Dimension(190, 30));

        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);

        tableTop.add(searchPanel, BorderLayout.EAST);

        tablePanel.add(tableTop, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        content.add(tablePanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // BUTTON ACTIONS
        btnAdd.addActionListener(e -> addStudent());
        btnSearch.addActionListener(e -> searchStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearFields());
        btnShowAll.addActionListener(e -> loadStudents());

        // LIVE TABLE FILTER
        txtSearch.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {

                private void filter() {
                    String value = txtSearch.getText().trim();

                    if (value.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(
                            RowFilter.regexFilter(
                                "(?i)" + Pattern.quote(value)
                            )
                        );
                    }
                }

                public void insertUpdate(
                    javax.swing.event.DocumentEvent e
                ) {
                    filter();
                }

                public void removeUpdate(
                    javax.swing.event.DocumentEvent e
                ) {
                    filter();
                }

                public void changedUpdate(
                    javax.swing.event.DocumentEvent e
                ) {
                    filter();
                }
            }
        );

        // TABLE ROW CLICK
        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = table.getSelectedRow();

                if (selectedRow >= 0) {

                    int row = table.convertRowIndexToModel(selectedRow);

                    txtRollNo.setText(safe(model.getValueAt(row, 0)));
                    txtName.setText(safe(model.getValueAt(row, 1)));
                    txtEmail.setText(safe(model.getValueAt(row, 2)));
                    txtPhone.setText(safe(model.getValueAt(row, 3)));
                    txtDepartment.setText(safe(model.getValueAt(row, 4)));
                    txtSemester.setText(safe(model.getValueAt(row, 5)));
                }
            }
        });

        loadStudents();
    }

    // FIELD DESIGN
    private JPanel makeField(String label, JTextField field) {

        JPanel panel = new JPanel(new BorderLayout(5, 6));
        panel.setBackground(WHITE);

        JLabel title = new JLabel(label);
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(TEXT);

        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(150, 32));
        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));

        panel.add(title, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    // BUTTON DESIGN
    private JButton makeButton(String name) {

        JButton button = new JButton(name);

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 16, 9, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // VALIDATION
    private boolean validateFields() {

        if (txtRollNo.getText().trim().isEmpty()
                || txtName.getText().trim().isEmpty()
                || txtEmail.getText().trim().isEmpty()
                || txtDepartment.getText().trim().isEmpty()
                || txtSemester.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                this, "Please fill all required fields."
            );
            return false;
        }

        try {
            int semester = Integer.parseInt(
                txtSemester.getText().trim()
            );

            if (semester < 1 || semester > 8) {
                JOptionPane.showMessageDialog(
                    this, "Semester must be between 1 and 8."
                );
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid semester number."
            );
            return false;
        }

        String phone = txtPhone.getText().trim();

        if (!phone.isEmpty() && !phone.matches("[0-9]{10,15}")) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid phone number (10–15 digits)."
            );
            return false;
        }

        if (!txtEmail.getText().trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            JOptionPane.showMessageDialog(
                this, "Enter a valid email address."
            );
            return false;
        }

        return true;
    }

    // ADD STUDENT
    private void addStudent() {

        if (!validateFields()) return;

        String sql = "INSERT INTO students " +
            "(roll_no, name, email, phone, department, semester) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtRollNo.getText().trim());
            ps.setString(2, txtName.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.setString(4, txtPhone.getText().trim().isEmpty()
                ? null : txtPhone.getText().trim());
            ps.setString(5, txtDepartment.getText().trim());
            ps.setInt(6, Integer.parseInt(txtSemester.getText().trim()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student added successfully!");

            loadStudents();
            clearFields();

        } catch (SQLException e) {
            showError(e);
        }
    }

    // SEARCH STUDENT
    private void searchStudent() {

        String roll = txtRollNo.getText().trim();

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Enter a roll number to search."
            );
            return;
        }

        String sql = "SELECT roll_no, name, email, phone, department, semester " +
                     "FROM students WHERE roll_no = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, roll);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    txtRollNo.setText(rs.getString("roll_no"));
                    txtName.setText(rs.getString("name"));
                    txtEmail.setText(rs.getString("email"));
                    txtPhone.setText(safe(rs.getString("phone")));
                    txtDepartment.setText(rs.getString("department"));
                    txtSemester.setText(
                        String.valueOf(rs.getInt("semester"))
                    );

                } else {
                    JOptionPane.showMessageDialog(
                        this, "Student not found."
                    );
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // UPDATE STUDENT
    private void updateStudent() {

        if (!validateFields()) return;

        String sql = "UPDATE students SET name=?, email=?, phone=?, " +
                     "department=?, semester=? WHERE roll_no=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtPhone.getText().trim().isEmpty()
                ? null : txtPhone.getText().trim());
            ps.setString(4, txtDepartment.getText().trim());
            ps.setInt(5, Integer.parseInt(txtSemester.getText().trim()));
            ps.setString(6, txtRollNo.getText().trim());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                    this, "Student updated successfully!"
                );
                loadStudents();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Student not found."
                );
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // DELETE STUDENT
    private void deleteStudent() {

        String roll = txtRollNo.getText().trim();

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Select or enter a student roll number."
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this student?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) return;

        String findId = "SELECT student_id FROM students WHERE roll_no=?";

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                int studentId;

                try (PreparedStatement ps = con.prepareStatement(findId)) {
                    ps.setString(1, roll);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(
                                this, "Student not found."
                            );
                            con.rollback();
                            return;
                        }
                        studentId = rs.getInt("student_id");
                    }
                }

                String[] childDeletes = {
                    "DELETE FROM attendance WHERE student_id=?",
                    "DELETE FROM marks WHERE student_id=?",
                    "DELETE FROM course_registration WHERE student_id=?"
                };

                for (String sql : childDeletes) {
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, studentId);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM students WHERE student_id=?")) {
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(
                    this, "Student deleted successfully!"
                );

                loadStudents();
                clearFields();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // LOAD ALL STUDENTS
    private void loadStudents() {

        model.setRowCount(0);

        String sql = "SELECT roll_no, name, email, phone, department, semester " +
                     "FROM students ORDER BY roll_no";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("email"),
                    safe(rs.getString("phone")),
                    rs.getString("department"),
                    rs.getInt("semester")
                });
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // CLEAR FORM
    private void clearFields() {

        txtRollNo.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtDepartment.setText("");
        txtSemester.setText("");

        table.clearSelection();
        txtRollNo.requestFocus();
    }

    // HANDLE NULL VALUES
    private String safe(Object value) {

        if (value == null || "null".equalsIgnoreCase(value.toString())) {
            return "";
        }

        return value.toString();
    }

    // ERROR MESSAGE
    private void showError(SQLException e) {

        JOptionPane.showMessageDialog(
            this,
            "Database Error:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
