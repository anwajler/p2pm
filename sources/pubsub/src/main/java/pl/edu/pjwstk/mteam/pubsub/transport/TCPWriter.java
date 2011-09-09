package pl.edu.pjwstk.mteam.pubsub.transport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import pl.edu.pjwstk.mteam.core.GeneralNodeInfo;
import pl.edu.pjwstk.mteam.pubsub.message.Message;

/**
 * Object used for sending messages using TCP.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class TCPWriter{
	/**
	 * Socket used for sending messages.
	 */
	//private Socket socket;
	//OutputStream outStream = null;
        BufferedOutputStream outStream = null;

        Socket socket = null;
        GeneralNodeInfo dest;
        byte [] msgToByte;
	/**
	 * Sends message.
	 * @param msg Message to be send.
	 * @return Value indicating, whether sending message was successful
	 */
	public synchronized boolean sendMessage(Message msg){            
		boolean result = false;
		try {
                        System.out.println("sending message: "+msg.toString());
			dest = msg.getDestinationInfo();
			socket = new Socket(InetAddress.getByName(dest.getIP()), dest.getPort());
			outStream =new BufferedOutputStream(socket.getOutputStream()); 
                        msgToByte = msg.encode();
			outStream.write(msgToByte);
                        outStream.flush();
                        result = true;
		}catch (UnknownHostException e) {
			result = false;
                        e.printStackTrace();
			//TODO: here handling peer failure
		} catch (IOException e) {
                        e.printStackTrace();
			result = false;
			//TODO: here handling peer failure on loopback ???
		}catch(Exception e){
                    e.printStackTrace();
                }
                finally{
            try {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                outStream = null;
                socket = null;
                dest = null;
                msgToByte = null;

            } catch (Exception ex) {
                //ex.printStackTrace();
            }
                }
		return result;
	}
}
