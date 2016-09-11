package com.thecoderscorner.example.datacomms.rs232commands.protocol;

import gnu.io.NRSerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * This is an RS232 implementation of the ArduinoInterfaceControl. It allows for
 * sending to and receiving from a device over RE232. A simple text message based
 * protocol using a message per line is used. It uses the NR Robotics serial
 * library to talk to the device.
 */
public class SimpleRS232ArduinoController implements ArduinoInterfaceControl {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** the serial port we are going to talk to the device using */
    private final NRSerialPort serialPort;

    /** the output stream of the serial port */
    private final OutputStream outputStream;

    /** a thread that we can safely read responses on without locking the UI */
    private final Thread readerThread;

    /** records the current connection status */
    private boolean connected = false;

    /** where to send events about connection state */
    private final List<Consumer<Boolean>> connectionStateConsumers = new CopyOnWriteArrayList<>();

    /** where to send replies we receive from the device.*/
    private final List<Consumer<ArduinoStatusReply>> replyConsumers = new CopyOnWriteArrayList<>();

    /**
     * Create an instance by connecting to serial port and starting up an thread
     * suitable for receiving responses.
     * @param portName the port to connect on
     * @param baudRate the baud rate for the port.
     */
    public SimpleRS232ArduinoController(String portName, int baudRate) {
        serialPort = new NRSerialPort(portName, baudRate);
        if(!serialPort.connect()) {
            throw new IllegalStateException("Not connected to Serial port");
        }
        notifyConnectionChange(true);

        outputStream = serialPort.getOutputStream();

        readerThread = new Thread(this::readThread);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * The read thread just loops forever trying to read messages from the serial
     * port, distributing them to the reply listeners.
     */
    public void readThread() {
        logger.info("Starting read thread");
        InputStream stream = serialPort.getInputStream();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String cmd = readLineFromStream(stream);
                logger.info("Received: " + cmd);
                ArduinoStatusReply reply = new ArduinoStatusReply(cmd);
                notifyReplyConsumers(reply);
            }
            catch(IOException e) {
                logger.error("Exception while reading from rs232 port", e);
                notifyConnectionChange(false);
            }
            catch(Exception e) {
                logger.error("Unexpected exception while processing", e);
            }
        }
        logger.info("Exiting read thread");
    }

    /**
     * Reads a single line from the stream.
     * @param stream the stream to read a line from
     * @return the line of text read.
     * @throws IOException if the connection fails.
     */
    private String readLineFromStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        boolean crFound=false;
        while(!crFound) {
            int read = stream.read();
            crFound = (read == 10 || read == 13);
            if (read >= 0) {
                sb.append((char) read);
            }
        }
        return sb.toString();
    }

    /**
     * Tell all reply listeners there is a new reply
     * @param reply the reply to send to them
     */
    private void notifyReplyConsumers(ArduinoStatusReply reply) {
        for (Consumer<ArduinoStatusReply> replyConsumer : replyConsumers) {
            replyConsumer.accept(reply);
        }
    }

    /**
     * tell all connection listeners there is a change in connectivity
     * @param connected the new status.
     */
    private void notifyConnectionChange(boolean connected) {
        for (Consumer<Boolean> connectionListener : connectionStateConsumers) {
            connectionListener.accept(connected);
        }
        this.connected = connected;
    }

    /**
     * Add a new connection listener and tell them about the current state.
     * @param changeConsumer consumer interested in connection changes
     */
    @Override
    public void addConnectionChangeConsumer(Consumer<Boolean> changeConsumer) {
        connectionStateConsumers.add(changeConsumer);
        notifyConnectionChange(connected);
    }

    @Override
    public void addReplyConsumer(Consumer<ArduinoStatusReply> replyConsumer) {
        replyConsumers.add(replyConsumer);
    }

    /**
     * Send the LED command to the device
     * @param led the LED to turn on or off.
     * @param on true to turn on the LED
     */
    @Override
    public void sendLedCommand(int led, boolean on) {
        try {
            String cmd = String.format("led %d %s\n", led, on);
            logger.info("Command sent: " + cmd);
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            notifyConnectionChange(false);
        }
    }

    /**
     * Send a print command to the device
     * @param text the text to be printed
     */
    @Override
    public void sendPrintCommand(String text) {
        try {
            String cmd = String.format("print %s\n", text);
            logger.info("Command sent: " + cmd);
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            notifyConnectionChange(false);
        }
    }

    @Override
    public void sendStatusRequest() {
        try {
            outputStream.write("status\n".getBytes());
            logger.info("Sent status request");
        } catch (IOException e) {
            notifyConnectionChange(false);
        }
    }
}
