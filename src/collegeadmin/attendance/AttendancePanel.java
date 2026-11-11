package collegeadmin.attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

import collegeadmin.database.DatabaseConnection;

public class AttendancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Colour palette
    private static final Color SOFT_BLUE =
        new Color(234, 242, 248);

    private static final Color SAGE_GREEN =
        new Color(220, 233, 223);

    private static final Color MUTED_TEAL =
        new Color(86, 143, 139);

    private static final Color DARK_TEAL =
        new Color(48, 91, 91);

    private static final Color WHITE = Color.WHITE;

    private static final Color TEXT_COLOR =
        new Color(55, 75, 85);

    // Form components
    private JComboBox<StudentItem> studentCombo;
    private JComboBox<CourseItem> courseCombo;
    private JComboBox<String> statusCombo;

    private JSpinner dateSpinner;

    // Table
    private JTable attendanceTable;
    private DefaultTableModel model;

    // Dashboard
    private JLabel totalStudentsValue;
    private JLabel presentValue;
    private JLabel absentValue;
    private JLabel lateValue;
    private JLabel percentageValue;

    public AttendancePanel() {

        setLayout(new BorderLayout(12, 12));
        setBackground(SOFT_BLUE);

        setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // ================= HEADER =================

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SOFT_BLUE);

        JLabel heading = new JLabel("Attendance Management");

        heading.setFont(
            new Font("Arial", Font.BOLD, 27)
        );

        heading.setForeground(DARK_TEAL);

        JLabel subtitle = new JLabel(
            "Record and manage student attendance"
        );

        subtitle.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );

        subtitle.setForeground(TEXT_COLOR);

        JPanel titlePanel = new JPanel(
            new GridLayout(2, 1, 0, 5)
        );

        titlePanel.setBackground(SOFT_BLUE);
        titlePanel.add(heading);
        titlePanel.add(subtitle);

        headerPanel.add(titlePanel, BorderLayout.NORTH);

        // ================= DASHBOARD =================

        JPanel dashboardPanel = new JPanel(
            new GridLayout(1, 5, 12, 12)
        );

        dashboardPanel.setBackground(SOFT_BLUE);

        dashboardPanel.setBorder(
            BorderFactory.createEmptyBorder(15, 0, 10, 0)
        );

        totalStudentsValue = createValueLabel();
        presentValue = createValueLabel();
        absentValue = createValueLabel();
        lateValue = createValueLabel();
        percentageValue = createValueLabel();

        dashboardPanel.add(
            createStatCard(
                "Total Students",
                totalStudentsValue,
                SAGE_GREEN
            )
        );

        dashboardPanel.add(
            createStatCard(
                "Present",
                presentValue,
                WHITE
            )
        );

        dashboardPanel.add(
            createStatCard(
                "Absent",
                absentValue,
                SAGE_GREEN
            )
        );

        dashboardPanel.add(
            createStatCard(
                "Late",
                lateValue,
                WHITE
            )
        );

        dashboardPanel.add(
            createStatCard(
                "Attendance Rate",
                percentageValue,
                SAGE_GREEN
            )
        );

        headerPanel.add(
            dashboardPanel,
            BorderLayout.CENTER
        );

        add(headerPanel, BorderLayout.NORTH);

        // ================= SELECT STUDENT =================

        studentCombo = new JComboBox<>();
        studentCombo.setMaximumRowCount(8);
        studentCombo.setEditable(false);

        // ================= SELECT COURSE =================

        courseCombo = new JComboBox<>();
        courseCombo.setMaximumRowCount(8);
        courseCombo.setEditable(false);

        // ================= SELECT DATE =================

        dateSpinner = new JSpinner(
            new SpinnerDateModel()
        );

        dateSpinner.setValue(
            java.sql.Date.valueOf(LocalDate.now())
        );

        JSpinner.DateEditor dateEditor =
            new JSpinner.DateEditor(
                dateSpinner,
                "yyyy-MM-dd"
            );

        dateSpinner.setEditor(dateEditor);

        // ================= SELECT STATUS =================

        statusCombo = new JComboBox<>(
            new String[]{
                "Present",
                "Absent",
                "Late"
            }
        );

        styleInput(studentCombo);
        styleInput(courseCombo);
        styleInput(dateSpinner);
        styleInput(statusCombo);

        // ================= FORM PANEL =================

        JPanel formPanel = new JPanel(
            new GridLayout(2, 3, 15, 12)
        );

        formPanel.setBackground(WHITE);

        formPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    SAGE_GREEN, 2
                ),
                BorderFactory.createTitledBorder(
                    "Select Attendance Details"
                )
            )
        );

        formPanel.add(
            createFieldPanel(
                "Select Student",
                studentCombo
            )
        );

        formPanel.add(
            createFieldPanel(
                "Select Course",
                courseCombo
            )
        );

        formPanel.add(
            createFieldPanel(
                "Select Date",
                dateSpinner
            )
        );

        formPanel.add(
            createFieldPanel(
                "Attendance Status",
                statusCombo
            )
        );

        // ================= BUTTONS =================

        JButton saveButton =
            createButton("Save Attendance");

        JButton clearButton =
            createButton("Clear");

        JButton deleteButton =
            createButton("Delete Selected");

        JButton refreshButton =
            createButton("Refresh");

        JPanel savePanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        savePanel.setBackground(WHITE);
        savePanel.add(saveButton);

        formPanel.add(savePanel);

        JPanel clearPanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        clearPanel.setBackground(WHITE);
        clearPanel.add(clearButton);

        formPanel.add(clearPanel);

        // ================= TABLE =================

        model = new DefaultTableModel(
            new String[]{
                "Student ID",
                "Course ID",
                "Roll Number",
                "Student Name",
                "Course",
                "Date",
                "Status"
            },
            0
        ) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(
                int row,
                int column
            ) {
                return false;
            }
        };

        attendanceTable = new JTable(model);

        attendanceTable.setRowHeight(29);

        attendanceTable.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );

        attendanceTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        attendanceTable.setSelectionBackground(SAGE_GREEN);
        attendanceTable.setSelectionForeground(TEXT_COLOR);
        attendanceTable.setGridColor(
            new Color(225, 232, 235)
        );

        JTableHeader tableHeader =
            attendanceTable.getTableHeader();

        tableHeader.setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        tableHeader.setBackground(MUTED_TEAL);
        tableHeader.setForeground(WHITE);

        tableHeader.setPreferredSize(
            new Dimension(0, 35)
        );

        JScrollPane scrollPane =
            new JScrollPane(attendanceTable);

        scrollPane.getViewport().setBackground(WHITE);

        scrollPane.setBorder(
            BorderFactory.createLineBorder(
                SAGE_GREEN, 2
            )
        );

        // ================= TABLE BUTTON PANEL =================

        JPanel tableButtonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 12, 8)
        );

        tableButtonPanel.setBackground(SOFT_BLUE);

        tableButtonPanel.add(deleteButton);
        tableButtonPanel.add(refreshButton);

        // ================= CENTER PANEL =================

        JPanel centerPanel = new JPanel(
            new BorderLayout(12, 12)
        );

        centerPanel.setBackground(SOFT_BLUE);

        centerPanel.add(
            formPanel,
            BorderLayout.NORTH
        );

        centerPanel.add(
            scrollPane,
            BorderLayout.CENTER
        );

        centerPanel.add(
            tableButtonPanel,
            BorderLayout.SOUTH
        );

        add(centerPanel, BorderLayout.CENTER);

        // ================= ACTION LISTENERS =================

        saveButton.addActionListener(
            e -> saveAttendance()
        );

        clearButton.addActionListener(
            e -> clearFields()
        );

        deleteButton.addActionListener(
            e -> deleteAttendance()
        );

        refreshButton.addActionListener(
            e -> refreshAll()
        );

        courseCombo.addActionListener(
            e -> loadDashboard()
        );

        dateSpinner.addChangeListener(
            e -> loadDashboard()
        );

        // Initial loading
        refreshAll();
    }

    // ================= FIELD PANEL =================

    private JPanel createFieldPanel(
        String title,
        JComponent component
    ) {

        JPanel panel = new JPanel(
            new BorderLayout(5, 5)
        );

        panel.setBackground(WHITE);

        JLabel label = new JLabel(title);

        label.setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        label.setForeground(TEXT_COLOR);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // ================= DASHBOARD CARD =================

    private JLabel createValueLabel() {

        JLabel label = new JLabel("0");

        label.setFont(
            new Font("Arial", Font.BOLD, 24)
        );

        label.setForeground(DARK_TEAL);

        label.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        return label;
    }

    private JPanel createStatCard(
        String title,
        JLabel value,
        Color background
    ) {

        JPanel card = new JPanel(
            new BorderLayout(5, 8)
        );

        card.setBackground(background);

        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    SAGE_GREEN
                ),
                BorderFactory.createEmptyBorder(
                    12, 8, 12, 8
                )
            )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
            new Font("Arial", Font.BOLD, 12)
        );

        titleLabel.setForeground(TEXT_COLOR);

        titleLabel.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    // ================= INPUT STYLE =================

    private void styleInput(JComponent component) {

        component.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );

        component.setBackground(WHITE);
        component.setForeground(TEXT_COLOR);

    }

    // ================= BUTTON STYLE =================

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
            new Font("Arial", Font.BOLD, 12)
        );

        button.setBackground(MUTED_TEAL);
        button.setForeground(WHITE);

        button.setFocusPainted(false);

        button.setBorder(
            BorderFactory.createEmptyBorder(
                10, 16, 10, 16
            )
        );

        button.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // ================= STUDENT ITEM =================

    private static class StudentItem {

        int id;
        String rollNo;
        String name;

        StudentItem(
            int id,
            String rollNo,
            String name
        ) {

            this.id = id;
            this.rollNo = rollNo;
            this.name = name;
        }

        @Override
        public String toString() {

            return rollNo + " - " + name;
        }
    }

    // ================= COURSE ITEM =================

    private static class CourseItem {

        int id;
        String name;

        CourseItem(int id, String name) {

            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {

            return name;
        }
    }

    // ================= LOAD STUDENTS =================

    private void loadStudents() {

        studentCombo.removeAllItems();

        String sql =
            "SELECT student_id, roll_no, name " +
            "FROM students ORDER BY name";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                studentCombo.addItem(
                    new StudentItem(
                        rs.getInt("student_id"),
                        rs.getString("roll_no"),
                        rs.getString("name")
                    )
                );
            }

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= LOAD COURSES =================

    private void loadCourses() {

        courseCombo.removeAllItems();

        String sql =
            "SELECT course_id, course_name " +
            "FROM courses ORDER BY course_name";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                courseCombo.addItem(
                    new CourseItem(
                        rs.getInt("course_id"),
                        rs.getString("course_name")
                    )
                );
            }

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= GET SELECTED DATE =================

    private java.sql.Date getSelectedDate() {

        java.util.Date selected =
            (java.util.Date) dateSpinner.getValue();

        return new java.sql.Date(
            selected.getTime()
        );
    }

    // ================= SAVE ATTENDANCE =================

    private void saveAttendance() {

        StudentItem student =
            (StudentItem) studentCombo.getSelectedItem();

        CourseItem course =
            (CourseItem) courseCombo.getSelectedItem();

        if (student == null) {

            JOptionPane.showMessageDialog(
                this,
                "Please select a student."
            );

            return;
        }

        if (course == null) {

            JOptionPane.showMessageDialog(
                this,
                "Please select a course."
            );

            return;
        }

        java.sql.Date attendanceDate =
            getSelectedDate();

        String status =
            (String) statusCombo.getSelectedItem();

        try (
            Connection con =
                DatabaseConnection.getConnection()
        ) {

            // Check for existing attendance
            String checkSql =
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id = ? " +
                "AND course_id = ? " +
                "AND attendance_date = ?";

            try (
                PreparedStatement check =
                    con.prepareStatement(checkSql)
            ) {

                check.setInt(1, student.id);
                check.setInt(2, course.id);
                check.setDate(3, attendanceDate);

                try (ResultSet rs = check.executeQuery()) {

                    if (rs.next() && rs.getInt(1) > 0) {

                        JOptionPane.showMessageDialog(
                            this,
                            "Attendance already exists for " +
                            student.name +
                            " in this course on " +
                            attendanceDate +
                            ". Only one attendance entry is allowed."
                        );

                        return;
                    }
                }
            }

            // Insert attendance only if it does not exist
            String insertSql =
                "INSERT INTO attendance " +
                "(student_id, course_id, attendance_date, status) " +
                "VALUES (?, ?, ?, ?)";

            try (
                PreparedStatement ps =
                    con.prepareStatement(insertSql)
            ) {

                ps.setInt(1, student.id);
                ps.setInt(2, course.id);
                ps.setDate(3, attendanceDate);
                ps.setString(4, status);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                this,
                "Attendance saved successfully!"
            );

            clearFields();
            refreshAll();

        } catch (SQLIntegrityConstraintViolationException ex) {

            JOptionPane.showMessageDialog(
                this,
                "Duplicate attendance is not allowed for this student, course and date."
            );

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= LOAD ATTENDANCE TABLE =================

    private void loadAttendance() {

        model.setRowCount(0);

        String sql =
            "SELECT a.student_id, a.course_id, " +
            "s.roll_no, s.name, c.course_name, " +
            "a.attendance_date, a.status " +
            "FROM attendance a " +
            "JOIN students s ON a.student_id = s.student_id " +
            "JOIN courses c ON a.course_id = c.course_id " +
            "ORDER BY a.attendance_date DESC";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                model.addRow(
                    new Object[]{

                        rs.getInt("student_id"),

                        rs.getInt("course_id"),

                        rs.getString("roll_no"),

                        rs.getString("name"),

                        rs.getString("course_name"),

                        rs.getDate("attendance_date"),

                        rs.getString("status")
                    }
                );
            }

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= DASHBOARD STATISTICS =================

    private void loadDashboard() {

        java.sql.Date selectedDate =
            getSelectedDate();

        try (
            Connection con =
                DatabaseConnection.getConnection()
        ) {

            // Total students
            String studentSql =
                "SELECT COUNT(*) FROM students";

            try (
                PreparedStatement ps =
                    con.prepareStatement(studentSql);

                ResultSet rs = ps.executeQuery()
            ) {

                if (rs.next()) {

                    totalStudentsValue.setText(
                        String.valueOf(rs.getInt(1))
                    );
                }
            }

            // Statistics for selected date and course
            CourseItem course =
                (CourseItem) courseCombo.getSelectedItem();

            String attendanceSql =
                "SELECT COUNT(*) AS total_records, " +
                "COALESCE(SUM(CASE WHEN status = 'Present' " +
                "THEN 1 ELSE 0 END), 0) AS present_count, " +
                "COALESCE(SUM(CASE WHEN status = 'Absent' " +
                "THEN 1 ELSE 0 END), 0) AS absent_count, " +
                "COALESCE(SUM(CASE WHEN status = 'Late' " +
                "THEN 1 ELSE 0 END), 0) AS late_count " +
                "FROM attendance WHERE attendance_date = ?";

            if (course != null) {

                attendanceSql += " AND course_id = ?";
            }

            try (
                PreparedStatement ps =
                    con.prepareStatement(attendanceSql)
            ) {

                ps.setDate(1, selectedDate);

                if (course != null) {
                    ps.setInt(2, course.id);
                }

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        int total =
                            rs.getInt("total_records");

                        int present =
                            rs.getInt("present_count");

                        int absent =
                            rs.getInt("absent_count");

                        int late =
                            rs.getInt("late_count");

                        presentValue.setText(
                            String.valueOf(present)
                        );

                        absentValue.setText(
                            String.valueOf(absent)
                        );

                        lateValue.setText(
                            String.valueOf(late)
                        );

                        double percentage = 0;

                        if (total > 0) {

                            percentage =
                                (present * 100.0) / total;
                        }

                        percentageValue.setText(
                            String.format(
                                "%.1f%%",
                                percentage
                            )
                        );
                    }
                }
            }

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= DELETE ATTENDANCE =================

    private void deleteAttendance() {

        int row =
            attendanceTable.getSelectedRow();

        if (row == -1) {

            JOptionPane.showMessageDialog(
                this,
                "Please select an attendance record."
            );

            return;
        }

        int studentId =
            (int) model.getValueAt(row, 0);

        int courseId =
            (int) model.getValueAt(row, 1);

        java.sql.Date attendanceDate =
            java.sql.Date.valueOf(
                model.getValueAt(row, 5).toString()
            );

        int choice =
            JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this attendance record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
            "DELETE FROM attendance " +
            "WHERE student_id = ? " +
            "AND course_id = ? " +
            "AND attendance_date = ?";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.setDate(3, attendanceDate);

            int deleted = ps.executeUpdate();

            if (deleted > 0) {

                JOptionPane.showMessageDialog(
                    this,
                    "Attendance deleted successfully."
                );
            }

            refreshAll();

        } catch (SQLException ex) {

            showError(ex);
        }
    }

    // ================= CLEAR FORM =================

    private void clearFields() {

        dateSpinner.setValue(
            java.sql.Date.valueOf(LocalDate.now())
        );

        statusCombo.setSelectedIndex(0);

        if (studentCombo.getItemCount() > 0) {

            studentCombo.setSelectedIndex(0);
        }

        if (courseCombo.getItemCount() > 0) {

            courseCombo.setSelectedIndex(0);
        }

        attendanceTable.clearSelection();
    }

    // ================= REFRESH =================

    private void refreshAll() {

        loadStudents();
        loadCourses();
        loadAttendance();
        loadDashboard();
    }

    // ================= ERROR MESSAGE =================

    private void showError(SQLException ex) {

        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}