package pl.edu.pjwstk.mteam.core;

public interface P2PInterface{

//dołączenie się do sieci (np SIP REGISTER)
	void networkJoin();
	
//odłączenie się od sieci (np SIP UNREGISTER)
	void networkLeave();
		
//wstawienie obiektu do sieci p2p
	void insert(NetworkObject object);
        
        void sendMessage(String peerId, byte[] msg);
	
//usunięcie obiektu z sieci p2p
	void remove(Object objectID);
	
//update obiektu do sieci p2p --> wykonywane przez insert ??
	void update(Object objectID, byte[] value);
	
//wyszukiwanie obiektu w sieci
	void networkLookupObject(Object params);
	
//wyszukiwanie peera/klienta w sieci
	void networkLookupUser(Object params);

//wysłanie wiadomości asynchronicznych
	/*void sendUnicastMessage(Object agentID, Object message);
	void sendMulticastMessage(Object [] agentID, Object message);
	void sendAnycastMessage(Object agentID, Object message);
	void sendBroadcastMessage(Object message);*/
	

}