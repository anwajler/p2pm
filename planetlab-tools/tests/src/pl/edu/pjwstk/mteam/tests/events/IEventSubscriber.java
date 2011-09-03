package pl.edu.pjwstk.mteam.tests.events;

public interface IEventSubscriber {

    public void handleEvent(String eventType, Object data);

    //public String[] acceptedEvents();

}
