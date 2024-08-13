import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ComplaintManagerApp extends JFrame {

    private static Connection conn;

    public static void main(String[] args) {
        // Establish the database connection
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "vishnu");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Display the login interface
        SwingUtilities.invokeLater(() -> new ComplaintManagerApp().createLoginInterface());
    }

    private void createLoginInterface() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 200);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        loginFrame.add(panel);

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField();

        JButton loginButton = new JButton("Login");

        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        panel.add(new JLabel());  // Empty label for alignment
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText().trim();
                String password = new String(passwordText.getPassword()).trim();

                if (authenticate(username, password)) {
                    loginFrame.dispose();
                    displayComplaintTables();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginFrame.setVisible(true);
    }

    private static boolean authenticate(String username, String password) {
        try {
            String query = "SELECT * FROM manager WHERE TRIM(LOWER(username)) = TRIM(LOWER(?)) AND TRIM(password) = TRIM(?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void displayComplaintTables() {
        JFrame frame = new JFrame("Complaints Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel for ComplaintsIssued Table
        JPanel issuedPanel = new JPanel(new BorderLayout());
        JTable issuedTable = new JTable();
        DefaultTableModel issuedModel = new DefaultTableModel();
        issuedTable.setModel(issuedModel);
        JScrollPane issuedScrollPane = new JScrollPane(issuedTable);
        issuedPanel.add(issuedScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Complaints Issued", issuedPanel);

        // Panel for ComplaintsSolved Table
        JPanel solvedPanel = new JPanel(new BorderLayout());
        JTable solvedTable = new JTable();
        DefaultTableModel solvedModel = new DefaultTableModel();
        solvedTable.setModel(solvedModel);
        JScrollPane solvedScrollPane = new JScrollPane(solvedTable);
        solvedPanel.add(solvedScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Complaints Solved", solvedPanel);

        // Load data into the tables
        loadTableData(issuedModel, "ComplaintsIssued");
        loadTableData(solvedModel, "ComplaintsSolved");

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private void loadTableData(DefaultTableModel model, String tableName) {
        try {
            String query = "SELECT * FROM " + tableName;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Set table columns
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Populate table rows
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                model.addRow(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
