package gui;

import connector.ConnectorClient;
import models.Hotel;
import models.Room;
import models.ServerRequestType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservedGui {
    private JPanel mainPanel;
    private JButton endReservationButton;
    private JButton enterExitButton;
    private JTable roomTable;
    private final JFrame frame;

    private final List<Room> reservedRooms = new ArrayList<>();

    ReservedGui() {
        JPanel root = mainPanel;
        frame = new JFrame();
        frame.setTitle("My reserved room(s)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setContentPane(root);
        createTable();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        enterExitButton.addActionListener(e -> changeStatus());
        endReservationButton.addActionListener(e -> endReservation());
    }

    public void createTable() {
        roomTable.setDefaultEditor(Object.class, null);
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new Object[]{"Number", "Size", "Occupied"});
        roomTable.setModel(tableModel);
    }

    public void addToTable(int number, int port, int size, boolean occupied, UUID key) {
        reservedRooms.add(new Room(number, port, size, key));
        var myModel = (DefaultTableModel) roomTable.getModel();
        myModel.addRow(new Object[]{number, size, occupied});
    }

    private void changeStatus() {
        var myModel = roomTable.getModel();
        var myRow = roomTable.getSelectedRow();
        var occupied = reservedRooms.get(myRow).isRoomOccupied();
        reservedRooms.get(myRow).setRoomOccupied(!occupied);
        myModel.setValueAt(!occupied, myRow, 2);
        try {
            ConnectorClient roomConnect = new ConnectorClient(reservedRooms.get(myRow).getPort());
            roomConnect.communicateServer("CHANGE_STATUS");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void endReservation() {

        try {
            ConnectorClient roomConnect = new ConnectorClient(Hotel.getServerPort());
            StringBuilder myRequest = new StringBuilder();
            myRequest.append(ServerRequestType.ROOM_RESERVE_END).append(";");
//            myRequest.append(reservedRooms.get(0).getReservation()).append(";");
            for (Room myReservedRoom : reservedRooms
            ) {
                myRequest.append(myReservedRoom.getNumber()).append("|").append(myReservedRoom.getKey()).append(",");
            }
            myRequest.deleteCharAt(myRequest.length() - 1);
            var answer = roomConnect.communicateServer(myRequest.toString()).split(";");
            if (answer[0].equals("ERROR")) {
                JOptionPane.showMessageDialog(frame, answer[1], "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                frame.dispose();
                new TerminalGui();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
