import javax.swing.*; 
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.util.List;

public class Main extends JFrame {
    private Student studentLogic = new Student();
    private Program programLogic = new Program();
    private College collegeLogic = new College();

    public Main() {
        setTitle("MSU-IIT Student Information System");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIManager.put("Button.select", new Color(180, 150, 240)); // Light Violet for button click 
        UIManager.put("TabbedPane.selected", new Color(230, 230, 250)); // Light Lavender for selected tab
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT); // Moves tabs to the left sidebar
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(new Color(111, 66, 193));   // Purple for tab text
        tabs.setOpaque(true); 

        tabs.addTab("Students", createTabPanel(studentLogic, 
        new String[]{"ID", "First Name", "Last Name", "Program", "College", "Year", "Gender"}, "Student"));
        tabs.addTab("Programs", createTabPanel(programLogic, new String[]{"Code", "Name", "College"}, "Program"));
        tabs.addTab("Colleges", createTabPanel(collegeLogic, new String[]{"Code", "Name"}, "College"));

        add(tabs);
    }
   private JPanel createTabPanel(BaseEntity logic, String[] columns, String entityName) {
    JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
    mainPanel.setBackground(new Color(245, 245, 250)); // Light grey background
    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

    // --- HEADER SECTION ---
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    
    JLabel title = new JLabel(entityName + "s"); // "Students", "Programs", "Colleges"
    title.setFont(new Font("SansSerif", Font.BOLD, 28));
    title.setForeground(new Color(50, 50, 70));
    
    JButton addBtn = new JButton("+ Add " + entityName);
    addBtn.setPreferredSize(new Dimension(150, 40));
    addBtn.setBackground(new Color(111, 66, 193)); // Purple theme
    addBtn.setForeground(Color.WHITE);
    addBtn.setFocusPainted(false);
    addBtn.setBorderPainted(false);

    header.add(title, BorderLayout.WEST);
    header.add(addBtn, BorderLayout.EAST);

    //table and search container
    JPanel card = new JPanel(new BorderLayout(0, 15));  
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Search & Filter (Sort is automatic on headers)
    JTextField searchField = new JTextField();
    searchField.setPreferredSize(new Dimension(0, 40));
    searchField.setBorder(BorderFactory.createTitledBorder("Search by " + columns[0] + " or name..."));
    searchField.setBackground(new Color(245, 245, 250));
    
    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    }; 
    JTable table = new JTable(model);
    table.setRowHeight(30);
    table.getTableHeader().setPreferredSize(new Dimension(0, 33));
    table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); 
    table.setShowVerticalLines(false);
    table.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
    table.getTableHeader().setBackground(new Color(230, 230, 250)); // Light Lavender
    
    card.add(searchField, BorderLayout.NORTH);
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(Color.WHITE); // Matches your table background
    scroll.setBorder(BorderFactory.createEmptyBorder());
    card.add(scroll, BorderLayout.CENTER);

    JPopupMenu rightClickMenu = new JPopupMenu(); 
    JMenuItem editItem = new JMenuItem("Edit " + entityName);
    JMenuItem deleteItem = new JMenuItem("Delete " + entityName);
    rightClickMenu.add(editItem);
    rightClickMenu.add(deleteItem);
    table.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row != -1) {
                table.setRowSelectionInterval(row, row);
                rightClickMenu.show(table, e.getX(), e.getY()); // Instant show para dili sha murag mu fail sa first click
            }
        }
    }
});

    mainPanel.add(header, BorderLayout.NORTH);
    mainPanel.add(card, BorderLayout.CENTER);

    // Logic for Search
    searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { // 
        public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

        private void filter() { 
            String text = searchField.getText();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    });
    // to Edit Form
    editItem.addActionListener(e -> showForm(logic, entityName, columns, table, true));
    addBtn.addActionListener(e -> showForm(logic, entityName, columns, table, false));
    
    deleteItem.addActionListener(e -> {
    int row = table.getSelectedRow();
    if (row != -1) {
        int modelRow = table.convertRowIndexToModel(row);
        String key = (String) table.getModel().getValueAt(modelRow, 0); // e.g., the Code/ID

        int count = 0;
        String warningMsg = "";
        if (entityName.equals("Program")) {
            // Check Student CSV (index 3 Program)
            count = studentLogic.countAffectedEntries(key, 3); 
            warningMsg = "students";
        } else if (entityName.equals("College")) {
            // Check Program CSV (index 2 College)
            count = programLogic.countAffectedEntries(key, 2); 
            warningMsg = "programs";
        }   

        // Show the confirmation with the count and warning message
        String message = "Are you sure you want to delete " + key + "?";
        if (count > 0) {
            message += "\n\nWarning: There are " + count + " " + warningMsg + 
                       " linked to this. They will be set to NULL.";
        }

        int confirm = JOptionPane.showConfirmDialog(null, message, "Confirm Delete", 
                      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
        // Set linked records to NULL before deleting the parent
        if (entityName.equals("Program")) {
            // If Program, (index 3)
            studentLogic.nullifyAffectedEntries(key, 3);
        } else if (entityName.equals("College")) {
            // IfCollege, (index 2) 
            programLogic.nullifyAffectedEntries(key, 2);
        }
        //  actual deletion of the Program or College
        String msg = logic.delete(key);
        JOptionPane.showMessageDialog(null, msg);
        refreshAllTabs();
        }
    }
});

    loadTableData(model, logic);
    return mainPanel;
}

