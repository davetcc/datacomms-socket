package com.thecoderscorner.example.datacomms.messagestream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the time request server, it accepts connections on the known port and
 * awaits a TimeRequestMsg. Once this is received, it responds with a TimeResponseMsg.
 */
public class TimeRequestServer {
    // this is the port on which we will accept connections.
    public static final int SERVER_PORT = 5000;

    private final Logger logger = Logger.getLogger("SERVER");

    public static void main(String[] args) throws IOException {
        TimeRequestServer server = new TimeRequestServer();
        server.start();
    }

    private void start() throws IOException {
        // we firstly create a server socket on a spare port, this allows
        // our client to connect, this time we use the channel api.
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", SERVER_PORT));

        logger.info("Listening on " + SERVER_PORT);

        // while our process is running
        while (!Thread.currentThread().isInterrupted()) {

            try {
                // attempt to accept a connection on our server port.
                SocketChannel socket = serverSocket.accept();

                // we have a connection, lets service it.
                processSocket(socket);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "socket error", e);
            }
        }
        serverSocket.close();
    }

    private void processSocket(SocketChannel socket) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(2048);

        // it's customary to log out who's just connected.
        logger.info("Socket opened to " + socket.getRemoteAddress());

        // we read a message from the socket, and ensure it is a time request
        Message msg = Message.nextMessageFromSocket(socket, dataBuffer);
        if(msg instanceof TimeRequestMsg) {

            // get the timezone from the message and prepare the response.
            logger.info("Received time request from client, servicing");
            String zone = ((TimeRequestMsg) msg).getTimeZone();
            TimeZone tz = TimeZone.getTimeZone(zone);
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            dateFormat.setTimeZone(tz);

            // and write out the message to the socket, job done,
            // another happy client!
            TimeResponseMsg response = new TimeResponseMsg(dateFormat.format(new Date()));
            Message.sendMessage(socket, response);
        }
        else {
            logger.severe("Unexpected message " + msg);
        }

        // as in the client example, this close should really be
        // wrapped in a finally, omitted for clarity.
        socket.close();
        logger.info("Socket closed");
    }


}
