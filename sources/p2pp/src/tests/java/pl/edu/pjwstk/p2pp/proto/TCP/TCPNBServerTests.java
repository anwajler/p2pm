package pl.edu.pjwstk.p2pp.proto.TCP;

import junit.framework.TestCase;
import org.junit.Test;
import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.proto.TCP.TCPNBServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TCPNBServerTests extends TestCase {

	@Test
	public void testTCPNBServer() throws IOException{

        TCPNBServer server = new TCPNBServer(7777);

        // server should have port assigned
        assertEquals(7777, server.getPort());

        // ServerSocketChannel tests
        ServerSocketChannel serverChannel = server.getServerSocketChannel();

        // serverChannel should be open
        assertTrue(serverChannel.isOpen());

        // serverChannel should be set non blocking
        assertFalse(serverChannel.isBlocking());

        // serverChannel should be registered
        assertTrue(serverChannel.isRegistered());

        // SelectionKey tests
        Selector selector = server.getSelector();
        SelectionKey selKey = serverChannel.keyFor(selector);
        int interestOps = selKey.interestOps();

        // selKey should be valid
        assertTrue(selKey.isValid());

        // selKey channel should be ready to accept new socket connection but not ready to read or write
        assertEquals(SelectionKey.OP_ACCEPT, interestOps & SelectionKey.OP_ACCEPT);
        assertEquals(0, interestOps & SelectionKey.OP_READ);
        assertEquals(0, interestOps & SelectionKey.OP_WRITE);

	}

    @Test
    public void testReceivePacket() throws Exception{

        TCPNBServer server = new TCPNBServer(8888); // Receiver
        server.start();

        int iPacketSize = 33;

        SocketChannel sc = null;
        InetSocketAddress isa = null;
        ByteBuffer byteBuf = ByteBuffer.allocate(iPacketSize);

        sc = SocketChannel.open();
        isa = new InetSocketAddress(server.getAddress(), server.getPort());
        sc.connect(isa);
        sc.configureBlocking(false);
        sc.write(byteBuf);
        sc.close();

        // Wait for message to be put inside receivedPackets (BlockingQueue)
        Thread.currentThread().sleep(5);

        // Test if message was correctly sent via channel, if test fail here try to increase sleep value (sleep(500)) first
        assertEquals(99, server.getBufferCapacity());

        // Test received message size
        TransportPacket received = server.getReceivedPacket();
        assertEquals(iPacketSize, received.packetBody.length);

    }


}
