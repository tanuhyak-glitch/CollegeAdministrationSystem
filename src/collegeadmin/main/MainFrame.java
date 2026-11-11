package collegeadmin.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("College Administration System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Are you sure you want to exit?",
                    "Exit",
                    JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        JMenuBar menuBar = new JMenuBar();

        JMenu studentMenu = new JMenu("Student");
        JMenuItem manageStudents = new JMenuItem("Manage Students");
        manageStudents.addActionListener(e ->
            showPanel(new collegeadmin.student.StudentPanel()));
        studentMenu.add(manageStudents);
        menuBar.add(studentMenu);

        JMenu facultyMenu = new JMenu("Faculty");
        JMenuItem manageFaculty = new JMenuItem("Manage Faculty");
        manageFaculty.addActionListener(e ->
            showPanel(new collegeadmin.faculty.FacultyPanel()));
        facultyMenu.add(manageFaculty);
        menuBar.add(facultyMenu);

        JMenu courseMenu = new JMenu("Course");
        JMenuItem manageCourses = new JMenuItem("Manage Courses");
        manageCourses.addActionListener(e ->
            showPanel(new collegeadmin.course.CoursePanel()));
        courseMenu.add(manageCourses);
        menuBar.add(courseMenu);

        JMenu examMenu = new JMenu("Examination");
        JMenuItem manageExams = new JMenuItem("Marks and Results");
        manageExams.addActionListener(e ->
            showPanel(new collegeadmin.examination.ExaminationPanel()));
        examMenu.add(manageExams);
        menuBar.add(examMenu);

        JMenu attMenu = new JMenu("Attendance");
        JMenuItem manageAtt = new JMenuItem("Record and Reports");
        manageAtt.addActionListener(e ->
            showPanel(new collegeadmin.attendance.AttendancePanel()));
        attMenu.add(manageAtt);
        menuBar.add(attMenu);

        setJMenuBar(menuBar);

        JLabel title = new JLabel("College Administration System",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, BorderLayout.CENTER);
    }

    private void showPanel(JPanel panel) {
        getContentPane().removeAll();
        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new MainFrame().setVisible(true));
    }
}