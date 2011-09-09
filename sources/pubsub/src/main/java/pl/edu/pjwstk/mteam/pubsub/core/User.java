package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

/**
 * Class representing user.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class User implements Serializable{
	private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.core.User");
	
	protected NodeInfo nodeInfo;
	
	public User(String uName){
		nodeInfo = new NodeInfo("","127.0.0.1", uName, 1000);
	}
	
	public User(NodeInfo ninfo){
		nodeInfo = ninfo;
	}

    @Override
    protected void finalize() throws Throwable {
        this.nodeInfo = null;
        super.finalize();
    }

	
	public User(byte[] bytes){
		ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
		DataInputStream dtstr = new DataInputStream(istream);
	
		nodeInfo = new NodeInfo("");
		
		try {
			//reading user name 
			byte[] encname = new byte[dtstr.readInt()];
			dtstr.read(encname);
			nodeInfo.setName(new String(encname));
			//reading peer id 
			byte[] encid = new byte[dtstr.readInt()];
			dtstr.read(encid);
			nodeInfo.setID(new String(encid));
			//writing IP address
			byte[] encip = new byte[dtstr.readInt()];
			dtstr.read(encip);
			nodeInfo.setIP(new String(encip));
			//reading port number
			nodeInfo.setPort(dtstr.readInt());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public NodeInfo getNodeInfo(){
		return nodeInfo;
	}
	
	public void setNodeInfo(NodeInfo ninf){
		nodeInfo = ninf;
	}
	
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		String uname = nodeInfo.getName();
		String pid = nodeInfo.getID();
		String ip = nodeInfo.getIP();
		try {
			//writing user name byte length
			dtstr.writeInt(uname.length());
			dtstr.write(uname.getBytes());
			//writing peer id byte length
			dtstr.writeInt(pid.length());
			dtstr.write(pid.getBytes());
			//writing peer IP byte length
			dtstr.writeInt(ip.length());
			dtstr.write(ip.getBytes());
			//writing port number
			dtstr.writeInt(nodeInfo.getPort());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ostr.toByteArray();
	}
	
	public String toString(){
		String result = nodeInfo.getName()+"("+nodeInfo.getID()+")@"+
		                nodeInfo.getIP()+":"+nodeInfo.getPort();
		return result;
	}
	
	public boolean equals(Object compareWith){
		User user = (User)compareWith;
		if(nodeInfo.getName().equals(user.nodeInfo.getName()))
			return true;
		return false;
	}
}
