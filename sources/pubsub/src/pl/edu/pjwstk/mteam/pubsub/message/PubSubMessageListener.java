package pl.edu.pjwstk.mteam.pubsub.message;

import pl.edu.pjwstk.mteam.pubsub.message.indication.PubSubIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;

public interface PubSubMessageListener{
	boolean onDeliverRequest(PubSubRequest req);
	boolean onForwardingRequest(PubSubRequest req);
	boolean onDeliverIndication(PubSubIndication ind);
	boolean onDeliverResponse(PubSubResponse res);
}
