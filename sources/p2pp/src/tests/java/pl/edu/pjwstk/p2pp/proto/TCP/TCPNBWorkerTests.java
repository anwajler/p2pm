package pl.edu.pjwstk.p2pp.proto.TCP;

import static org.junit.Assert.*;
import org.junit.Test;

import pl.edu.pjwstk.net.proto.TCP.TCPNBWorker;
import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;

import java.net.InetAddress;

public class TCPNBWorkerTests {

	@Test
	public void testInitialize() throws Exception {

        TCPNBWorker worker = new TCPNBWorker(17777);

        // Protocol should be reliable
        assertTrue(worker.isReliable());

        // TCPServer should be running
        assertNotNull(worker.getServer());
        assertTrue(worker.getServer().isAlive());
        assertNotNull(worker.getAddress());

        // worker should be ready
        assertTrue(worker.isWorkerReady());

	}

    @Test
    public void testTCPNBWorker() throws Exception {

        TCPNBWorker worker1 = new TCPNBWorker(18888);
        assertEquals(18888, worker1.getPort());

        TCPNBWorker worker2 = new TCPNBWorker(InetAddress.getByAddress("0.0.0.1", new byte[]{0, 0, 0, 0}), 19999);
        assertEquals("0.0.0.1", worker2.getAddress().getHostName().toString());
        assertEquals(19999, worker2.getPort());

        TCPNBWorker worker3 = new TCPNBWorker(InetAddress.getByAddress("0.0.0.2", new byte[]{0, 0, 0, 0}), 11111,
                InetAddress.getByAddress("127.0.0.1", new byte[]{0, 0, 0, 0}), 11112);
        assertEquals("0.0.0.2", worker3.getAddress().getHostName().toString());
        assertEquals(11111, worker3.getPort());
        assertEquals("127.0.0.1", worker3.getRemoteAddress().getHostName().toString());
        assertEquals(11112, worker3.getRemotePort());

    }
}