package com.thecoderscorner.example.datacomms.stream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the server side of the teletype process, it reads lines of input
 * from the socket and echos it back to the client in upper case. This is
 * the simplest form of communication possible over a socket.
 *
 * Designed for use with the data communications section at http://www.thecoderscorner.com/data-comms
 */
public class TeletypeServer {
    private final Logger logger = Logger.getLogger("SERVER");
    public static final int SERVER_PORT = 5000;

    public static void main(String[] args) throws IOException {
        TeletypeServer server = new TeletypeServer();
        server.start();
    }

    private void start() throws IOException {
        // we firstly create a server socket on a spare port, this allows
        // our client to connect
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        logger.info("Listening on " + SERVER_PORT);

        // while our process is running
        while(!Thread.currentThread().isInterrupted()) {

            try {
                // attempt to accept a connection on our server port.
                Socket socket = serverSocket.accept();

                // as per the documentation above, here we only process
                // one connection at a time. IE: we don't call accept
                // again until the current socket is disconnected.
                logger.info("New socket connection to: " + socket.getRemoteSocketAddress());
                processSocketConnection(socket);

            }
            catch(IOException e) {
                logger.log(Level.SEVERE, "Exception in server", e);
            }
        }
        serverSocket.close();
    }

    private void processSocketConnection(Socket socket) throws IOException {
        // memory buffer for the socket data.
        byte[] bytes = new byte[1024];
        int len;

        // while the socket has more data (ie: not closed)
        while ((len = socket.getInputStream().read(bytes)) > 0) {

            // convert the bytes to a string.
            String toEcho = new String(bytes, 0, len);
            logger.info("Received data from client: " + toEcho);

            // upper case it and send it back to the client.
            socket.getOutputStream().write(toEcho.toUpperCase().getBytes());
        }
        socket.close();
    }
}
