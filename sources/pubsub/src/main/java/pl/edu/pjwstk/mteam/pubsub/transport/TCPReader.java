package pl.edu.pjwstk.mteam.pubsub.transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import pl.edu.pjwstk.mteam.core.MessageListener;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

/**
 * Listens for incoming messages on a specified port. Currently used by publish-subscribe 
 * layer.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class TCPReader extends Thread {

    private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.transport.TCPReader");
    private MessageListener listener;
    private ServerSocket socket;
    private boolean isRunning;
    private int port;

    /**
     * Creates new TCP socket reader.
     * @param p Port number
     * @param lstnr Object that receive buffer is to be passed to.
     */
    public TCPReader(int p, MessageListener lstnr) {
        port = p;
        listener = lstnr;
    }

    /**
     * Listens on specified port for incoming messages. When message arrives
     * it passes raw byte buffer to {@link #listener} object, which is responsible
     * for protocol-specific message parsing.
     */
    public void run() {
        isRunning = true;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            logger.fatal("NotifyReceiver::run - IOException", e);
            isRunning = false;
        }
        logger.trace("Starting listening for 'notify' on port " + port);
        Socket clientSocket = null;
        byte[] message;
        //InputStream stream;
        BufferedInputStream stream;
        ByteArrayOutputStream ostream = null;
        while (isRunning) {
            try {
                clientSocket = socket.accept();
                stream = new BufferedInputStream(clientSocket.getInputStream());
                ostream = new ByteArrayOutputStream();
                for (int c; (c = stream.read()) != -1;) {
                    ostream.write(c);
                }
                message = ostream.toByteArray();
                stream.read(message);
                listener.onDeliverMessage(message);
                message = null;
                //}
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if(clientSocket!=null)
                    clientSocket.close();
                    clientSocket = null;
                    stream = null;
                    ostream.close();
                    ostream = null;
                } catch (Exception ex) {
                }
            }
        }
        logger.trace("Exiting listener thread");
    }

    /**
     * @return Value informing, whether reader is listening on a specified port
     * 		   or not.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Method used for terminating this thread.
     * @param b When is set to <code>false</code> TCP reader thread terminates.
     */
    public synchronized void setRunning(boolean b) {
        isRunning = b;
        try {
            if(socket!=null)
                socket.close();
        } catch (IOException e) {
        }
    }
}
