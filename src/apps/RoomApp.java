package apps;

import connector.ConnectorClient;
import gui.RoomGui;
import models.Hotel;
import models.Room;
import models.RoomRequestType;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class RoomApp implements Runnable {

    Room myRoom;
    RoomGui roomGui = new RoomGui();

    public RoomApp(int port, String LauncherSize) throws IOException {
        ConnectorClient hotelConnect = new ConnectorClient(port);
        boolean goodValue = false;
        String size;
        if (LauncherSize.equals("0")) {
            size = JOptionPane.showInputDialog("single (1), double(2) or triple(3)?");
        } else {
            size = LauncherSize;
        }
        while (!goodValue) {
            if (Integer.parseInt(size) < 0 || Integer.parseInt(size) > 3) {
                size = JOptionPane.showInputDialog("Wrong value, write 1,2 or 3");
            } else {
                goodValue = true;
            }
        }
        var answer = hotelConnect.communicateServer("ROOM_CONNECT;" + size).split(";");
        // out->  <port>;<number>;<size>;<key>;
        myRoom = new Room(
                Integer.parseInt(answer[0])
                , Integer.parseInt(answer[1])
                , Integer.parseInt(answer[2])
                , UUID.fromString(answer[3]));
        roomGui.setRoomData(answer);
        new Thread(this).start();
    }

    public static void main(String[] args) throws IOException {
        new RoomApp(Hotel.getServerPort(), "0");
    }

    public void run() {

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(myRoom.getPort()));
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                new Thread(() -> {
                    try {
                        readingRequests(fromClient, toClient);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readingRequests(BufferedReader fromClient, PrintWriter toClient) throws IOException {
        String clientRequest = fromClient.readLine();
        while (clientRequest != null && !clientRequest.isEmpty()) {
            String[] request = clientRequest.split(";");
            var myRequest = RoomRequestType.valueOf(request[0]);
            switch (myRequest) {
                case CHANGE_KEY:
                    changeKey(request, toClient);
                    break;
                case CHANGE_STATUS:
                    changeStatus(toClient);
                    break;
                default:
                    toClient.println("ERROR;Wrong request");
            }
            clientRequest = fromClient.readLine();
        }
    }

    private synchronized void changeKey(String[] request, PrintWriter toClient) {
        myRoom.changeKey(UUID.fromString(request[1]));
        toClient.println("ACCEPT;Changed.");
    }

    private synchronized void changeStatus(PrintWriter toClient) throws IOException {
        roomGui.changeOccupation();
        ConnectorClient hotelConnect = new ConnectorClient(Hotel.getServerPort());
        toClient.println(hotelConnect.communicateServer("ROOM_STATUS;" + myRoom.getKey() + ";" + myRoom.getNumber()));
    }
}
