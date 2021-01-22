package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.UUID;

public class HotelGui {
    private JPanel mainPanel;
    private JTable roomList;

    public HotelGui() {
        JPanel root = mainPanel;
        JFrame frame = new JFrame();
        createTable();
        frame.setTitle("Hotel Gui, List of all rooms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void createTable() {
        roomList.setDefaultEditor(Object.class, null);
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new Object[]{"Number", "Port", "Size", "Reserved", "Occupied"});
        roomList.setModel(tableModel);
    }

    public void addToTable(int number, int port, int size, String reserved, boolean occupied, UUID key) {
        var myModel = (DefaultTableModel) roomList.getModel();
        if (reserved == null)
            myModel.addRow(new Object[]{number, port, size, "No", occupied, key});
        else
            myModel.addRow(new Object[]{number, port, size, reserved, occupied, key});
    }

    public void removeFromTable(int id) {
        var myModel = (DefaultTableModel) roomList.getModel();
        for (int i = 0; i < myModel.getRowCount(); i++) {
            var myId = myModel.getValueAt(i, 0).toString();
            if (Integer.parseInt(myId) == id) {
                myModel.removeRow(i);
                break;
            }
        }
    }

    public void editTable(String reserved, boolean occupied, int id) {
        var myModel = roomList.getModel();
        for (int i = 0; i < myModel.getRowCount(); i++) {
            var myId = myModel.getValueAt(i, 0).toString();
            if (Integer.parseInt(myId) == id) {
                myModel.setValueAt(reserved, i, 3);
                myModel.setValueAt(occupied, i, 4);
                break;
            }
        }
    }
}
