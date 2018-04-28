package com.thecoderscorner.example.datacomms.rs232commands.protocol.mockserver;

import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoInterfaceControl;
import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoStatusReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * This class simulates the Arduino control interface such that we can test the client
 * without needing a connection via RS232. It shows up as the simulator option in the
 * connection dialog.
 */
public class SimulatedArduinoControl implements ArduinoInterfaceControl {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean[] leds = new boolean[6];
    private List<Consumer<ArduinoStatusReply>> replyConsumers = new CopyOnWriteArrayList<>();
    private ArrayBlockingQueue<ArduinoStatusReply> replies = new ArrayBlockingQueue<>(32);
    private Random random = new Random();

    /**
     * construct the simulator and register a thread to handle replies.
     *
     * @param comPort the com port, not important here
     * @param port the speed, not important here
     */
    public SimulatedArduinoControl(String comPort, int port) {
        logger.info("Started new serial port on {} with port {}", comPort, port);

        // this thread handles all replies
        Thread th = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(500);
                    sendReply(replies.take());
                }
            }catch(InterruptedException e){
                logger.error("Exiting thread", e);
            }
        });
        th.setDaemon(true);
        th.start();
    }

    /**
     * send a reply to message we have received.
     * @param reply the reply to send.
     */
    private void sendReply(ArduinoStatusReply reply) {
        for (Consumer<ArduinoStatusReply> replyConsumer : replyConsumers) {
            replyConsumer.accept(reply);
        }
    }

    /**
     * we got a request from the client to set an LED.
     * @param led the LED to turn on or off.
     * @param on true to turn on the LED
     */
    @Override
    public void sendLedCommand(int led, boolean on) {
        logger.info("Received a request to set led {} to {}", led, on);
        leds[led - 1] = on;
        queueResponse(true);
    }

    /**
     * We got a request from the client to print something
     * @param text the text to be printed
     */
    @Override
    public void sendPrintCommand(String text) {
        logger.info("Print command: " + text);
        queueResponse(true);
    }

    /**
     * here we formulate response messages to send back to the client.
     * @param b
     */
    private void queueResponse(boolean b)  {
        try {
            int pos = 0;
            int ledsOn = 0;
            for (boolean led : leds) {
                if(led) {
                    ledsOn |= (1<<pos);
                }
                pos++;
            }
            ArduinoStatusReply reply = new ArduinoStatusReply(ledsOn, random.nextInt(0x3f), b);
            replies.put(reply);
        } catch (InterruptedException e) {
            logger.error("Interrupted: ", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendStatusRequest() {
        logger.info("Received a request for status");
        queueResponse(false);
    }

    @Override
    public void addConnectionChangeConsumer(Consumer<Boolean> changeConsumer) {
        changeConsumer.accept(true); // always connected!
    }

    @Override
    public void addReplyConsumer(Consumer<ArduinoStatusReply> replyConsumer) {
        replyConsumers.add(replyConsumer);
    }
}
