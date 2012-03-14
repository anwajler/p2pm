package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.algorithm.CustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/**
 *
 * @author Piotr Bucior
 */
public class PubSubAlgorithmConfigurator extends DefaultAlgorithmConfigurator {

    @Override
    public CustomizableAlgorithm chooseAlgorithm(String algorithmName) {
        //registering messages for the algorithm
        CustomizableAlgorithm alg = super.chooseAlgorithm(algorithmName);
        //not used
//
        PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.jcsync.core.pubsub.StandardResponse_", StandardResponse_.MSG_STDRESPONSE_);
//        PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.jcsync.message,JCSyncIndication", JCSyncConstans.JCSYNC_GENERIC_INDICATION);
//        PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.jcsync.message,JCSyncResponse", JCSyncConstans.JCSYNC_GENERIC_RESPONSE);
        return new PubSubCustomisableAlgorithm();
    }

}
