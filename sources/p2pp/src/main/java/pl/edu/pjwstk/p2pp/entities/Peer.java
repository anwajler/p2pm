package pl.edu.pjwstk.p2pp.entities;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.resources.ResourceManager;
import pl.edu.pjwstk.p2pp.services.Service;
import pl.edu.pjwstk.p2pp.util.Arrays;

/**
 * Abstract class for peers of Peer-to-Peer Protocol.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public abstract class Peer extends Node {

        /** Manager of resources. TODO maybe move it to Node class. */
	protected ResourceManager resourceManager = new ResourceManager();

	public Peer() {
		super();
	}

        public abstract Byte getRoutingAlgorithm();

	@Override
	protected boolean canConsume(Message receivedMessage) {
		boolean canConsume = false;
		if (receivedMessage instanceof P2PPMessage) {
			P2PPMessage p2ppMessage = (P2PPMessage) receivedMessage;
			byte requestType = p2ppMessage.getRequestOrResponseType();
			boolean[] messageType = p2ppMessage.getMessageType();
			// TODO more handling for indications
			// handling for non-acknowledgment messages
			if (!p2ppMessage.isAcknowledgment()) {
				if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
					if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
							|| requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
							|| requestType == P2PPMessage.QUERY_MESSAGE_TYPE
							|| requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
							|| requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
							|| requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE) {
						canConsume = true;
					}
				} else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
						|| Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
					if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
							|| requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE
							|| requestType == P2PPMessage.JOIN_MESSAGE_TYPE
							|| requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
							|| requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
							|| requestType == P2PPMessage.QUERY_MESSAGE_TYPE
							|| requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
							|| requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
							|| requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE) {
						canConsume = true;
					}
				} // end of is response or responseACK
			} // end of if isn't Ack
			// if is ACK
			else {
				if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
					if (// TODO what about ACK for bootstrap response? probably only responseACK is generated, so
					// there's no need for consuming received ACK for bootstrap response
					requestType == P2PPMessage.JOIN_MESSAGE_TYPE || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
							|| requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
							|| requestType == P2PPMessage.QUERY_MESSAGE_TYPE
							|| requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
							|| requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
							|| requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE) {
						canConsume = true;
					}
				} else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
						|| Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
					if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
							|| requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.JOIN_MESSAGE_TYPE
							|| requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
							|| requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
							|| requestType == P2PPMessage.QUERY_MESSAGE_TYPE
							|| requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
							|| requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
							|| requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
							|| requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
							|| requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
							|| requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE) {
						canConsume = true;
					}
				} // end of is response or responseACK
			} // end of is ACK
		} // end of if is P2PPMessage
		return canConsume;
	}

	/**
	 * Method that adds service to this peer, so that this peer will be using it.
	 * 
	 * @param newService
	 *            Service to be added.
	 * @return Returns true if adding was successful. Returns false if this manager already has a service of that type.
	 */
	public boolean addService(Service newService) {
		newService.setOutgoingListener(outgoingListener);
		return resourceManager.addService(newService);
	}

	@Override
	public void performLeaveTasks() {
		sharedManager.leaveReset();

		resourceManager.leaveReset();

		bootstrapCandidates = null;

		// it think that transaction table shouldn't be resetted because it resets itself
		// transactionTable.leaveReset();

		// TODO should P2PPmanager be informed about a fact that we don't know properties of peerid

	}

	@Override
	public void setRoutingTable(RoutingTable routingTable) {
		super.setRoutingTable(routingTable);

		resourceManager.setRoutingTable(routingTable);
	}

	@Override
	public void setNeighborTable(NeighborTable neighborTable) {
		super.setNeighborTable(neighborTable);

		resourceManager.setNeighborTable(neighborTable);
	}

    // TODO This should not be here
	public void parseSocialFile(String unhashedID, String socialFile) {
		// TODO Auto-generated method stub
		
	}

}
