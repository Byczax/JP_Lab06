package models;

import java.util.UUID;

public class Room {
    int number;
    int port;
    int size;
    UUID key;
    String reservation;
    boolean isRoomOccupied;

    public Room(int number, int port, int size, UUID key) {
        this.number = number;
        this.port = port;
        this.size = size;
        this.key = key;
        this.reservation = null;
        this.isRoomOccupied = false;
    }

    public int getPort() {
        return port;
    }


    public int getNumber() {
        return number;
    }


    public int getSize() {
        return size;
    }


    public UUID getKey() {
        return key;
    }

    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
    }

    public boolean isRoomOccupied() {
        return isRoomOccupied;
    }

    public void setRoomOccupied(boolean roomOccupied) {
        isRoomOccupied = roomOccupied;
    }

    public void changeKey(UUID newKey) {
        this.key = newKey;
    }
}
