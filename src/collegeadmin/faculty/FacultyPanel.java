package collegeadmin.faculty;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class FacultyPanel extends JPanel {
	private static final long serialVersionUID = 1L;

    private JTextField txtId = new JTextField(5);
    private JTextField txtName = new JTextField(14);
    private JTextField txtEmail = new JTextField(16);
    private JTextField txtDept = new JTextField(10);
    private JTextField txtPhone = new JTextField(10);
    private JTextField txtCourseId = new JTextField(5);

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{
            "ID", "Name", "Email", "Department",
            "Phone", "Courses Assigned"
        }, 0
    );

    private JTable table = new JTable(model);

    // PLEASANT LIGHT COLOUR PALETTE
    private final Color BACKGROUND =
        new Color(240, 245, 250);

    private final Color SAGE =
        new Color(221, 235, 231);

    private final Color WHITE =
        Color.WHITE;

    private final Color PRIMARY =
        new Color(73, 125, 120);

    private final Color TEXT =
        new Color(45, 60, 75);

    private final Color BORDER =
        new Color(210, 222, 230);

    private final Color SELECTED =
        new Color(215, 234, 230);

    public FacultyPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // HEADER
        JLabel heading = new JLabel("Faculty Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
            "Add, update, manage faculty details and assign courses"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(110, 125, 140));

        JPanel header = new JPanel(new BorderLayout(5, 5));
        header.setOpaque(false);

        header.add(heading, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(12, 15));
        content.setOpaque(false);

        // FACULTY FORM PANEL
        JPanel formPanel = new JPanel(new BorderLayout(10, 15));
        formPanel.setBackground(SAGE);

        formPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel formHeading = new JLabel("Faculty Details");
        formHeading.setFont(new Font("Arial", Font.BOLD, 18));
        formHeading.setForeground(TEXT);

        // FORM FIELDS
        JPanel fields = new JPanel(new GridLayout(2, 3, 18, 15));
        fields.setOpaque(false);

        fields.add(makeField("Faculty ID", txtId));
        fields.add(makeField("Faculty Name", txtName));
        fields.add(makeField("Email Address", txtEmail));

        fields.add(makeField("Department", txtDept));
        fields.add(makeField("Phone Number", txtPhone));

        // COURSE ASSIGNMENT
        JPanel coursePanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 12, 5)
        );

        coursePanel.setOpaque(false);

        JLabel courseLabel = new JLabel("Course ID");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 13));
        courseLabel.setForeground(TEXT);

        styleField(txtCourseId);

        JButton btnAssign = makeButton(
            "Assign Course to Faculty"
        );

        coursePanel.add(courseLabel);
        coursePanel.add(txtCourseId);
        coursePanel.add(btnAssign);

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 12, 5)
        );

        buttonPanel.setOpaque(false);

        JButton btnAdd = makeButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");
        JButton btnAll = makeButton("View All");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnAll);

        // FORM LAYOUT
        JPanel formContent = new JPanel(new BorderLayout(10, 15));
        formContent.setOpaque(false);

        JPanel formTop = new JPanel(new BorderLayout());
        formTop.setOpaque(false);
        formTop.add(formHeading, BorderLayout.NORTH);

        JPanel formCenter = new JPanel(new BorderLayout(10, 15));
        formCenter.setOpaque(false);

        formCenter.add(fields, BorderLayout.CENTER);
        formCenter.add(coursePanel, BorderLayout.SOUTH);

        formContent.add(formTop, BorderLayout.NORTH);
        formContent.add(formCenter, BorderLayout.CENTER);
        formContent.add(buttonPanel, BorderLayout.SOUTH);

        formPanel.add(formContent, BorderLayout.CENTER);

        // TOP SECTION
        JPanel topSection = new JPanel(new BorderLayout(10, 15));
        topSection.setOpaque(false);

        topSection.add(header, BorderLayout.NORTH);
        topSection.add(formPanel, BorderLayout.CENTER);

        content.add(topSection, BorderLayout.NORTH);

        // TABLE HEADING
        JLabel tableHeading = new JLabel("Faculty Records");
        tableHeading.setFont(new Font("Arial", Font.BOLD, 18));
        tableHeading.setForeground(TEXT);

        // TABLE DESIGN
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        table.setForeground(TEXT);
        table.setBackground(WHITE);

        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);

        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(
            new Color(225, 236, 235)
        );

        table.getTableHeader().setForeground(TEXT);

        table.getTableHeader().setPreferredSize(
            new Dimension(0, 35)
        );

        // SCROLL PANE
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(WHITE);

        // TABLE PANEL
        JPanel tablePanel = new JPanel(new BorderLayout(8, 12));
        tablePanel.setBackground(WHITE);

        tablePanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        content.add(tablePanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // ACTION LISTENERS
        btnAdd.addActionListener(e -> addFaculty());
        btnUpdate.addActionListener(e -> updateFaculty());
        btnDelete.addActionListener(e -> deleteFaculty());
        btnClear.addActionListener(e -> clearFields());
        btnAll.addActionListener(e -> loadAll());
        btnAssign.addActionListener(e -> assignCourse());

        // TABLE ROW CLICK
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int r = table.getSelectedRow();

                if (r >= 0) {

                    int row = table.convertRowIndexToModel(r);

                    txtId.setText(cell(row, 0));
                    txtName.setText(cell(row, 1));
                    txtEmail.setText(cell(row, 2));
                    txtDept.setText(cell(row, 3));
                    txtPhone.setText(cell(row, 4));
                }
            }
        });

        // LIVE FILTER BY NAME
        txtName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                load(txtName.getText().trim());
            }
        });

        // DIGITS ONLY
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

    // FIELD DESIGN
    private JPanel makeField(String label, JTextField field) {

        JPanel panel = new JPanel(new BorderLayout(5, 6));
        panel.setOpaque(false);

        JLabel title = new JLabel(label);
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(TEXT);

        styleField(field);

        panel.add(title, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    // INPUT FIELD STYLE
    private void styleField(JTextField field) {

        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);

        field.setPreferredSize(new Dimension(150, 33));

        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    // BUTTON DESIGN
    private JButton makeButton(String name) {

        JButton button = new JButton(name);

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(PRIMARY);

        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 17, 10, 17));

        button.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        button.setOpaque(true);

        return button;
    }

    // GET TABLE CELL
    private String cell(int row, int col) {

        Object v = model.getValueAt(row, col);

        return v == null ? "" : v.toString();
    }

    // ADD FACULTY
    private void addFaculty() {

        if (txtName.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                this, "Name is required."
            );
            return;
        }

        String sql = "INSERT INTO faculty " +
            "(name, email, department, phone) VALUES (?,?,?,?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtDept.getText().trim());
            ps.setString(4, txtPhone.getText().trim());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Faculty added."
            );

            clearFields();
            loadAll();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // UPDATE FACULTY
    private void updateFaculty() {

        String sql = "UPDATE faculty SET name=?, email=?, " +
            "department=?, phone=? WHERE faculty_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtDept.getText().trim());
            ps.setString(4, txtPhone.getText().trim());

            ps.setInt(5, Integer.parseInt(
                txtId.getText().trim()
            ));

            int n = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                n > 0 ? "Faculty updated."
                      : "No faculty with that ID."
            );

            loadAll();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                this, "Enter a valid ID."
            );

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DELETE FACULTY
    private void deleteFaculty() {

        try {

            int id = Integer.parseInt(
                txtId.getText().trim()
            );

            if (JOptionPane.showConfirmDialog(
                this,
                "Delete faculty " + id + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            ) != JOptionPane.YES_OPTION) return;

            try (Connection con = DatabaseConnection.getConnection()) {

                try (PreparedStatement p1 = con.prepareStatement(
                    "UPDATE courses SET faculty_id=NULL WHERE faculty_id=?"
                )) {

                    p1.setInt(1, id);
                    p1.executeUpdate();
                }

                try (PreparedStatement p2 = con.prepareStatement(
                    "DELETE FROM faculty WHERE faculty_id=?"
                )) {

                    p2.setInt(1, id);

                    int n = p2.executeUpdate();

                    JOptionPane.showMessageDialog(
                        this,
                        n > 0 ? "Faculty deleted."
                              : "No faculty with that ID."
                    );
                }
            }

            clearFields();
            loadAll();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                this, "Enter a valid ID."
            );

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ASSIGN COURSE
    private void assignCourse() {

        String sql =
            "UPDATE courses SET faculty_id=? WHERE course_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(
                txtId.getText().trim()
            ));

            ps.setInt(2, Integer.parseInt(
                txtCourseId.getText().trim()
            ));

            int n = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                n > 0 ? "Course assigned."
                      : "No course with that ID."
            );

            loadAll();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                this,
                "Enter Faculty ID and Course ID as numbers."
            );

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // LOAD ALL FACULTY
    private void loadAll() {
        load("");
    }

    // LOAD FACULTY WITH NAME FILTER
    private void load(String nameFilter) {

        String sql = "SELECT f.faculty_id, f.name, f.email, " +
            "f.department, f.phone, " +
            "COALESCE(GROUP_CONCAT(c.course_name SEPARATOR ', '), '-') AS courses " +
            "FROM faculty f LEFT JOIN courses c " +
            "ON c.faculty_id = f.faculty_id " +
            "WHERE f.name LIKE ? " +
            "GROUP BY f.faculty_id, f.name, f.email, f.department, f.phone " +
            "ORDER BY f.faculty_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nameFilter + "%");

            try (ResultSet rs = ps.executeQuery()) {

                model.setRowCount(0);

                while (rs.next()) {

                    model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6)
                    });
                }
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // CLEAR FIELDS
    private void clearFields() {

        txtId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtDept.setText("");
        txtPhone.setText("");
        txtCourseId.setText("");

        table.clearSelection();
    }

    // DATABASE ERROR
    private void showError(SQLException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Database error: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
