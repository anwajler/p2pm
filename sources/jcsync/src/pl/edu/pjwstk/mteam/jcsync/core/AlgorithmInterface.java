package pl.edu.pjwstk.mteam.jcsync.core;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier;
/**
 * @author Piotr Bucior
 * @version 1.0
 */
public interface AlgorithmInterface {

         public JCSyncAbstractCollection getCollection(String id);

         public void onDeliverJCSyncMessage(JCSyncMessageCarrier jCSyncMessageCarrier);


}