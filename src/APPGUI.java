import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

 class AppGUI extends JFrame implements ActionListener {

    private final JLabel studentIdLabel, firstNameLabel, lastNameLabel, majorLabel, phoneLabel, gpaLabel, dobLabel;
    private final JTextField studentIdField, firstNameField, lastNameField, majorField, phoneField, gpaField, dobField;
    private final JButton addButton, displayButton, sortButton, searchButton, modifyButton;
    private Statement stmt;

    public AppGUI() {
        super("Student Database"); // title of JFrame
        JPanel panel = new JPanel();

        // Initialize labels
        studentIdLabel = new JLabel("Student ID:");
        firstNameLabel = new JLabel("First Name:");
        lastNameLabel = new JLabel("Last Name:");
        majorLabel = new JLabel("Major:");
        phoneLabel = new JLabel("Phone:");
        gpaLabel = new JLabel("GPA:");
        dobLabel = new JLabel("Date of Birth (yyyy-mm-dd):");

        // Initialize text fields
        studentIdField = new JTextField(10);
        firstNameField = new JTextField(10);
        lastNameField = new JTextField(10);
        majorField = new JTextField(10);
        phoneField = new JTextField(10);
        gpaField = new JTextField(10);
        dobField = new JTextField(10);

        // Initialize buttons
        addButton = new JButton("Add");
        displayButton = new JButton("Display");
        sortButton = new JButton("Sort");
        searchButton = new JButton("Search");
        modifyButton = new JButton("Modify");

        // Add action listeners
        addButton.addActionListener(this);
        displayButton.addActionListener(this);
        sortButton.addActionListener(this);
        searchButton.addActionListener(this);
        modifyButton.addActionListener(this);

        // Add components to panel
        panel.add(studentIdLabel);
        panel.add(studentIdField);
        panel.add(firstNameLabel);
        panel.add(firstNameField);
        panel.add(lastNameLabel);
        panel.add(lastNameField);
        panel.add(majorLabel);
        panel.add(majorField);
        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(gpaLabel);
        panel.add(gpaField);
        panel.add(dobLabel);
        panel.add(dobField);
        panel.add(addButton);
        panel.add(displayButton);
        panel.add(sortButton);
        panel.add(searchButton);
        panel.add(modifyButton);

        // Add panel to frame
        this.add(panel);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dbConnect db = new dbConnect();
        Connection conn;
        try {
            conn = db.getConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + ex.getMessage());
            return;
        }

        try {
            stmt = conn.createStatement();
        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(this, "Could not create statement: " + e1.getMessage());
            return;
        }

        Table tb = new Table();

        if (e.getSource() == addButton) {
            // Insert new student into database
            String sql = "INSERT INTO students VALUES('" + studentIdField.getText() + "', '"
                    + firstNameField.getText() + "', '" + lastNameField.getText() + "', '"
                    + majorField.getText() + "', '" + phoneField.getText() + "', '"
                    + gpaField.getText() + "', '" + dobField.getText() + "')";
            try {
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "Student added successfully.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage());
            }

        } else if (e.getSource() == displayButton) {
            // Display all data
            String sql = "SELECT * FROM students";
            try {
                ResultSet rs = stmt.executeQuery(sql);
                JTable table = new JTable(tb.buildTableModel(rs));
                JOptionPane.showMessageDialog(this, new JScrollPane(table));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error displaying data: " + ex.getMessage());
            }

        } else if (e.getSource() == sortButton) {
            // Sort data
            String[] options = {"First Name", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Sort by:", "Sort",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            String sql = switch (choice) {
                case 0 -> "SELECT * FROM students ORDER BY first_name";
                case 1 -> "SELECT * FROM students ORDER BY last_name";
                case 2 -> "SELECT * FROM students ORDER BY major";
                default -> "";
            };
            if (!sql.isEmpty()) {
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    JTable table = new JTable(tb.buildTableModel(rs));
                    JOptionPane.showMessageDialog(this, new JScrollPane(table));
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error sorting data: " + ex.getMessage());
                }
            }

        } else if (e.getSource() == searchButton) {
            // Search data
            String[] options = {"Student ID", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Search by:", "Search",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            String column = switch (choice) {
                case 0 -> "student_id";
                case 1 -> "last_name";
                case 2 -> "major";
                default -> "";
            };
            String searchTerm = JOptionPane.showInputDialog(this, "Enter search term:");
            String sql = "SELECT * FROM students WHERE " + column + " LIKE '%" + searchTerm + "%'";
            try {
                ResultSet rs = stmt.executeQuery(sql);
                JTable table = new JTable(tb.buildTableModel(rs));
                JOptionPane.showMessageDialog(this, new JScrollPane(table));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error searching data: " + ex.getMessage());
            }

        } else if (e.getSource() == modifyButton) {
            // Modify data
            String studentId = JOptionPane.showInputDialog(this, "Enter student ID:");
            String sql = "SELECT * FROM students WHERE student_id = '" + studentId + "'";
            try {
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    String[] options = {"First Name", "Last Name", "Major", "Phone", "GPA", "Date of Birth"};
                    int choice = JOptionPane.showOptionDialog(this, "Select field to modify:", "Modify",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                    String column = switch (choice) {
                        case 0 -> "first_name";
                        case 1 -> "last_name";
                        case 2 -> "major";
                        case 3 -> "phone";
                        case 4 -> "gpa";
                        case 5 -> "date_of_birth";
                        default -> "";
                    };
                    String newValue = JOptionPane.showInputDialog(this, "Enter new value:");
                    sql = "UPDATE students SET " + column + " = '" + newValue + "' WHERE student_id = '" + studentId + "'";
                    stmt.executeUpdate(sql);
                    JOptionPane.showMessageDialog(this, "Student data updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error modifying data: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppGUI::new);
    }
}
