package com.thecoderscorner.example.datacomms.messagestream;

import java.nio.ByteBuffer;

/**
 * Represents the TimeResponse message sent from the server to the client. This
 * is the java form of the message.
 */
public class TimeResponseMsg extends Message {

    private String currentTime;

    public TimeResponseMsg() {
    }

    public TimeResponseMsg(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    @Override
    public String toString() {
        return "TimeResponse{currentTime='" + currentTime + "'}";
    }


    /** converts the raw message into this message object */
    public void fromBytes(ByteBuffer message) {
        currentTime = stringFromMsg(message);
    }

    /** converts the message into raw bytes. */
    public void toBytes(ByteBuffer buffer) {
        stringToMsg(buffer, currentTime);
    }

}
