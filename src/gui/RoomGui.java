package gui;

import connector.ConnectorClient;
import models.Hotel;
import models.ServerRequestType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

public class RoomGui {
    private JPanel mainPanel;
    private JLabel roomNumber;
    private JLabel roomPort;
    private JLabel roomSize;
    private JLabel roomOccupation;

    public RoomGui() {
        JPanel root = mainPanel;
        JFrame frame = new JFrame();
        frame.setTitle("Room");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                try {
                    onExit(frame);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void onExit(JFrame frame) throws IOException {
        ConnectorClient serverConnect = new ConnectorClient(Hotel.getServerPort());
        String answer = serverConnect.communicateServer(String.format("%s;%s\n", ServerRequestType.ROOM_DISCONNECT, roomNumber.getText()));
        System.out.println(answer);
        frame.dispose();
    }

    public void setRoomData(String[] roomData) {
        roomNumber.setText(roomData[0]);
        roomPort.setText(roomData[1]);
        roomSize.setText(roomData[2]);
        roomOccupation.setText("false");
    }

    public void changeOccupation() {
        roomOccupation.setText(String.valueOf(!Boolean.parseBoolean(roomOccupation.getText())));
    }
}
