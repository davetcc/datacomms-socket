package com.thecoderscorner.example.datacomms.rs232commands.protocol;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Provision to send commands to the device for processing.
 * In java this is a very common pattern, as it allows us to replace the RS232
 * class with anything else that implements this interface.
 */
public interface ArduinoInterfaceControl {

    /**
     * Request that an LED be turned on or off.
     * @param led the LED to turn on or off.
     * @param on true to turn on the LED
     * @throws IOException if the connection is lost.
     */
    void sendLedCommand(int led, boolean on);

    /**
     * Request that text be displayed on the Arduino display
     * @param text the text to be printed
     * @throws IOException if the connection is lost
     */
    void sendPrintCommand(String text);

    /**
     * Request the state of a given button from the Arduino
     * @param button the numbered button to test.
     * @throws IOException
     */
    void sendStatusRequest();

    /**
     * Here we provide the means to determine if the connection has been
     * started or dropped.
     * @param changeConsumer consumer interested in connection changes
     */
    void addConnectionChangeConsumer(Consumer<Boolean> changeConsumer);

    /**
     * Here we provide the means of receiving replies to commands that
     * have been sent to the Arduino device.
     * @param replyConsumer consumer interested in replies from the board
     */
    void addReplyConsumer(Consumer<ArduinoStatusReply> replyConsumer);
}
