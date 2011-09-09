/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper;

import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.pubsub.algorithm.CustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/**
 *
 * @author
 */
public class AlgorithmConfigurator extends DefaultAlgorithmConfigurator {

    @Override
    public CustomizableAlgorithm chooseAlgorithm(String algorithmName) {
        //registering messages for the algorithm
        CustomizableAlgorithm alg = super.chooseAlgorithm(algorithmName);

        PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier", JCSyncConstans.JCSYNC_GENERIC_MESSAGE);
        /*
         * TODO: Here algorithm could be parameterized (f.e. caches sizes could be 0
         * or very small for structured overlays and larger for unstructured ones,
         * because the cost of repairing the structure without it is larger)
         */
        return new PubSubCustomisableAlgorithm();
    }
}