private void showForm(BaseEntity logic, String entityName, String[] cols, JTable table, boolean isEdit) {
    JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
    JComponent[] inputs = new JComponent[cols.length];

    for (int i = 0; i < cols.length; i++) {
    if (entityName.equals("Student") && cols[i].equalsIgnoreCase("College")) {
        inputs[i] = new JLabel(" (Auto-generated)");
        continue; 
    }

    //container for label and error message like this: "ID [red comment here]" or "First Name [red comment here]"
    JPanel fieldHeader = new JPanel(new BorderLayout());
    fieldHeader.setOpaque(false);
    
    JLabel label = new JLabel(cols[i]); // "ID", "First Name", etc.
    JLabel errorLabel = new JLabel(""); 
    errorLabel.setForeground(Color.RED);
    errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
    
    fieldHeader.add(label, BorderLayout.WEST);
    fieldHeader.add(errorLabel, BorderLayout.EAST);
    form.add(fieldHeader);

    // para ni sa dropdown
    if (entityName.equals("Student") && cols[i].equalsIgnoreCase("Gender")) {
        inputs[i] = new JComboBox<>(new String[]{ "","Male", "Female", "Other"});
    } else {
         // show error messages for each field
        JTextField txt = new JTextField();
        inputs[i] = txt;
        int currentIdx = i;
        txt.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextField) input).getText().trim();
                boolean isValid = true;
                String errorMsg = "";

                if (cols[currentIdx].equals("ID")) {
                    isValid = text.matches("\\d{4}-\\d{4}"); 
                    errorMsg = "Use XXXX-NNNN";
                } else if (cols[currentIdx].contains("Name")) {
                        isValid = text.matches("[a-zA-Z ]+");
                        errorMsg = "Letters only";
                } else if (cols[currentIdx].equals("Year")) {
                        isValid = text.matches("\\d+");
                        errorMsg = "Numbers only";
                } if (!isValid && !text.isEmpty()) {
                        errorLabel.setText(errorMsg);
                        txt.setBorder(BorderFactory.createLineBorder(Color.RED)); // Turn border red
                } else {
                        errorLabel.setText("");
                        txt.setBorder(UIManager.getLookAndFeelDefaults().getBorder("TextField.border"));
                }
                return true; // Return true so they can still move focus, but the red stays
                    
            }
            });
        }
    form.add(inputs[i]); 
}   for (int i = 0; i < inputs.length; i++) { // Para mu adtog next field inig press sa Enter
    final int currentIdx = i;
        if (inputs[i] instanceof JTextField) {
        ((JTextField) inputs[i]).addActionListener(e -> {
            for (int j = currentIdx + 1; j < inputs.length; j++) {
                if (inputs[j] instanceof JTextField || inputs[j] instanceof JComboBox) {
                inputs[j].requestFocusInWindow();
                return;
                }
            }
        });
    }
}
    if (isEdit) { 
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) { // incase sorted, convert to model index para sakto ang data nga makuha
        int modelRow = table.convertRowIndexToModel(selectedRow);
        for (int i = 0; i < cols.length; i++) {
        // Get value from table model para ma pre-fill ang form with existing data
            Object value = table.getModel().getValueAt(modelRow, i);
            String valStr = (value != null) ? value.toString() : "";

            if (inputs[i] instanceof JTextField) {
                ((JTextField) inputs[i]).setText(valStr);
            } else if (inputs[i] instanceof JComboBox) {
                ((JComboBox<?>) inputs[i]).setSelectedItem(valStr);
            }
        }
        }
    }
    while (true) { 
    int result = JOptionPane.showConfirmDialog(this, form, (isEdit ? "Edit " : "Add ") + entityName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
        // EXTRACT VALUES GIKAN SA INPUTS 
        String[] values = new String[cols.length];
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] instanceof JTextField) {  
                values[i] = ((JTextField) inputs[i]).getText().trim().replace(",", ""); 
            } else if (inputs[i] instanceof JComboBox) {
                values[i] = ((JComboBox<?>) inputs[i]).getSelectedItem().toString();
            } else if (inputs[i] instanceof JLabel) {
                values[i] = ""; 
            }
        }
        String msg = "";
        
        if (isEdit) {    
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        String oldID = table.getModel().getValueAt(modelRow, 0).toString();

        if (entityName.equals("Student")) {
            // Correct Order: ID[0], First[1], Last[2], Program[3], Year[5], Gender[6]
            msg = studentLogic.update(oldID, values[0], values[1], values[2], values[3], values[5], values[6]);
        } else if (entityName.equals("Program")) {
            msg = programLogic.update(oldID, values[0], values[1], values[2]);
        } else if (entityName.equals("College")) {
            msg = collegeLogic.update(oldID, values[0], values[1]);
        }
    }  else { 
        if (entityName.equals("Student")) {
            // Skips value[4] (College) para inig isave ang program code ra ang ma-save, then sa display lang nato i-translate to college name
            msg = studentLogic.add(values[0], values[1], values[2], values[3], values[5], values[6]);
        } else if (entityName.equals("Program")) {
            msg = programLogic.add(values[0], values[1], values[2]);
        } else if (entityName.equals("College")) {
            msg = collegeLogic.add(values[0], values[1]);
        }
    }

        if (!msg.isEmpty()) { 
        JOptionPane.showMessageDialog(this, msg);
        
        if (msg.contains("successfully") || msg.contains("SUCCESS")) { 
            refreshAllTabs();
            break; 
            }  
        }
    }else {
        break;}
    }
}

