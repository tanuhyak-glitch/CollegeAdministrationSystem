package collegeadmin.examination;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ExaminationPanel extends JPanel {
	private static final long serialVersionUID = 1L;

    private static class Item {
        final int id; final String label;
        Item(int id, String label) { this.id = id; this.label = label; }
        public String toString() { return label; }
    }

    private JComboBox<Item> cmbCourse = new JComboBox<>();
    private JTextField txtRoll = new JTextField(12);
    private JTextField txtMarks = new JTextField(5);
    private DefaultTableModel markModel = new DefaultTableModel(
        new String[]{"Student ID", "Roll No", "Name", "Marks", "Grade"}, 0);
    private DefaultTableModel resultModel = new DefaultTableModel(
        new String[]{"Roll No", "Name", "Subjects", "Total", "Percentage", "Result"}, 0);
    private JTable table = new JTable(markModel);

    public ExaminationPanel() {
        setLayout(new BorderLayout());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Course")); row1.add(cmbCourse);
        JButton btnLoad = new JButton("Load Students");
        row1.add(btnLoad);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Roll No")); row2.add(txtRoll);
        row2.add(new JLabel("Marks (0-100)")); row2.add(txtMarks);
        JButton btnSave = new JButton("Save Marks");
        JButton btnGrades = new JButton("Generate Grades");
        JButton btnResults = new JButton("Calculate Results");
        row2.add(btnSave); row2.add(btnGrades); row2.add(btnResults);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(row1); top.add(row2);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action listeners
        btnLoad.addActionListener(e -> loadStudents());
        cmbCourse.addActionListener(e -> loadStudents());
        btnSave.addActionListener(e -> saveMarks());
        btnGrades.addActionListener(e -> generateGrades());
        btnResults.addActionListener(e -> calculateResults());

        // Mouse listener: click a row to fill Roll No and Marks
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0 && table.getModel() == markModel) {
                    txtRoll.setText(String.valueOf(markModel.getValueAt(r, 1)));
                    Object m = markModel.getValueAt(r, 3);
                    txtMarks.setText(m == null ? "" : String.valueOf(m));
                    txtMarks.requestFocusInWindow();
                }
            }
        });

        // Key listener: Roll No - upper case, Enter jumps to Marks
        txtRoll.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtMarks.requestFocusInWindow();
                } else {
                    int pos = txtRoll.getCaretPosition();
                    txtRoll.setText(txtRoll.getText().toUpperCase());
                    txtRoll.setCaretPosition(Math.min(pos, txtRoll.getText().length()));
                }
            }
        });

        // Key listener: Marks - digits only, Enter saves
        txtMarks.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c != '\n' && !Character.isDigit(c)) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveMarks();
                }
            }
        });

        loadCourses();
    }

    private static String gradeFor(int m) {
        if (m >= 90) return "O";
        if (m >= 80) return "A+";
        if (m >= 70) return "A";
        if (m >= 60) return "B+";
        if (m >= 50) return "B";
        if (m >= 40) return "C";
        return "F";
    }

    private void loadCourses() {
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT course_id, course_name FROM courses")) {
            while (rs.next()) {
                cmbCourse.addItem(new Item(rs.getInt(1), rs.getInt(1) + " - " + rs.getString(2)));
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private int selectedCourse() {
        Item it = (Item) cmbCourse.getSelectedItem();
        return it == null ? -1 : it.id;
    }

    private void loadStudents() {
        int cid = selectedCourse();
        if (cid < 0) return;
        table.setModel(markModel);
        String sql = "SELECT s.student_id, s.roll_no, s.name, m.marks_obtained, m.grade "
                   + "FROM students s "
                   + "JOIN course_registration r ON r.student_id = s.student_id AND r.course_id = ? "
                   + "LEFT JOIN marks m ON m.student_id = s.student_id AND m.course_id = ? "
                   + "ORDER BY s.roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid); ps.setInt(2, cid);
            ResultSet rs = ps.executeQuery();
            markModel.setRowCount(0);
            while (rs.next()) {
                markModel.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                    rs.getString(3), rs.getObject(4), rs.getString(5)});
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void saveMarks() {
        int cid = selectedCourse();
        String roll = txtRoll.getText().trim();
        if (cid < 0 || roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a course and enter a roll number.");
            return;
        }
        try {
            int marks = Integer.parseInt(txtMarks.getText().trim());
            if (marks < 0 || marks > 100) {
                JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100.");
                return;
            }
            try (Connection con = DatabaseConnection.getConnection()) {
                int sid = -1;
                try (PreparedStatement p = con.prepareStatement(
                        "SELECT student_id FROM students WHERE roll_no=?")) {
                    p.setString(1, roll);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) sid = rs.getInt(1);
                }
                if (sid < 0) {
                    JOptionPane.showMessageDialog(this, "No student with that roll number.");
                    return;
                }
                int markId = -1;
                try (PreparedStatement p = con.prepareStatement(
                        "SELECT mark_id FROM marks WHERE student_id=? AND course_id=?")) {
                    p.setInt(1, sid); p.setInt(2, cid);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) markId = rs.getInt(1);
                }
                if (markId > 0) {
                    try (PreparedStatement p = con.prepareStatement(
                            "UPDATE marks SET marks_obtained=?, grade=? WHERE mark_id=?")) {
                        p.setInt(1, marks); p.setString(2, gradeFor(marks)); p.setInt(3, markId);
                        p.executeUpdate();
                    }
                } else {
                    try (PreparedStatement p = con.prepareStatement(
                            "INSERT INTO marks (student_id, course_id, marks_obtained, grade) VALUES (?,?,?,?)")) {
                        p.setInt(1, sid); p.setInt(2, cid); p.setInt(3, marks); p.setString(4, gradeFor(marks));
                        p.executeUpdate();
                    }
                }
            }
            txtRoll.setText(""); txtMarks.setText("");
            loadStudents();
            txtRoll.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Marks must be a number.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void generateGrades() {
        int cid = selectedCourse();
        if (cid < 0) return;
        String sql = "UPDATE marks SET grade = CASE "
                   + "WHEN marks_obtained >= 90 THEN 'O' WHEN marks_obtained >= 80 THEN 'A+' "
                   + "WHEN marks_obtained >= 70 THEN 'A' WHEN marks_obtained >= 60 THEN 'B+' "
                   + "WHEN marks_obtained >= 50 THEN 'B' WHEN marks_obtained >= 40 THEN 'C' "
                   + "ELSE 'F' END WHERE course_id=? AND marks_obtained IS NOT NULL";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            int n = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Grades generated for " + n + " students.");
            loadStudents();
        } catch (SQLException ex) { showError(ex); }
    }

    private void calculateResults() {
        String sql = "SELECT s.roll_no, s.name, COUNT(m.mark_id), SUM(m.marks_obtained), MIN(m.marks_obtained) "
                   + "FROM students s JOIN marks m ON m.student_id = s.student_id "
                   + "GROUP BY s.student_id, s.roll_no, s.name ORDER BY SUM(m.marks_obtained) DESC";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            int totalCourses = 0;
            try (ResultSet rc = st.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (rc.next()) totalCourses = rc.getInt(1);
            }
            table.setModel(resultModel);
            resultModel.setRowCount(0);
            try (ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    int subjects = rs.getInt(3);
                    int total = rs.getInt(4);
                    int min = rs.getInt(5);
                    double pct = subjects == 0 ? 0 : (double) total / subjects;
                    String result = subjects < totalCourses ? "INCOMPLETE"
                                  : (min >= 40 ? "PASS" : "FAIL");
                    resultModel.addRow(new Object[]{rs.getString(1), rs.getString(2),
                        subjects, total, String.format("%.2f%%", pct), result});
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
}