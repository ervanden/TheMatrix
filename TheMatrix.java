
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class TheMatrix extends JPanel implements ActionListener {

    JTable table;
    MatrixTableModel tm;
    JTextField memberField;
    JTextField itemField;

    ArrayList<String> members = new ArrayList<>();
    ArrayList<String> items = new ArrayList<>();
    ArrayList<Float> totals = new ArrayList<>();
    ParticipatedMap participated = new ParticipatedMap();
    PaidMap paid = new PaidMap();

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println("action " + action);
        executeAction(action);
    }

    void executeAction(String action) {
        if (action.equals("Add Member")) {
            if (memberField.getText().length() == 0) {
                memberField.setText("enter the name of the person here,then click the button");
            } else {
                members.add(memberField.getText());
                memberField.setText("");
                totals.add(0f);
                evaluateTotals();
                tm.fireTableStructureChanged();
                table.setModel(tm);
            }
        }
        if (action.equals("Add Item")) {
            if (itemField.getText().length() == 0) {
                itemField.setText("enter the name of the item here,then click the button");
            } else {
                items.add(items.size() - 1, itemField.getText());
                itemField.setText("");
                evaluateTotals();
                tm.fireTableDataChanged();
                table.setModel(tm);
            }
        }
        if (action.equals("Save")) {
            JFileChooser fileChooser = new JFileChooser();
            int retval = fileChooser.showOpenDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File fileOut = fileChooser.getSelectedFile();
                try {
                    OutputStream is = new FileOutputStream(fileOut);
                    OutputStreamWriter isr = new OutputStreamWriter(is, "UTF-8");
                    BufferedWriter out = new BufferedWriter(isr);
                    for (String member : members) {
                        out.write("member|" + member + "|");
                        out.newLine();
                    };
                    for (int row = 0; row <= items.size() - 2; row++) {
                        out.write("item|" + items.get(row) + "|");
                        out.newLine();
                    };
                    for (String member : members) {
                        for (int row = 0; row <= items.size() - 2; row++) {
                            String item = items.get(row);
                            out.write("entry|" + member + "|" + item + "|" + paid.get(member, item) + "|" + participated.get(member, item));
                            out.newLine();
                        }
                    };
                    out.close();
                    System.out.println("Table is saved");
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        }
        if (action.equals("Load")) {
            JFileChooser fileChooser = new JFileChooser();
            int retval = fileChooser.showOpenDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File fileIn = fileChooser.getSelectedFile();
                try {
                    InputStream is = new FileInputStream(fileIn);
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader in = new BufferedReader(isr);
                    items.clear();
                    members.clear();
                    totals.clear();
                    paid.clear();
                    participated.clear();
                    items.add("TOTAL");
                    String l;
                    String[] ls;
                    while ((l = in.readLine()) != null) {
                        ls = l.split("\\|");
                        System.out.println(l);
                        System.out.println(ls[0]);
                        if (ls[0].equals("member")) {
                            members.add(ls[1]);
                            totals.add(0f);
                        } else if (ls[0].equals("item")) {
                            items.add(items.size() - 1, ls[1]);
                        } else if (ls[0].equals("entry")) {
                            paid.add(ls[1], ls[2], Float.valueOf(ls[3]));
                            if (ls[4].equals("true")) {
                                participated.add(ls[1], ls[2], true);
                            }
                        }
                    };
                    in.close();
                    evaluateTotals();
                    tm.fireTableStructureChanged();
                    table.setModel(tm);
                    System.out.println("Table is Loaded");
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        }
    }

    public TheMatrix() {
        super();

        BoxLayout box = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        this.setLayout(box);

        tm = new MatrixTableModel();
        items.add("TOTAL");

        table = new JTable(new MatrixTableModel());
        table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);

        JScrollPane scrollPane = new JScrollPane(table);

        memberField = new JTextField();
        itemField = new JTextField();

        JButton memberButton = new JButton("Add Member");
        memberButton.addActionListener(this);
        memberButton.setActionCommand("Add Member");
        memberField.addActionListener(this); // execute when <Enter> is pressed
        memberField.setActionCommand("Add Member");

        JButton itemButton = new JButton("Add Item");
        itemButton.addActionListener(this);
        itemButton.setActionCommand("Add Item");
        itemField.addActionListener(this); // execute when <Enter> is pressed
        itemField.setActionCommand("Add Item");

        memberField.setMaximumSize(new Dimension(1000, 25)); // prevent vertical stretching 
        itemField.setMaximumSize(new Dimension(1000, 25));

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand("Save");
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        loadButton.setActionCommand("Load");

        JPanel memberPane = new JPanel();
        memberPane.setLayout(new BoxLayout(memberPane, BoxLayout.LINE_AXIS));
        memberPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//        buttonPane.add(Box.createHorizontalGlue());
        memberPane.add(memberButton);
        memberPane.add(Box.createRigidArea(new Dimension(10, 0)));
        memberPane.add(memberField);

        JPanel itemPane = new JPanel();
        itemPane.setLayout(new BoxLayout(itemPane, BoxLayout.LINE_AXIS));
        itemPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        itemPane.add(itemButton);
        itemPane.add(Box.createRigidArea(new Dimension(10, 0)));
        itemPane.add(itemField);
//        itemPane.add(Box.createHorizontalGlue());

        JPanel ioPane = new JPanel();
        ioPane.setLayout(new BoxLayout(ioPane, BoxLayout.LINE_AXIS));
        ioPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//        buttonPane.add(Box.createHorizontalGlue());
        ioPane.add(saveButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(loadButton);

        add(scrollPane);
        add(itemPane);
        add(memberPane);
        add(ioPane);
    }

    boolean odd(int n) {
        return (n % 2 == 0);
    }

    boolean even(int n) {
        return (n % 2 == 1);
    }

    int colToMember(int col) {
        if (col < 1) {
            return 0;
        }
        return (col - 1) / 2;
    }

    int memberToCol(int member) {
        return member * 2 + 1;
    }

    class ParticipatedMap {

        HashMap<String, Boolean> m = new HashMap<>();

        public ParticipatedMap() {
            m.clear();
        }

        public void clear() {
            m.clear();
        }

        public void add(String member, String item, Boolean value) {
            m.put(member + "|" + item, value);
        }

        public boolean get(String member, String item) {
            if (m.containsKey(member + "|" + item)) {
                return m.get(member + "|" + item);
            } else {
                return false;
            }
        }
    }

    class PaidMap {

        HashMap<String, Float> m = new HashMap<>();

        public PaidMap() {
            m.clear();
        }

        public void clear() {
            m.clear();
        }

        public void add(String member, String item, Float value) {
            m.put(member + "|" + item, value);
        }

        public Float get(String member, String item) {
            if (m.containsKey(member + "|" + item)) {
                return m.get(member + "|" + item);
            } else {
                return 0f;
            }
        }
    }

    void evaluateTotals() {
        String item;
        String member;
        int itemConsumers;
        float itemCost;

        // reset totals
        for (int imem = 0; imem <= members.size() - 1; imem++) {
            totals.add(imem, 0f);
        }

        for (int row = 0; row <= items.size() - 2; row++) {
            item = items.get(row);
            itemConsumers = 0;
            itemCost = 0f;

            // count itemConsumers for this item
            for (int imem = 0; imem <= members.size() - 1; imem++) {
                member = members.get(imem);
                if (participated.get(member, item)) {
                    itemConsumers++;
                }

            }

            if (itemConsumers > 0) {  // if no consumers yet , do not process this item
                // find the total amount paid for this item 
                for (int imem = 0; imem <= members.size() - 1; imem++) {
                    member = members.get(imem);
                    totals.set(imem, totals.get(imem) + paid.get(member, item));
                    itemCost = itemCost + paid.get(member, item);
                }

                for (int imem = 0; imem <= members.size() - 1; imem++) {
                    member = members.get(imem);
                    if (participated.get(member, item)) {
                        totals.set(imem, totals.get(imem) - (itemCost / itemConsumers));
                    }

                }
            }
        }
    }

    class MatrixTableModel extends DefaultTableModel {

        public int getColumnCount() {
            return 2 * members.size() + 1;
        }

        public int getRowCount() {
            return items.size();
        }

        public String getColumnName(int col) {
            String cn;
            if (col == 0) {
                cn = "item";
                return ("item");
            } else {
                cn = members.get(colToMember(col));
            };
            return cn;
        }

        public Object getValueAt(int row, int col) {
            Object value;
            if (col == 0) {  // item names
                value = items.get(row);
            } else {
                if (row == items.size() - 1) {  // last row = totals
                    if (even(col)) {
                        value = totals.get(colToMember(col));
                    } else {
                        value = false;
                    }
                } else {
                    if (even(col)) {  // amount paid
                        value = paid.get(members.get(colToMember(col)), items.get(row));
                    } else {  // participated or not
                        value = participated.get(members.get(colToMember(col)), items.get(row));
                    }
                }
            }
            return value;
        }

        public Class getColumnClass(int c) {
            Float i = 1f;
            Boolean b = true;
            String s = "";
            if (c == 0) {
                return s.getClass();
            } else {
                if (even(c)) {
                    return i.getClass();
                } else {
                    return b.getClass();
                }
            }
        }

        public boolean isCellEditable(int row, int col) {
            return (col > 0) && (row < items.size()-1);
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                items.set(row, (String) value);
            } else {
                if (even(col)) {
                    paid.add(members.get(colToMember(col)), items.get(row), (Float) value);
                } else {
                    participated.add(members.get(colToMember(col)), items.get(row), (Boolean) value);
                }
            }
            evaluateTotals();
            fireTableDataChanged();
        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("The Matrix");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
        TheMatrix newContentPane = new TheMatrix();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
