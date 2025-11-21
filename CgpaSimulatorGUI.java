import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;


public class CgpaSimulatorGUI extends JFrame {

    // UI components
    private final JTextField lastCgpaField = new JTextField("0.0", 8);
    private final JTextField lastUnitsField = new JTextField("0", 8);
    private final DefaultTableModel tableModel;
    private final JTable courseTable;
    private final JLabel termUnitsLabel = new JLabel("0");
    private final JLabel termGradePointsLabel = new JLabel("0.00");
    private final JLabel termGpaLabel = new JLabel("0.00");
    private final JLabel grandUnitsLabel = new JLabel("0");
    private final JLabel newCgpaLabel = new JLabel("0.00");
    private final DecimalFormat df = new DecimalFormat("#.00");

    public CgpaSimulatorGUI() {
        super("CGPA Simulator — Java Swing");

        // Setup table model and table
        String[] cols = {"Course Code", "Units", "Grade (1-5)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // all cells editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Keep as Object to accept strings/numbers; we'll validate later
                return Object.class;
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setFillsViewportHeight(true);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        buildUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 560);
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Input panel for last CGPA & units
        JPanel inputsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputsPanel.setBorder(BorderFactory.createTitledBorder("Previous Semester"));
        inputsPanel.add(new JLabel("Last CGPA (0 if first semester): "));
        inputsPanel.add(lastCgpaField);
        inputsPanel.add(Box.createHorizontalStrut(12));
        inputsPanel.add(new JLabel("Last Total Units (0 if first semester): "));
        inputsPanel.add(lastUnitsField);
        inputsPanel.add(Box.createHorizontalStrut(12 ) );
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        topPanel.add(inputsPanel, gbc);

        // Table area with Add / Remove buttons
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("This Semester — Courses"));

        JPanel tableButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowBtn = new JButton("Add Row");
        JButton removeRowBtn = new JButton("Remove Selected Row");
        tableButtonPanel.add(addRowBtn);
        tableButtonPanel.add(removeRowBtn);

        // quick tip label
        JLabel tip = new JLabel("Tip: Enter Course Code (optional), Units as integer, Grade as number 1 to 5");
        tip.setFont(tip.getFont().deriveFont(Font.ITALIC, 11f));
        tableButtonPanel.add(tip);

        tablePanel.add(tableButtonPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(tablePanel, gbc);

        // Right panel containing results and actions
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Results & Actions"));

        JPanel resultsGrid = new JPanel(new GridLayout(6, 2, 6, 6));
        resultsGrid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        resultsGrid.add(new JLabel("This Semester Units:"));
        resultsGrid.add(termUnitsLabel);
        resultsGrid.add(new JLabel("This Semester Grade Points:"));
        resultsGrid.add(termGradePointsLabel);
        resultsGrid.add(new JLabel("This Semester GPA (Term GPA):"));
        resultsGrid.add(termGpaLabel);
        resultsGrid.add(new JLabel("Grand Total Units:"));
        resultsGrid.add(grandUnitsLabel);
        resultsGrid.add(new JLabel("New Cumulative CGPA:"));
        resultsGrid.add(newCgpaLabel);

        rightPanel.add(resultsGrid);

        // Action buttons
        JPanel actions = new JPanel(new GridLayout(1, 4, 8, 8));
        JButton calculateBtn = new JButton("Calculate");
        JButton saveBtn = new JButton("Save Results");
        JButton resetBtn = new JButton("Reset");
        JButton exitBtn = new JButton("Exit");

        actions.add(calculateBtn);
        actions.add(saveBtn);
        actions.add(resetBtn);
        actions.add(exitBtn);

        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(actions);

        // Put topPanel and rightPanel into main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);

        // Wire the add/remove/calc/save/reset/exit actions
        addRowBtn.addActionListener(evt -> addEmptyRow());
        removeRowBtn.addActionListener(evt -> removeSelectedRow());
        calculateBtn.addActionListener(evt -> performCalculation());
        saveBtn.addActionListener(evt -> saveResultsToFile());
        resetBtn.addActionListener(evt -> resetAll());
        exitBtn.addActionListener(evt -> dispose());

        // Enter key in lastCgpaField will focus lastUnitsField (inlined listener)
        lastCgpaField.addActionListener(e -> lastUnitsField.requestFocusInWindow());

        // Pre-add 3 empty rows for convenience
        for (int i = 0; i < 3; i++) addEmptyRow();
    }

    private void addEmptyRow() {
        tableModel.addRow(new Object[]{"", "", ""});
    }

