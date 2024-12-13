import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class HashCatApp {

    private JFrame frame;
    private JTextField hashInputField;
    private JTextArea outputArea;
    private JComboBox<String> algorithmComboBox;
    private JButton startButton;
    private JFileChooser fileChooser;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HashCatApp::new);
    }

    public HashCatApp() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("HashCat - Password Cracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Layout
        frame.setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));

        JLabel hashInputLabel = new JLabel("Enter Hash:");
        hashInputField = new JTextField();

        JLabel algorithmLabel = new JLabel("Select Algorithm:");
        algorithmComboBox = new JComboBox<>(new String[]{"MD5", "SHA-1", "SHA-256"});

        JLabel dictionaryLabel = new JLabel("Select Dictionary File:");
        JButton chooseFileButton = new JButton("Choose File");
        fileChooser = new JFileChooser();

        chooseFileButton.addActionListener(e -> fileChooser.showOpenDialog(frame));

        inputPanel.add(hashInputLabel);
        inputPanel.add(hashInputField);
        inputPanel.add(algorithmLabel);
        inputPanel.add(algorithmComboBox);
        inputPanel.add(dictionaryLabel);
        inputPanel.add(chooseFileButton);

        // Output Area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Start Button
        startButton = new JButton("Start Cracking");
        startButton.addActionListener(new StartButtonListener());

        // Add components to frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(startButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String hashToCrack = hashInputField.getText().trim();
            String algorithm = (String) algorithmComboBox.getSelectedItem();

            if (hashToCrack.isEmpty() || fileChooser.getSelectedFile() == null) {
                JOptionPane.showMessageDialog(frame, "Please enter a hash and select a dictionary file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            outputArea.setText("Starting to crack the hash...\n");

            List<String> dictionary = loadDictionary(fileChooser.getSelectedFile().getAbsolutePath());
            String result = crackHash(hashToCrack, algorithm, dictionary);

            if (result != null) {
                outputArea.append("Password found: " + result + "\n");
            } else {
                outputArea.append("Password not found in the dictionary.\n");
            }
        }

        private List<String> loadDictionary(String filePath) {
            List<String> words = new ArrayList<>();
            try (java.util.Scanner scanner = new java.util.Scanner(new java.io.File(filePath))) {
                while (scanner.hasNextLine()) {
                    words.add(scanner.nextLine());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error reading dictionary file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return words;
        }

        private String crackHash(String hashToCrack, String algorithm, List<String> dictionary) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
                for (String word : dictionary) {
                    byte[] hashedBytes = messageDigest.digest(word.getBytes());
                    String computedHash = bytesToHex(hashedBytes);
                    if (computedHash.equalsIgnoreCase(hashToCrack)) {
                        return word;
                    }
                }
            } catch (NoSuchAlgorithmException ex) {
                outputArea.append("Error: Unsupported algorithm selected.\n");
            }
            return null;
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}