package pl.edu.pjwstk.mteam.p2pm.tests.core.events;

import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventContainer;
import org.apache.log4j.Logger;

public class EventExecutor implements Runnable {

    public static final Logger LOG = Logger.getLogger(EventExecutor.class);

    private final EventContainer eventContainer;
    private final IEventSubscriber subscriber;

    public EventExecutor(EventContainer eventContainer, IEventSubscriber subscriber) {
        this.eventContainer = eventContainer;
        this.subscriber = subscriber;
    }

    public void run() {

        if (LOG.isTraceEnabled()) LOG.trace("Executing event " + this.eventContainer);

        this.subscriber.handleEvent(this.eventContainer.getEventType(), this.eventContainer.getEventData());

    }

}
