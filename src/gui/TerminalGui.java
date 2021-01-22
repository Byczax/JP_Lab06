package gui;

import connector.ConnectorClient;
import models.Hotel;
import models.ServerRequestType;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

public class TerminalGui {
    private JPanel mainPanel;
    private JButton reserveRoomsButton;
    private JTextField guestName;
    private JSpinner singleRoom;
    private JSpinner doubleRoom;
    private JSpinner tripleRoom;

    public TerminalGui() {
        JPanel root = mainPanel;
        JFrame frame = new JFrame();
        frame.setTitle("Registration terminal");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        reserveRoomsButton.addActionListener(e -> {
            try {
                ConnectorClient hotelConnect = new ConnectorClient(Hotel.getServerPort());
                var answer = hotelConnect.communicateServer(
                        String.format("%s;%s;%s;%s;%s"
                                , ServerRequestType.ROOM_RESERVE_START
                                , guestName.getText()
                                , singleRoom.getValue()
                                , doubleRoom.getValue()
                                , tripleRoom.getValue())).split(";");
                if (answer[0].equals("ERROR")) {
                    JOptionPane.showMessageDialog(frame, answer[1], "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    ReservedGui reservedGui = new ReservedGui();
                    for (String data : answer
                    ) {
                        var sliced = data.split(",");
                        reservedGui.addToTable(
                                Integer.parseInt(sliced[0])
                                , Integer.parseInt(sliced[1])
                                , Integer.parseInt(sliced[2])
                                , false
                                , UUID.fromString(sliced[3]));
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            frame.dispose();

        });
    }
}
