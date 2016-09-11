package com.thecoderscorner.example.datacomms.rs232commands.protocol;

import java.io.IOException;

/**
 * This class is used to send information about the arduino's state back to the
 * client. This includes the current state of all the LEDs buttons and if the
 * last command was executed successfully.
 */
public class ArduinoStatusReply {
    private final int ledsLit;
    private final int buttons;
    private boolean lastCommandSuccessful;

    public ArduinoStatusReply(int ledsLit, int buttons, boolean success) {
        this.ledsLit = ledsLit;
        this.buttons = buttons;
        this.lastCommandSuccessful = success;
    }

    /**
     * here we parse the status information from a single string
     * @param command the incoming status command.
     * @throws IOException if there is a problem.
     */
    public ArduinoStatusReply(String command) throws IOException {
        String[] params = command.split("\\s+");
        // something went wrong, just decode to blank.
        if(params.length < 4) {
            lastCommandSuccessful = false;
            buttons = 0;
            ledsLit = 0;
        }
        else {
            // successful command received.
            ledsLit = Integer.parseInt(params[1]);
            buttons = Integer.parseInt(params[2]);
            lastCommandSuccessful = Boolean.valueOf(params[3]);
        }
    }

    /**
     *  Check if a button is pressed, buttons are packed into an integer bitwise.
     *  @param buttonNumber the button number to check
     *  @return if it is on are off.
     *
     */
    public boolean isButtonPressed(int buttonNumber) {
        return (buttons & (1 << (buttonNumber-1))) != 0;
    }

    /**
     * Check if an led is on, these are pacedk into an integer bitwise.
     * @param ledNumber the led to check
     * @return if it is on or off.
     */
    public boolean isLedOn(int ledNumber) {
        return (ledsLit & (1 << (ledNumber-1))) != 0;
    }

    /**
     * checks if the last command worked properly, true or false.
     * @return the last command status
     */
    public boolean isLastCommandSuccessful() {
        return lastCommandSuccessful;
    }
}