private void loadTableData(DefaultTableModel model, BaseEntity logic) {
    model.setRowCount(0);
    List<String[]> data = logic.fetchData();
    for (String[] row : data) {
        if (logic instanceof Student) {
            String pCode = row[3];
            String college = ((Student) logic).getCollegeForProgram(pCode);
            
            // row[1] First Name, row[2] Last Name
            model.addRow(new String[]{row[0], row[1], row[2], pCode, college, row[4], row[5]});
        } else {
            model.addRow(row);
        }
    }
}

    // Call this after any Add/Delete to update the whole UI
    private void refreshAllTabs() {
    JTabbedPane tabs = (JTabbedPane) getContentPane().getComponent(0);
    int activeTab = tabs.getSelectedIndex(); // Remember where the user is
    
    // para mu loop sa tanan tabs and refresh data
    for (int i = 0; i < tabs.getTabCount(); i++) {
        JPanel tabPanel = (JPanel) tabs.getComponentAt(i);
        // 
        JPanel card = (JPanel) tabPanel.getComponent(1); 
        JScrollPane scroll = (JScrollPane) card.getComponent(1);
        JTable table = (JTable) scroll.getViewport().getView();
        
        // Reload data based on tab
        if (i == 0) loadTableData((DefaultTableModel) table.getModel(), studentLogic);
        else if (i == 1) loadTableData((DefaultTableModel) table.getModel(), programLogic);
        else if (i == 2) loadTableData((DefaultTableModel) table.getModel(), collegeLogic);
    }
    
    tabs.setSelectedIndex(activeTab); // mubalik sa active tab after refresh
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}