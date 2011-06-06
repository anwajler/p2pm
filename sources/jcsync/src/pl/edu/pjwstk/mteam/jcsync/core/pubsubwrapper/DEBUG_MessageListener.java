/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper;

import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/**
 *
 * @author buti
 */
public interface DEBUG_MessageListener {

    public boolean onDeliverRequest(String ID,PubSubMessage msg);
    public boolean onDeliverResponse(String ID,PubSubMessage msg);
    public boolean onDeliverIndication(String ID,PubSubMessage msg);
    
}
