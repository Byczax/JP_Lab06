package apps;

import connector.ConnectorClient;
import gui.HotelGui;
import models.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class HotelApp implements Runnable {

    Hotel hotel = new Hotel();
    HotelGui hotelGui = new HotelGui();

    public HotelApp() {
        new Thread(this).start();
    }

    public static void main(String[] args) {
        new HotelApp();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Hotel.getServerPort()));
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    new Thread(() -> {
                        try {
                            readingRequest(fromClient, toClient);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readingRequest(BufferedReader fromClient, PrintWriter toClient) throws IOException {
        String clientRequest = fromClient.readLine();
        while (clientRequest != null && !clientRequest.isEmpty()) {
            System.out.println(clientRequest + "\t hotel");
            String[] request = clientRequest.split(";");
            var myRequest = ServerRequestType.valueOf(request[0]);
            synchronized (this) {

                switch (myRequest) {
                    case ROOM_CONNECT:
                        // in->   ROOM_CONNECT;<Room size>
                        connectRoom(request, toClient);
                        // out->  <number>;<port>;<size>;<key>
                        break;
                    case ROOM_DISCONNECT:
                        // in->   ROOM_DISCONNECT;<number>
                        disconnectRoom(request, toClient);
                        // out->  ACCEPT;Disconnected
                        break;
                    case ROOM_RESERVE_START:
                        // in->   ROOM_RESERVE_START;clientName;<1 size count>;<2 size count>;<3 size count>
                        reserveRoom(request, toClient);
                        //out->   <number>,<port>,<size>,<key>;<number>,<port>,<size>,<key>;...
                        break;
                    case ROOM_RESERVE_END:
                        //in -> ROOM_RESERVE_END;<clientName>;<number|<key>,<number>|<key>,<number>|<key>,...
                        endReservationRoom(request, toClient);
                        //out-> ACCEPT;Unreserved rooms
                        //or
                        //out-> ERROR
                        break;
                    case ROOM_STATUS:
                        //in-> ROOM_STATUS;<room id>;<room key>
                        changeRoomStatus(request, toClient);
                        break;
                    default:
                        toClient.println("ERROR;Wrong request");
                }
            }
            clientRequest = fromClient.readLine();
        }
    }

    private void connectRoom(String[] data, PrintWriter answer) {
        var myRoomList = hotel.getRoomList();
        UUID myKey = UUID.randomUUID();
        Room myRoom;
        if (myRoomList.isEmpty()) {
            myRoom = new Room(1, 2048, Integer.parseInt(data[1]), myKey);
        } else {
            myRoom = new Room(
                    myRoomList.get(myRoomList.size() - 1).getNumber() + 1
                    , myRoomList.get(myRoomList.size() - 1).getPort() + 1
                    , Integer.parseInt(data[1])
                    , myKey
            );
        }
        hotelGui.addToTable(
                myRoom.getNumber()
                , myRoom.getPort()
                , myRoom.getSize()
                , myRoom.getReservation()
                , myRoom.isRoomOccupied()
                , myRoom.getKey());

        myRoomList.add(myRoom);
        answer.format("%d;%d;%d;%s\n"
                , myRoom.getNumber()
                , myRoom.getPort()
                , myRoom.getSize()
                , myRoom.getKey());
    }

    public void disconnectRoom(String[] request, PrintWriter toClient) {
        var myRoomList = hotel.getRoomList();
        myRoomList.remove(myRoomList.stream()
                .filter(room -> room.getNumber() == Integer.parseInt(request[1]))
                .findFirst()
                .orElseThrow());
        toClient.println("ACCEPT;disconnected");
        hotelGui.removeFromTable(Integer.parseInt(request[1]));
    }

    public void reserveRoom(String[] request, PrintWriter toClient) {
        if (Integer.parseInt(request[2]) + Integer.parseInt(request[3]) + Integer.parseInt(request[4]) == 0) {
            toClient.println("ERROR;Not enough rooms!");
            return;
        }
        var myRoomList = hotel.getRoomList();
        var sizeOne = myRoomList.stream()
                .filter(room -> room.getSize() == 1)
                .filter(room -> room.getReservation() == null)
                .collect(Collectors.toList());
        var sizeTwo = myRoomList.stream()
                .filter(room -> room.getSize() == 2)
                .filter(room -> room.getReservation() == null)
                .collect(Collectors.toList());
        var sizeThree = myRoomList.stream()
                .filter(room -> room.getSize() == 3)
                .filter(room -> room.getReservation() == null)
                .collect(Collectors.toList());
        if (sizeOne.size() < Integer.parseInt(request[2])
                || sizeTwo.size() < Integer.parseInt(request[3])
                || sizeThree.size() < Integer.parseInt(request[4])) {
            toClient.println("ERROR;Not enough rooms!");
        } else {
            String outputToClient = String.valueOf(
                    sendRooms(sizeOne, Integer.parseInt(request[2]), request[1]))
                    + sendRooms(sizeTwo, Integer.parseInt(request[3]), request[1])
                    + sendRooms(sizeThree, Integer.parseInt(request[4]), request[1]);
            toClient.println(outputToClient);
        }
    }

    private void changeRoomStatus(String[] request, PrintWriter toClient) {

        var roomList = hotel.getRoomList();
        var myRoom = roomList.stream()
                .filter(room -> room.getNumber() == Integer.parseInt(request[2]))
                .findFirst()
                .orElseThrow();
        if (myRoom.getKey().equals(UUID.fromString(request[1])))
            if (!myRoom.isRoomOccupied()) {
                myRoom.setRoomOccupied(true);
                toClient.println("Room set to busy.");
            } else {
                myRoom.setRoomOccupied(false);
                toClient.println("Room set to free");
            }
        hotelGui.editTable(myRoom.getReservation(), myRoom.isRoomOccupied(), myRoom.getNumber());
    }

    public void endReservationRoom(String[] request, PrintWriter toClient) {

        // Number|Key,Number|Key,...
//        Map<Integer, UUID> myReservedRooms = new HashMap<>();
        var splitRooms = request[1].split(",");

        var hotelRoomList = hotel.getRoomList();
        List<Room> myReservedRooms = new ArrayList<>();
        for (String splitRoom : splitRooms) {
            String[] room = splitRoom.split("\\|");
            var myNumber = Integer.parseInt(room[0]);
            var myKey = UUID.fromString(room[1]);
            var myFoundedRoom = hotelRoomList
                    .stream()
//                    .findFirst()
                    .filter(room1 -> room1.getNumber() == myNumber).filter(room1 -> room1.getKey().equals(myKey)).findFirst();
            if (myFoundedRoom.orElse(null) != null)
            myReservedRooms.add(myFoundedRoom.orElse(null));
//                    .stream()
//                    .collect(Collectors.toList());
//                    .stream()
//                    .filter(room1 -> room1.getKey().equals(myKey)).collect(Collectors.toList());
//                    .ifPresent(myReservedRooms::add);
        }
        if (myReservedRooms.stream().anyMatch(Room::isRoomOccupied)) {
            toClient.println("ERROR;Not every room is free!");
            return;
        }
        for (Room myRoom : myReservedRooms
        ) {
            UUID newKey = UUID.randomUUID();
            myRoom.changeKey(newKey);
            myRoom.setReservation(null);
            hotelGui.editTable("No", false, myRoom.getNumber());
            try {
                ConnectorClient changeKey = new ConnectorClient(myRoom.getPort());
                changeKey.communicateServer(RoomRequestType.CHANGE_KEY + ";" + newKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
            toClient.println("ACCEPT;Unregistered.");
        }
    }

    public StringBuilder sendRooms(List<Room> sizeRooms, int howMuch, String clientName) {

        int check = 1;
        StringBuilder outputToClient = new StringBuilder();

        if (howMuch == 0)
            return outputToClient;

        for (Room reservedRoom : sizeRooms) {
            reservedRoom.setReservation(clientName);
            outputToClient.append(String.format("%d,%d,%d,%s;"
                    , reservedRoom.getNumber()
                    , reservedRoom.getPort()
                    , reservedRoom.getSize()
                    , reservedRoom.getKey()
            ));
            hotelGui.editTable(clientName, false, reservedRoom.getNumber());
            if (check >= howMuch) {
                break;
            } else {
                check++;
            }
        }
        return outputToClient;
    }
}
