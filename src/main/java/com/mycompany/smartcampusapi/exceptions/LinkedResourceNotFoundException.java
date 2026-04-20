package com.mycompany.smartcampusapi.exceptions;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String roomId;
    public LinkedResourceNotFoundException(String roomId) {
        super("Room not found with id: " + roomId);
        this.roomId = roomId;
    }
    public String getRoomId() { return roomId; }
}