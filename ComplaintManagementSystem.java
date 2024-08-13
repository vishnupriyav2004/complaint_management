import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ComplaintManagementSystem extends JFrame {
    private JTextField idField, userIdField, usernameField;
    private JTextArea complaintDetailsArea;
    private JButton submitComplaintButton, resolveComplaintButton;
    private Connection connection;

    public ComplaintManagementSystem() {
        // Set up the GUI
        setTitle("Complaint Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        // Create UI components
        idField = new JTextField();
        userIdField = new JTextField();
        usernameField = new JTextField();
        complaintDetailsArea = new JTextArea(4, 20);
        submitComplaintButton = new JButton("Submit Complaint");
        resolveComplaintButton = new JButton("Resolve Complaint");

        // Add components to the frame
        add(new JLabel("Complaint ID:"));
        add(idField);
        add(new JLabel("User ID:"));
        add(userIdField);
        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Complaint Details:"));
        add(new JScrollPane(complaintDetailsArea));
        add(submitComplaintButton);
        add(resolveComplaintButton);

        // Add action listeners
        submitComplaintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitComplaint();
            }
        });

        resolveComplaintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolveComplaint();
            }
        });

        // Establish database connection
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "username", "password");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitComplaint() {
        try {
            String id = idField.getText();
            String userId = userIdField.getText();
            String username = usernameField.getText();
            String complaintDetails = complaintDetailsArea.getText();

            String sql = "INSERT INTO ComplaintsIssued (id, user_id, username, complaint_details) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(id));
            preparedStatement.setInt(2, Integer.parseInt(userId));
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, complaintDetails);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Complaint submitted successfully.");

            // Clear the fields after submission
            idField.setText("");
            userIdField.setText("");
            usernameField.setText("");
            complaintDetailsArea.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting the complaint.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resolveComplaint() {
        try {
            String id = idField.getText();

            // Fetch the complaint details from ComplaintsIssued
            String fetchSql = "SELECT * FROM ComplaintsIssued WHERE id = ?";
            PreparedStatement fetchStatement = connection.prepareStatement(fetchSql);
            fetchStatement.setInt(1, Integer.parseInt(id));
            ResultSet resultSet = fetchStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String complaintDetails = resultSet.getString("complaint_details");
                Date dateOfComplaint = resultSet.getDate("date_of_complaint");

                // Insert the resolved complaint into ComplaintsSolved
                String insertSql = "INSERT INTO ComplaintsSolved (id, user_id, username, complaint_details, date_of_complaint) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setInt(1, Integer.parseInt(id));
                insertStatement.setInt(2, userId);
                insertStatement.setString(3, username);
                insertStatement.setString(4, complaintDetails);
                insertStatement.setDate(5, dateOfComplaint);
                insertStatement.executeUpdate();

                // Delete the complaint from ComplaintsIssued
                String deleteSql = "DELETE FROM ComplaintsIssued WHERE id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                deleteStatement.setInt(1, Integer.parseInt(id));
                deleteStatement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Complaint resolved successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "No complaint found with the given ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            // Clear the fields after resolution
            idField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resolving the complaint.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ComplaintManagementSystem().setVisible(true);
            }
        });
    }
}
