package pl.edu.pjwstk.net.proto.TCP;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.proto.ProtocolWorker;
import pl.edu.pjwstk.net.proto.SupportedEncryption;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.spi.SelectorProvider;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.*;
import java.util.*;

public class TCPNBServer extends Thread
{

    private final static Logger LOG = Logger.getLogger(TCPNBServer.class);

    private final BlockingQueue<TransportPacket> receivedPackets = new LinkedBlockingQueue<TransportPacket>(100);

    private final InetAddress address;
    private final int port;

    private Selector selector;
    private ServerSocketChannel serverChannel;

    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

     public TCPNBServer(InetAddress address, int port) throws IOException, IOException{
        if (LOG.isDebugEnabled()) LOG.debug("Initializing TCPNonBlockingServer");

        this.address = address;
        this.port = port;

        Selector socketSelector = SelectorProvider.provider().openSelector();

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);       // Set server socket channel non-blocking

        InetSocketAddress isa = new InetSocketAddress(address, port);
        serverChannel.socket().bind(isa);

        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        this.selector = socketSelector;
     }

    public TCPNBServer(int port) throws IOException{
        this(InetAddress.getByAddress("0.0.0.0", new byte[]{0, 0, 0, 0}), port);
    }

    public InetAddress getAddress() {
        if (this.serverChannel.socket() == null) {
            return this.address;
        } else {
            return this.serverChannel.socket().getInetAddress();
        }
    }

    public void receivePacket(TransportPacket transportPacket) throws InterruptedException {
        this.receivedPackets.offer(transportPacket, 1, TimeUnit.SECONDS);
    }

    public int getBufferCapacity() {
        return this.receivedPackets.remainingCapacity();
    }

    public TransportPacket getReceivedPacket() throws InterruptedException {
        return this.receivedPackets.poll(100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {

        while (true){
            if (isInterrupted()) break;
            try {

                this.selector.select();

                Iterator selectedKeys = this.selector.selectedKeys().iterator();

                while(selectedKeys.hasNext()){
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()){
                        continue;
                    }

                    if(key.isAcceptable()){
                        this.accept(key);
                    } else if (key.isReadable()){
                        this.read(key);
                    } else if (key.isWritable()){
                        this.write(key);
                    }
                }
            } catch (RejectedExecutionException e) {
                LOG.warn("Could not create new TCPNonBlockingServerThread - queue is full", e);
            } catch (Throwable e) {
                LOG.error("Error while running TCPNonBlockingServer", e);
            }
        }
    }

    private void accept (SelectionKey key) throws IOException{
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with Selector, indicating we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        this.readBuffer.clear();
        int nBytes;

        try{
            nBytes = socketChannel.read(this.readBuffer);
            this.readBuffer.flip();


        } catch (IOException e){
            // Remote forcibly closed the connection
            key.cancel();
            socketChannel.close();
            return;
        }

        if (nBytes == -1){
            // Remote entity shut the socket down cleanly. Do the same.
            key.channel().close();
            key.cancel();
            return;
        }
        try {
            byte[] bb = new byte[nBytes];
            readBuffer.get(bb);

            // Hand the data off to worker
            receivePacket(new TransportPacket(((SocketChannel) key.channel()).socket().getInetAddress(),
                    ((SocketChannel) key.channel()).socket().getPort(), bb));
        } catch (Throwable e) {
            LOG.error("Error while reading packet", e);
        }
    }

    private void write(SelectionKey key) throws IOException {

    }

    public int getPort(){
        return this.port;
    }

    public ServerSocketChannel getServerSocketChannel(){
        return this.serverChannel;
    }

    public Selector getSelector(){
        return this.selector;
    }

}