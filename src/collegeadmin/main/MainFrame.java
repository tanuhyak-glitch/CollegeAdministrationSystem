package collegeadmin.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

    private JLabel status =
        new JLabel("  Welcome to College Administration System");

    // Colour palette
    private final Color background = new Color(238, 245, 249);
    private final Color accent = new Color(71, 124, 120);
    private final Color darkText = new Color(43, 62, 76);
    private final Color white = Color.WHITE;

    public MainFrame() {

        setTitle("College Administration System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(background);
        setLayout(new BorderLayout());

        // Window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                int choice = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(white);
        menuBar.setBorder(
            BorderFactory.createMatteBorder(
                0, 0, 1, 0,
                new Color(215, 228, 231)
            )
        );

        JMenuItem home = new JMenuItem("Home");
        styleMenuItem(home);
        home.addActionListener(e -> showHome());
        menuBar.add(home);

        menuBar.add(makeMenu("Student", "Manage Students",
            () -> new collegeadmin.student.StudentPanel()));

        menuBar.add(makeMenu("Faculty", "Manage Faculty",
            () -> new collegeadmin.faculty.FacultyPanel()));

        menuBar.add(makeMenu("Course", "Manage Courses",
            () -> new collegeadmin.course.CoursePanel()));

        menuBar.add(makeMenu("Examination", "Marks and Results",
            () -> new collegeadmin.examination.ExaminationPanel()));

        menuBar.add(makeMenu("Attendance", "Record and Reports",
            () -> new collegeadmin.attendance.AttendancePanel()));
        
        menuBar.add(makeMenu("Attendance Report", "Student-wise Report",
                () -> new collegeadmin.attendance.AttendanceReportPanel()));

        setJMenuBar(menuBar);

        // Status bar
        status.setOpaque(true);
        status.setBackground(new Color(221, 236, 232));
        status.setForeground(darkText);
        status.setBorder(
            new EmptyBorder(10, 12, 10, 12)
        );
        status.setFont(new Font("Arial", Font.PLAIN, 13));

        add(status, BorderLayout.SOUTH);

        showHome();
    }

    // Home screen
    private void showHome() {

        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setBackground(background);

        JPanel content = new JPanel();
        content.setLayout(
            new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.setBackground(white);

        content.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(215, 228, 231), 1
                ),
                new EmptyBorder(45, 55, 45, 55)
            )
        );

        JLabel title = new JLabel(
            "College Administration System"
        );

        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(accent);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
            "A Simple and Efficient College Management Solution"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(darkText);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel modules = new JLabel(
            "STUDENT  |  FACULTY  |  COURSES  |  ATTENDANCE  |  ATTENDANCE REPORTS | EXAMINATIONS"
        );

        modules.setFont(new Font("Arial", Font.BOLD, 12));
        modules.setForeground(accent);
        modules.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(18));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(modules);

        homePanel.add(content);

        getContentPane().removeAll();

        add(homePanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        status.setText("  Home");

        revalidate();
        repaint();
    }

    // Create menu
    private JMenu makeMenu(
        String menuName,
        String itemName,
        Supplier<JPanel> panelMaker
    ) {

        JMenu menu = new JMenu(menuName);

        menu.setForeground(darkText);
        menu.setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        JMenuItem item = new JMenuItem(itemName);

        styleMenuItem(item);

        item.addActionListener(e -> {

            JPanel panel = panelMaker.get();

            showPanel(panel);

            status.setText("  " + itemName);
        });

        menu.add(item);

        return menu;
    }

    // Menu item styling
    private void styleMenuItem(JMenuItem item) {

        item.setBackground(white);
        item.setForeground(darkText);

        item.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );
    }

    // Display selected panel
    private void showPanel(JPanel panel) {

        getContentPane().removeAll();

        panel.setBackground(background);

        add(panel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    // Global colour theme
    private static void applyTheme() {

        try {

            for (UIManager.LookAndFeelInfo info :
                    UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {

                    UIManager.setLookAndFeel(
                        info.getClassName()
                    );

                    break;
                }
            }

        } catch (Exception ex) {

            System.out.println(
                "Using default appearance."
            );
        }

        Color bg = new Color(238, 245, 249);
        Color teal = new Color(71, 124, 120);
        Color text = new Color(43, 62, 76);
        Color white = Color.WHITE;

        UIManager.put("control", bg);
        UIManager.put("Panel.background", bg);
        UIManager.put("Label.foreground", text);

        UIManager.put("Button.background", teal);
        UIManager.put("Button.foreground", white);
        UIManager.put(
            "Button.font",
            new Font("Arial", Font.BOLD, 12)
        );

        UIManager.put("TextField.background", white);
        UIManager.put("TextField.foreground", text);
        UIManager.put("TextField.caretForeground", teal);

        UIManager.put("Table.background", white);
        UIManager.put("Table.foreground", text);

        UIManager.put(
            "Table.selectionBackground",
            new Color(205, 228, 223)
        );

        UIManager.put("Table.selectionForeground", text);

        UIManager.put(
            "Table.gridColor",
            new Color(225, 233, 236)
        );

        UIManager.put(
            "TableHeader.background",
            new Color(221, 236, 232)
        );

        UIManager.put("TableHeader.foreground", text);

        UIManager.put(
            "TableHeader.font",
            new Font("Arial", Font.BOLD, 12)
        );

        UIManager.put("OptionPane.background", bg);
        UIManager.put("OptionPane.messageForeground", text);
    }

    // Main method
    public static void main(String[] args) {

        applyTheme();

        SwingUtilities.invokeLater(() -> {

            MainFrame frame = new MainFrame();

            frame.setVisible(true);
        });
    }
}