    private void removeSelectedRow() {
        int selected = courseTable.getSelectedRow();
        if (selected >= 0) {
            tableModel.removeRow(selected);
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performCalculation() {
        // Validate lastCgpa and lastUnits
        double lastCgpa;
        int lastUnits;
        try {
            lastCgpa = Double.parseDouble(lastCgpaField.getText().trim());
            if (lastCgpa < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid non-negative number for Last CGPA (e.g., 0 or 3.45).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            lastUnits = Integer.parseInt(lastUnitsField.getText().trim());
            if (lastUnits < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid non-negative integer for Last Units (e.g., 0 or 18).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Process table rows
        int rows = tableModel.getRowCount();
        int termUnitsSum = 0;
        double termGradePointsSum = 0.0;

        for (int r = 0; r < rows; r++) {
            Object unitObj = tableModel.getValueAt(r, 1);
            Object gradeObj = tableModel.getValueAt(r, 2);

            // Skip completely empty rows (both unit and grade empty)
            boolean unitEmpty = unitObj == null || unitObj.toString().trim().isEmpty();
            boolean gradeEmpty = gradeObj == null || gradeObj.toString().trim().isEmpty();
            if (unitEmpty && gradeEmpty) continue;

            // Validate unit
            int units;
            try {
                units = Integer.parseInt(unitObj.toString().trim());
                if (units <= 0) {
                    throw new NumberFormatException("units must be > 0");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid units at row " + (r + 1) + ". Enter a positive integer (e.g., 3).", "Invalid Units", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate grade
            double grade;
            try {
                grade = Double.parseDouble(gradeObj.toString().trim());
                if (grade < 1.0 || grade > 5.0) {
                    throw new NumberFormatException("grade out of range");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid grade at row " + (r + 1) + ". Enter a number between 1 and 5 (e.g., 4 or 3.5).", "Invalid Grade", JOptionPane.ERROR_MESSAGE);
                return;
            }

            termUnitsSum += units;
            termGradePointsSum += (grade * units);
        } // end for rows

        // If there are no units at all and lastUnits=0, cannot compute CGPA
        int grandUnits = lastUnits + termUnitsSum;
        if (grandUnits == 0) {
            JOptionPane.showMessageDialog(this, "Total units are zero. Enter valid course units or last semester units.", "No Units", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Compute term GPA and new CGPA
        double termGpa = (termUnitsSum == 0) ? 0.0 : termGradePointsSum / termUnitsSum;
        double lastGradePoints = lastCgpa * lastUnits;
        double grandGradePoints = lastGradePoints + termGradePointsSum;
        double newCgpa = grandGradePoints / grandUnits;

        // Update results UI
        termUnitsLabel.setText(String.valueOf(termUnitsSum));
        termGradePointsLabel.setText(df.format(termGradePointsSum));
        termGpaLabel.setText(df.format(termGpa));
        grandUnitsLabel.setText(String.valueOf(grandUnits));
        newCgpaLabel.setText(df.format(newCgpa));

        // Also show a small summary dialog
        String msg = String.format("Calculation complete!\n\nTerm Units: %d\nTerm Grade Points: %s\nTerm GPA: %s\n\nGrand Units: %d\nNew CGPA: %s",
                termUnitsSum, df.format(termGradePointsSum), df.format(termGpa), grandUnits, df.format(newCgpa));
        JOptionPane.showMessageDialog(this, msg, "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveResultsToFile() {
        // Validate that calculation is up-to-date by re-running calculation (this will show errors if invalid)
        performCalculation();

        // Prepare file save dialog
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CGPA Result");
        chooser.setFileFilter(new FileNameExtensionFilter("Text file", "txt"));
        int userChoice = chooser.showSaveDialog(this);
        if (userChoice != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        // ensure .txt extension
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }

        // Build summary string
        StringBuilder sb = new StringBuilder();
        sb.append("CGPA Simulator Result\n");
        sb.append("=====================\n\n");
        sb.append("Last Semester CGPA: ").append(lastCgpaField.getText().trim()).append("\n");
        sb.append("Last Semester Units: ").append(lastUnitsField.getText().trim()).append("\n\n");
        sb.append("This Semester Courses:\n");
        sb.append(String.format("%-12s %-8s %-8s\n", "CourseCode", "Units", "Grade"));
        sb.append("--------------------------------\n");

        for (int r = 0; r < tableModel.getRowCount(); r++) {
            Object code = tableModel.getValueAt(r, 0);
            Object unit = tableModel.getValueAt(r, 1);
            Object grade = tableModel.getValueAt(r, 2);

            String codeStr = (code == null) ? "" : code.toString();
            String unitStr = (unit == null) ? "" : unit.toString();
            String gradeStr = (grade == null) ? "" : grade.toString();

            // skip fully empty row
            if (codeStr.trim().isEmpty() && unitStr.trim().isEmpty() && gradeStr.trim().isEmpty()) continue;

            sb.append(String.format("%-12s %-8s %-8s\n", truncate(codeStr, 12), truncate(unitStr, 8), truncate(gradeStr, 8)));
        }

        sb.append("\nSummary:\n");
        sb.append("This Semester Units: ").append(termUnitsLabel.getText()).append("\n");
        sb.append("This Semester Grade Points: ").append(termGradePointsLabel.getText()).append("\n");
        sb.append("This Semester GPA: ").append(termGpaLabel.getText()).append("\n");
        sb.append("Grand Total Units: ").append(grandUnitsLabel.getText()).append("\n");
        sb.append("New CGPA: ").append(newCgpaLabel.getText()).append("\n");

        // Write to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(sb.toString());
            bw.flush();
            JOptionPane.showMessageDialog(this, "Results saved to:\n" + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    private void resetAll() {
        int confirm = JOptionPane.showConfirmDialog(this, "Reset all fields and course table?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        lastCgpaField.setText("0.0");
        lastUnitsField.setText("0");
        // clear table
        tableModel.setRowCount(0);
        // add some empty rows
        for (int i = 0; i < 3; i++) addEmptyRow();

        // reset labels
        termUnitsLabel.setText("0");
        termGradePointsLabel.setText("0.00");
        termGpaLabel.setText("0.00");
        grandUnitsLabel.setText("0");
        newCgpaLabel.setText("0.00");
    }

    public static void main(String[] args) {
        // Use system look and feel for a native look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(CgpaSimulatorGUI::new);
    }
}
