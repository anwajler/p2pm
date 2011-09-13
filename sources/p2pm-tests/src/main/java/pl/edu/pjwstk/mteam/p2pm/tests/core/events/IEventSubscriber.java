package pl.edu.pjwstk.mteam.p2pm.tests.core.events;

public interface IEventSubscriber {

    public void handleEvent(String eventType, Object data);

    //public String[] acceptedEvents();

}
