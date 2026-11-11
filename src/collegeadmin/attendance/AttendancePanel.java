package collegeadmin.attendance;

import collegeadmin.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;

public class AttendancePanel extends JPanel {

    private static final double MIN_PERCENT = 75.0;

    private static class Item {
        final int id; final String label;
        Item(int id, String label) { this.id = id; this.label = label; }
        public String toString() { return label; }
    }

    private JComboBox<Item> cmbCourse = new JComboBox<>();
    private JTextField txtDate = new JTextField(LocalDate.now().toString(), 9);

    private DefaultTableModel markModel = new DefaultTableModel(
        new String[]{"Student ID", "Roll No", "Name", "Present"}, 0) {
        @Override public Class<?> getColumnClass(int c) {
            return c == 3 ? Boolean.class : Object.class;
        }
        @Override public boolean isCellEditable(int r, int c) { return c == 3; }
    };
    private DefaultTableModel pctModel = new DefaultTableModel(
        new String[]{"Roll No", "Name", "Held", "Present", "Percentage", "Status"}, 0);
    private JTable table = new JTable(markModel);

    public AttendancePanel() {
        setLayout(new BorderLayout());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Course")); row1.add(cmbCourse);
        row1.add(new JLabel("Date (yyyy-mm-dd)")); row1.add(txtDate);
        JButton btnLoad = new JButton("Load Students");
        row1.add(btnLoad);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAllP = new JButton("All Present");
        JButton btnAllA = new JButton("All Absent");
        JButton btnSave = new JButton("Save Attendance");
        JButton btnPct = new JButton("Calculate Percentage");
        JButton btnShort = new JButton("Show Shortage (<75%)");
        row2.add(btnAllP); row2.add(btnAllA); row2.add(btnSave);
        row2.add(btnPct); row2.add(btnShort);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(row1); top.add(row2);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action listeners
        btnLoad.addActionListener(e -> loadStudents());
        cmbCourse.addActionListener(e -> loadStudents());
        btnAllP.addActionListener(e -> setAll(true));
        btnAllA.addActionListener(e -> setAll(false));
        btnSave.addActionListener(e -> saveAttendance());
        btnPct.addActionListener(e -> showPercentages(false));
        btnShort.addActionListener(e -> showPercentages(true));

        // Mouse listener: double-click Roll No / Name to toggle Present
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getModel() == markModel) {
                    int r = table.rowAtPoint(e.getPoint());
                    int c = table.columnAtPoint(e.getPoint());
                    if (r >= 0 && c != 3) {
                        toggleRow(r);
                    }
                }
            }
        });

        // Key listener: P = present, A = absent for the selected row
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (table.getModel() != markModel) return;
                int r = table.getSelectedRow();
                if (r < 0) return;
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    markModel.setValueAt(Boolean.TRUE, r, 3);
                    moveDown(r);
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    markModel.setValueAt(Boolean.FALSE, r, 3);
                    moveDown(r);
                }
            }
        });

        // Key listener: Date field - digits and '-' only, Enter loads students
        txtDate.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c != '\n' && c != '-' && !Character.isDigit(c)
                        && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadStudents();
                }
            }
        });

        loadCourses();
    }

    private void toggleRow(int r) {
        boolean now = Boolean.TRUE.equals(markModel.getValueAt(r, 3));
        markModel.setValueAt(!now, r, 3);
    }

    private void moveDown(int r) {
        if (r + 1 < table.getRowCount()) {
            table.setRowSelectionInterval(r + 1, r + 1);
        }
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
        String sql = "SELECT s.student_id, s.roll_no, s.name FROM students s "
                   + "JOIN course_registration r ON r.student_id = s.student_id "
                   + "WHERE r.course_id = ? ORDER BY s.roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            markModel.setRowCount(0);
            while (rs.next()) {
                markModel.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                    rs.getString(3), Boolean.TRUE});
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void setAll(boolean value) {
        if (table.getModel() != markModel) return;
        for (int i = 0; i < markModel.getRowCount(); i++) {
            markModel.setValueAt(value, i, 3);
        }
    }

    private void saveAttendance() {
        if (table.getModel() != markModel || markModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Load students first.");
            return;
        }
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        int cid = selectedCourse();
        Date date;
        try {
            date = Date.valueOf(txtDate.getText().trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Date must look like 2026-11-11.");
            return;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM attendance WHERE course_id=? AND att_date=?");
                 PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO attendance (student_id, course_id, att_date, status) VALUES (?,?,?,?)")) {
                del.setInt(1, cid); del.setDate(2, date);
                del.executeUpdate();
                for (int i = 0; i < markModel.getRowCount(); i++) {
                    boolean present = Boolean.TRUE.equals(markModel.getValueAt(i, 3));
                    ins.setInt(1, (Integer) markModel.getValueAt(i, 0));
                    ins.setInt(2, cid);
                    ins.setDate(3, date);
                    ins.setString(4, present ? "Present" : "Absent");
                    ins.addBatch();
                }
                ins.executeBatch();
                con.commit();
                JOptionPane.showMessageDialog(this, "Attendance saved for " + date + ".");
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showPercentages(boolean shortageOnly) {
        int cid = selectedCourse();
        if (cid < 0) return;
        String sql = "SELECT s.roll_no, s.name, "
                   + "(SELECT COUNT(DISTINCT att_date) FROM attendance WHERE course_id = ?) AS held, "
                   + "COALESCE(SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END), 0) AS present "
                   + "FROM students s "
                   + "JOIN course_registration r ON r.student_id = s.student_id AND r.course_id = ? "
                   + "LEFT JOIN attendance a ON a.student_id = s.student_id AND a.course_id = ? "
                   + "GROUP BY s.student_id, s.roll_no, s.name ORDER BY s.roll_no";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid); ps.setInt(2, cid); ps.setInt(3, cid);
            ResultSet rs = ps.executeQuery();
            table.setModel(pctModel);
            pctModel.setRowCount(0);
            while (rs.next()) {
                int held = rs.getInt(3);
                int present = rs.getInt(4);
                double pct = held == 0 ? 0 : present * 100.0 / held;
                boolean shortage = held > 0 && pct < MIN_PERCENT;
                if (shortageOnly && !shortage) continue;
                pctModel.addRow(new Object[]{rs.getString(1), rs.getString(2), held, present,
                    String.format("%.1f%%", pct), shortage ? "SHORTAGE" : "OK"});
            }
            if (pctModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, shortageOnly
                    ? "No students below " + MIN_PERCENT + "%."
                    : "No attendance recorded yet.");
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
}