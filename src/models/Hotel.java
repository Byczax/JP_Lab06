package models;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private static int serverPort = 42069;
    private final List<Room> roomList = new ArrayList<>();

    public static void setServerPort(int myServerPort) {
        serverPort = myServerPort;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public static int getServerPort() {
        return serverPort;
    }
}
