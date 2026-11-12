package collegeadmin.attendance;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Identifies students whose attendance percentage is below the required minimum.
 */
public class AttendanceShortagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color SHORT_BG = new Color(253, 224, 224);
    private static final Color OK_BG = Color.WHITE;

    private JComboBox<CourseItem> courseCombo = new JComboBox<>();
    private JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(75, 1, 100, 1));
    private JRadioButton rbShortage = new JRadioButton("Shortage only", true);
    private JRadioButton rbAll = new JRadioButton("All students");
    private JCheckBox chkLate = new JCheckBox("Count 'Late' as present");

    private JLabel summary = new JLabel(" ");

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"Roll No", "Name", "Course", "Classes", "Attended", "Percentage", "Status"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable table = new JTable(model);

    public AttendanceShortagePanel() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Attendance Shortage");
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.add(new JLabel("Course:"));
        controls.add(courseCombo);
        controls.add(new JLabel("Minimum %:"));
        controls.add(minSpinner);

        ButtonGroup group = new ButtonGroup();
        group.add(rbShortage);
        group.add(rbAll);
        controls.add(rbShortage);
        controls.add(rbAll);
        controls.add(chkLate);

        JButton generate = new JButton("Generate");
        controls.add(generate);

        JPanel north = new JPanel(new BorderLayout());
        north.add(heading, BorderLayout.NORTH);
        north.add(controls, BorderLayout.CENTER);

        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel) {
                    int m = t.convertRowIndexToModel(row);
                    c.setBackground("SHORTAGE".equals(model.getValueAt(m, 6)) ? SHORT_BG : OK_BG);
                    c.setForeground(Color.DARK_GRAY);
                }
                return c;
            }
        });

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);

        loadCourses();

        generate.addActionListener(e -> generateReport());
        rbShortage.addActionListener(e -> generateReport());
        rbAll.addActionListener(e -> generateReport());
        chkLate.addActionListener(e -> generateReport());
        courseCombo.addActionListener(e -> generateReport());
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        courseCombo.addItem(new CourseItem(0, "All Courses"));
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT course_id, course_name FROM courses ORDER BY course_name")) {
            while (rs.next()) {
                courseCombo.addItem(new CourseItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void generateReport() {

        CourseItem course = (CourseItem) courseCombo.getSelectedItem();
        if (course == null) return;

        int minimum = (Integer) minSpinner.getValue();
        boolean lateCounts = chkLate.isSelected();
        boolean onlyShortage = rbShortage.isSelected();

        String sql = "SELECT s.roll_no, s.name, c.course_name, COUNT(*) AS total, "
                   + "SUM(CASE WHEN a.status = 'Present' OR (? = 1 AND a.status = 'Late') "
                   + "THEN 1 ELSE 0 END) AS attended "
                   + "FROM attendance a "
                   + "JOIN students s ON s.student_id = a.student_id "
                   + "JOIN courses c ON c.course_id = a.course_id "
                   + (course.id > 0 ? "WHERE a.course_id = ? " : "")
                   + "GROUP BY s.student_id, s.roll_no, s.name, c.course_id, c.course_name "
                   + "ORDER BY s.roll_no, c.course_name";

        model.setRowCount(0);
        int shortage = 0, checked = 0;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, lateCounts ? 1 : 0);
            if (course.id > 0) ps.setInt(2, course.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("total");
                    int attended = rs.getInt("attended");
                    double pct = total == 0 ? 0 : attended * 100.0 / total;
                    boolean isShort = pct < minimum;

                    checked++;
                    if (isShort) shortage++;
                    if (onlyShortage && !isShort) continue;

                    model.addRow(new Object[]{
                            rs.getString("roll_no"), rs.getString("name"), rs.getString("course_name"),
                            total, attended, String.format("%.1f%%", pct),
                            isShort ? "SHORTAGE" : "OK"
                    });
                }
            }
            summary.setText("  " + shortage + " of " + checked
                    + " student-course records are below " + minimum + "%.");

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class CourseItem {
        final int id;
        final String name;
        CourseItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
