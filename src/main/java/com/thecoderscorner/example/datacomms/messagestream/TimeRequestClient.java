package com.thecoderscorner.example.datacomms.messagestream;

import sun.nio.ch.Net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A time request client that makes the request to the server and waits for the result.
 * Once the result arrives the client exits.
 */
public class TimeRequestClient {
    Logger logger = Logger.getLogger("CLIENT");

    public static void main(String[] args) throws IOException {
        TimeRequestClient client = new TimeRequestClient();
        client.start();
    }

    private void start() throws IOException {

        // open the socket and connect to the server on the known port.
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", TimeRequestServer.SERVER_PORT));

        logger.info("Connected to server: " + channel.getRemoteAddress());


        // if we have an open channel, then lets do the request.
        if(channel.isOpen()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

            // we formulate the request message and send it to the server.
            TimeRequestMsg msg = new TimeRequestMsg();
            msg.setTimezone(TimeZone.getDefault());
            Message.sendMessage(channel, msg);

            // we then await the servers response.
            Message response = Message.nextMessageFromSocket(channel, byteBuffer);
            if(! (response instanceof TimeResponseMsg)) {
                // didn't get what we expected!
                throw new IOException("Unexpected response from server:" + response);
            }
            logger.info("Response: " + response);
        }

        // don't forget to close the channel, really in a real world example this
        // would be in a finally block, to ensure it's always called.
        channel.close();

        logger.info("channel closed");
    }
}
