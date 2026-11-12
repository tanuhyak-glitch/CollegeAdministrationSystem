package collegeadmin.attendance;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceReportPanel extends JPanel {

    private static final Color SOFT_BLUE = new Color(234, 242, 248);
    private static final Color SAGE_GREEN = new Color(220, 233, 223);
    private static final Color MUTED_TEAL = new Color(86, 143, 139);
    private static final Color DARK_TEAL = new Color(48, 91, 91);
    private static final Color WHITE = Color.WHITE;

    private JComboBox<StudentItem> studentCombo;
    private JComboBox<CourseItem> courseCombo;

    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;

    private JLabel totalLabel;
    private JLabel presentLabel;
    private JLabel absentLabel;
    private JLabel lateLabel;
    private JLabel percentageLabel;

    private JTable reportTable;
    private DefaultTableModel tableModel;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd");

    public AttendanceReportPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(SOFT_BLUE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        createInterface();
        loadStudents();
        loadCourses();
    }

    private void createInterface() {

        JLabel heading = new JLabel("Student Attendance Report");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(DARK_TEAL);

        JLabel subtitle = new JLabel(
                "View attendance records and calculate attendance percentage"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setBackground(SOFT_BLUE);
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 225)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 7, 6, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(190, 32));
        studentCombo.setMaximumRowCount(8);

        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(170, 32));
        courseCombo.setMaximumRowCount(8);

        fromDateSpinner = createDateSpinner();
        toDateSpinner = createDateSpinner();

        JButton generateButton = createButton("Generate Report");
        JButton printButton = createButton("Print Report");
        JButton clearButton = createButton("Clear");

        addFilter(filterPanel, gbc, 0, 0, "Select Student", studentCombo);
        addFilter(filterPanel, gbc, 1, 0, "Select Course", courseCombo);
        addFilter(filterPanel, gbc, 2, 0, "From Date", fromDateSpinner);
        addFilter(filterPanel, gbc, 3, 0, "To Date", toDateSpinner);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        filterPanel.add(generateButton, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        filterPanel.add(printButton, gbc);

        gbc.gridx = 3;
        filterPanel.add(clearButton, gbc);

        JPanel topPanel = new JPanel(new BorderLayout(10, 12));
        topPanel.setBackground(SOFT_BLUE);
        topPanel.add(headingPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        JPanel statisticsPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        statisticsPanel.setBackground(SOFT_BLUE);

        totalLabel = new JLabel("0", SwingConstants.CENTER);
        presentLabel = new JLabel("0", SwingConstants.CENTER);
        absentLabel = new JLabel("0", SwingConstants.CENTER);
        lateLabel = new JLabel("0", SwingConstants.CENTER);
        percentageLabel = new JLabel("0%", SwingConstants.CENTER);

        statisticsPanel.add(createStatCard("Total Classes", totalLabel, SOFT_BLUE));
        statisticsPanel.add(createStatCard("Present", presentLabel, SAGE_GREEN));
        statisticsPanel.add(createStatCard("Absent", absentLabel, new Color(250, 225, 225)));
        statisticsPanel.add(createStatCard("Late", lateLabel, new Color(250, 241, 215)));
        statisticsPanel.add(createStatCard("Attendance Rate", percentageLabel, new Color(220, 235, 235)));

        String[] columns = {
                "Date", "Roll Number", "Student Name",
                "Course", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(28);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        reportTable.getTableHeader().setBackground(DARK_TEAL);
        reportTable.getTableHeader().setForeground(WHITE);

        JScrollPane tableScroll = new JScrollPane(reportTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                "Attendance Details"
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 12));
        centerPanel.setBackground(SOFT_BLUE);
        centerPanel.add(statisticsPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateReport());
        printButton.addActionListener(e -> printReport());
        clearButton.addActionListener(e -> clearReport());
    }

    private void addFilter(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String labelText,
            JComponent component) {

        gbc.gridx = x;
        gbc.gridy = y;

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));
        fieldPanel.add(component);

        panel.add(fieldPanel, gbc);
    }

    private JPanel createStatCard(
            String title,
            JLabel valueLabel,
            Color background) {

        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 215)),
                BorderFactory.createEmptyBorder(13, 8, 13, 8)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(DARK_TEAL);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        valueLabel.setForeground(DARK_TEAL);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH)
        );

        JSpinner.DateEditor editor =
                new JSpinner.DateEditor(spinner, "yyyy-MM-dd");

        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(140, 32));

        return spinner;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(MUTED_TEAL);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadStudents() {
        studentCombo.removeAllItems();

        String sql = "SELECT student_id, roll_no, name FROM students ORDER BY name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                studentCombo.addItem(new StudentItem(
                        rs.getInt("student_id"),
                        rs.getString("roll_no"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        courseCombo.addItem(new CourseItem(0, "All Courses"));

        String sql = "SELECT course_id, course_name FROM courses ORDER BY course_name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                courseCombo.addItem(new CourseItem(
                        rs.getInt("course_id"),
                        rs.getString("course_name")
                ));
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void generateReport() {

        StudentItem student =
                (StudentItem) studentCombo.getSelectedItem();

        CourseItem course =
                (CourseItem) courseCombo.getSelectedItem();

        if (student == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student.",
                    "Select Student",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (course == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a course.",
                    "Select Course",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Date fromDate = (Date) fromDateSpinner.getValue();
        Date toDate = (Date) toDateSpinner.getValue();

        if (fromDate.after(toDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "From Date cannot be later than To Date.",
                    "Invalid Date Range",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        tableModel.setRowCount(0);

        int total = 0;
        int present = 0;
        int absent = 0;
        int late = 0;

        String sql =
                "SELECT a.attendance_date, s.roll_no, s.name, " +
                "c.course_name, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.student_id " +
                "JOIN courses c ON a.course_id = c.course_id " +
                "WHERE a.student_id = ? " +
                "AND a.attendance_date BETWEEN ? AND ? " +
                "AND (? = 0 OR a.course_id = ?) " +
                "ORDER BY a.attendance_date DESC, c.course_name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, student.id);
            pst.setDate(2, new java.sql.Date(fromDate.getTime()));
            pst.setDate(3, new java.sql.Date(toDate.getTime()));
            pst.setInt(4, course.id);
            pst.setInt(5, course.id);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Date attendanceDate =
                            rs.getDate("attendance_date");

                    String status = rs.getString("status");

                    tableModel.addRow(new Object[]{
                            dateFormat.format(attendanceDate),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course_name"),
                            status
                    });

                    total++;

                    if (status != null) {
                        if (status.equalsIgnoreCase("Present")) {
                            present++;
                        } else if (status.equalsIgnoreCase("Absent")) {
                            absent++;
                        } else if (status.equalsIgnoreCase("Late")) {
                            late++;
                        }
                    }
                }
            }

            double percentage = total == 0
                    ? 0
                    : (present * 100.0) / total;

            totalLabel.setText(String.valueOf(total));
            presentLabel.setText(String.valueOf(present));
            absentLabel.setText(String.valueOf(absent));
            lateLabel.setText(String.valueOf(late));
            percentageLabel.setText(
                    String.format("%.2f%%", percentage)
            );

            if (total == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No attendance records found for the selected filters.",
                        "No Records",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void printReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Generate a report before printing.",
                    "No Report",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            boolean printed = reportTable.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("Student Attendance Report"),
                    new java.text.MessageFormat("Page {0}")
            );

            if (printed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Report sent to printer."
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to print report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearReport() {
        tableModel.setRowCount(0);

        totalLabel.setText("0");
        presentLabel.setText("0");
        absentLabel.setText("0");
        lateLabel.setText("0");
        percentageLabel.setText("0%");

        fromDateSpinner.setValue(new Date());
        toDateSpinner.setValue(new Date());

        if (studentCombo.getItemCount() > 0) {
            studentCombo.setSelectedIndex(0);
        }

        courseCombo.setSelectedIndex(0);
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Database Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }

    private static class StudentItem {
        int id;
        String rollNo;
        String name;

        StudentItem(int id, String rollNo, String name) {
            this.id = id;
            this.rollNo = rollNo;
            this.name = name;
        }

        @Override
        public String toString() {
            return rollNo + " - " + name;
        }
    }

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
}