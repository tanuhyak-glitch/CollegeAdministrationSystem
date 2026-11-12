package collegeadmin.course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

import collegeadmin.database.DatabaseConnection;

/**
 * Course module: add course, assign faculty, register students.
 */
public class CoursePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField searchField;
    private JTextField courseIdField;
    private JTextField courseNameField;
    private JTextField creditsField;

    private JComboBox<Item> facultyCombo = new JComboBox<>();
    private JComboBox<Item> studentCombo = new JComboBox<>();
    private JCheckBox showRegistered = new JCheckBox("Show registered students", true);

    private JTable courseTable;
    private DefaultTableModel model;
    private JTable regTable;
    private DefaultTableModel regModel;

    /** Simple id + label holder for combo boxes. */
    private static class Item {
        final int id;
        final String label;
        Item(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public CoursePanel() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[]{"Course ID", "Course Name", "Credits", "Faculty", "Registered"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        courseTable = new JTable(model);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        regModel = new DefaultTableModel(new String[]{"Roll No", "Student Name"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = new JTable(regModel);

        searchField = new JTextField(20);
        courseIdField = new JTextField(15);
        courseIdField.setEditable(false);
        courseNameField = new JTextField(15);
        creditsField = new JTextField(15);

        // ---- top: search ----
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        // ---- centre: courses | registered students ----
        JPanel regPanel = new JPanel(new BorderLayout(5, 5));
        regPanel.setBorder(BorderFactory.createTitledBorder("Registered students (selected course)"));
        regPanel.add(new JScrollPane(regTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(courseTable), regPanel);
        split.setResizeWeight(0.65);

        // ---- south: forms ----
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        formPanel.add(new JLabel("Course ID:"));
        formPanel.add(courseIdField);
        formPanel.add(new JLabel("Course Name:"));
        formPanel.add(courseNameField);
        formPanel.add(new JLabel("Credits:"));
        formPanel.add(creditsField);

        JButton addButton = new JButton("Add Course");
        JButton clearButton = new JButton("Clear");
        JPanel addButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButtons.add(addButton);
        addButtons.add(clearButton);

        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.setBorder(BorderFactory.createTitledBorder("Add course"));
        addPanel.add(formPanel, BorderLayout.CENTER);
        addPanel.add(addButtons, BorderLayout.SOUTH);

        JButton assignButton = new JButton("Assign Faculty");
        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        assignPanel.setBorder(BorderFactory.createTitledBorder("Assign faculty to selected course"));
        assignPanel.add(new JLabel("Faculty:"));
        assignPanel.add(facultyCombo);
        assignPanel.add(assignButton);

        JButton registerButton = new JButton("Register Student");
        JButton unregisterButton = new JButton("Unregister");
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        registerPanel.setBorder(BorderFactory.createTitledBorder("Register students for selected course"));
        registerPanel.add(new JLabel("Student:"));
        registerPanel.add(studentCombo);
        registerPanel.add(registerButton);
        registerPanel.add(unregisterButton);
        registerPanel.add(showRegistered);

        JPanel actions = new JPanel(new GridLayout(2, 1, 5, 5));
        actions.add(assignPanel);
        actions.add(registerPanel);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.add(addPanel, BorderLayout.CENTER);
        southPanel.add(actions, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // ---- listeners ----
        addButton.addActionListener(e -> addCourse());
        clearButton.addActionListener(e -> clearFields());
        assignButton.addActionListener(e -> assignFaculty());
        registerButton.addActionListener(e -> registerStudent());
        unregisterButton.addActionListener(e -> unregisterStudent());
        showRegistered.addActionListener(e -> {
            regPanel.setVisible(showRegistered.isSelected());
            split.setDividerLocation(showRegistered.isSelected() ? 0.65 : 1.0);
            split.revalidate();
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                load(searchField.getText().trim());
            }
        });

        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = courseTable.getSelectedRow();
                if (r < 0) return;
                courseIdField.setText(String.valueOf(model.getValueAt(r, 0)));
                courseNameField.setText(String.valueOf(model.getValueAt(r, 1)));
                creditsField.setText(String.valueOf(model.getValueAt(r, 2)));
                loadRegistered();
            }
        });

        loadFaculty();
        loadStudentsCombo();
        load("");
    }

    // ---------------------------------------------------------------- loading

    private void load(String search) {

        model.setRowCount(0);

        String sql = "SELECT c.course_id, c.course_name, c.credits, "
                   + "COALESCE(f.name, '-') AS faculty, "
                   + "(SELECT COUNT(*) FROM course_registration r WHERE r.course_id = c.course_id) AS reg "
                   + "FROM courses c LEFT JOIN faculty f ON f.faculty_id = c.faculty_id "
                   + "WHERE CAST(c.course_id AS CHAR) LIKE ? OR c.course_name LIKE ? "
                   + "ORDER BY c.course_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getString("faculty"),
                            rs.getInt("reg")
                    });
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadFaculty() {
        facultyCombo.removeAllItems();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT faculty_id, name FROM faculty ORDER BY name")) {
            while (rs.next()) {
                facultyCombo.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadStudentsCombo() {
        studentCombo.removeAllItems();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT student_id, roll_no, name FROM students ORDER BY roll_no")) {
            while (rs.next()) {
                studentCombo.addItem(new Item(rs.getInt(1), rs.getString(2) + " - " + rs.getString(3)));
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadRegistered() {
        regModel.setRowCount(0);
        int courseId = selectedCourseId();
        if (courseId < 0) return;

        String sql = "SELECT s.roll_no, s.name FROM course_registration r "
                   + "JOIN students s ON s.student_id = r.student_id "
                   + "WHERE r.course_id = ? ORDER BY s.roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    regModel.addRow(new Object[]{rs.getString(1), rs.getString(2)});
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private int selectedCourseId() {
        int r = courseTable.getSelectedRow();
        if (r < 0) return -1;
        return Integer.parseInt(String.valueOf(model.getValueAt(r, 0)));
    }

    private void reloadKeepingSelection(int courseId) {
        load(searchField.getText().trim());
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Integer.parseInt(String.valueOf(model.getValueAt(i, 0))) == courseId) {
                courseTable.setRowSelectionInterval(i, i);
                break;
            }
        }
        loadRegistered();
    }

    // ---------------------------------------------------------------- actions

    private void addCourse() {

        String name = courseNameField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (name.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter course name and credits.");
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
            if (credits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive credit value.");
            return;
        }

        String sql = "INSERT INTO courses (course_name, credits) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, credits);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course added successfully.");
            clearFields();
            load("");

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void assignFaculty() {

        int courseId = selectedCourseId();
        Item faculty = (Item) facultyCombo.getSelectedItem();

        if (courseId < 0) {
            JOptionPane.showMessageDialog(this, "Select a course in the table first.");
            return;
        }
        if (faculty == null) {
            JOptionPane.showMessageDialog(this, "No faculty available. Add faculty first.");
            return;
        }

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE courses SET faculty_id = ? WHERE course_id = ?")) {

            ps.setInt(1, faculty.id);
            ps.setInt(2, courseId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, faculty.label + " assigned to the course.");
            reloadKeepingSelection(courseId);

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void registerStudent() {

        int courseId = selectedCourseId();
        Item student = (Item) studentCombo.getSelectedItem();

        if (courseId < 0) {
            JOptionPane.showMessageDialog(this, "Select a course in the table first.");
            return;
        }
        if (student == null) {
            JOptionPane.showMessageDialog(this, "No students available. Add students first.");
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {

            try (PreparedStatement check = con.prepareStatement(
                    "SELECT COUNT(*) FROM course_registration WHERE student_id = ? AND course_id = ?")) {
                check.setInt(1, student.id);
                check.setInt(2, courseId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Student is already registered for this course.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO course_registration (student_id, course_id) VALUES (?, ?)")) {
                ps.setInt(1, student.id);
                ps.setInt(2, courseId);
                ps.executeUpdate();
            }

            reloadKeepingSelection(courseId);

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void unregisterStudent() {

        int courseId = selectedCourseId();
        Item student = (Item) studentCombo.getSelectedItem();

        if (courseId < 0 || student == null) {
            JOptionPane.showMessageDialog(this, "Select a course and a student.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Unregister " + student.label + " from this course?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM course_registration WHERE student_id = ? AND course_id = ?")) {

            ps.setInt(1, student.id);
            ps.setInt(2, courseId);
            int n = ps.executeUpdate();

            if (n == 0) {
                JOptionPane.showMessageDialog(this, "That student was not registered for this course.");
            }
            reloadKeepingSelection(courseId);

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void clearFields() {
        courseIdField.setText("");
        courseNameField.setText("");
        creditsField.setText("");
        courseTable.clearSelection();
        regModel.setRowCount(0);
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
