package com.thecoderscorner.example.datacomms.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class makes up the client side of our teletype, by reading
 * input from stdin and sending it over the socket. Then another
 * thread reads the resulting text that is echoed back by the server
 * and prints it to the console.
 *
 * Designed for use with the data communications section at http://www.thecoderscorner.com/data-comms
 */
public class TeletypeClient {
    private final Logger logger = Logger.getLogger("CLIENT");
    private Socket socket;

    /**
     * Create the teletype client when started.
     * @param args unused.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        TeletypeClient client = new TeletypeClient();
        client.start();
    }

    /**
     * We start the teletype client that will both read from stdin
     * and send to the server
     * @throws IOException
     */
    private void start() throws IOException {

        // Create a socket that will connect to the server and request
        // a new connection.
        socket = new Socket("localhost", TeletypeServer.SERVER_PORT);
        logger.info("Connected to " + socket.getRemoteSocketAddress());

        // we now create another thread that will read from the console
        // and write the captured text to the socket.
        new Thread(this::fromConsoleToSocket).start();

        // and on the main thread we read from the socket and write
        // the contents to the console.
        byte[] bytes = new byte[1024];
        int len;
        while((len = socket.getInputStream().read(bytes)) > 0) {
            String echoStr = new String(bytes, 0, len);
            logger.info("From Server:" + echoStr);
        }
    }

    /**
     * This method reads from stdin (console) and writes it to the socket.
     */
    public void fromConsoleToSocket() {
        // we need to read lines from stdin, buffered reader makes life easy.
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // while we still have the socket
        while(!socket.isClosed()) {
            try {
                // read a line from stdin.
                String toSend = reader.readLine();

                // and write it to the socket.
                socket.getOutputStream().write(toSend.getBytes());
                logger.info("Written to socket: " + toSend);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception writing to socket", e);
            }
        }
    }
}
