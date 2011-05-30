package pl.edu.pjwstk.net;

import java.net.InetAddress;

public class TransportPacket {

    public InetAddress	remoteAddress;
	public Integer		remotePort;
	public byte[]		packetBody;

	public TransportPacket(InetAddress remoteAddress, int remotePort, byte[] packetBody) {
		this.remoteAddress = remoteAddress;
		this.remotePort	= remotePort;
		this.packetBody = packetBody;
	}

    @Override
    public String toString() {
        StringBuilder strb = new StringBuilder("TransportPacket=[");
        strb.append("remoteAddress=");
        strb.append((this.remoteAddress!=null)?this.remoteAddress:"null");
        strb.append("; remotePort=");
        strb.append((this.remotePort!=null)?this.remotePort:"null");
        strb.append("; packetLength=");
        strb.append((this.packetBody!=null)?this.packetBody.length:"null");
        return strb.append("]").toString();
    }

}
