package pl.edu.pjwstk.mteam.core;

import java.io.Serializable;

public class NodeInfo extends GeneralNodeInfo implements Serializable{
	private String peerId;
	private String userName;

	/**
	 * Creates NodeInfo object with specified user name, that can be described
	 * as follows:<p>
	 * uName(emptyID)@127.0.0.1:0
	 * @param uName User name.
	 */
        public NodeInfo(){
            super();
        }

    @Override
    protected void finalize() throws Throwable {
        this.peerId = null;
        this.userName = null;
        super.finalize();
    }

	public NodeInfo(String uName){
		super("127.0.0.1", 0);
		peerId = "";
		userName = uName;
	}
	
	public NodeInfo(String ipAddr, int portNum){
		super(ipAddr, portNum);
	}
	
	public NodeInfo(String pId, String ipAddr, int portNum){
		super(ipAddr, portNum);
		peerId = pId;
	}
	
	public NodeInfo(String pId, String ipAddr, String uName, int portNum){
		super(ipAddr, portNum);
		peerId = pId;
		userName = uName;
	}
	
	public String getID(){
		return this.peerId;
	}
	
	public String getName(){
		return this.userName;
	}
	
	public void setID(String pId){
		this.peerId = pId;
	}
	
	public void setName(String uName){
		this.userName = uName;
	}
	
	public String toString(){
		return getName()+"("+getID()+")@"+getIP()+":"+getPort();
	}
	
	public boolean equals(Object compareWith){
		NodeInfo ninf = (NodeInfo)compareWith;
		if(getID().equals(ninf.getID()))
			return true;
		return false;
	}
}
