package pl.edu.pjwstk.mteam.tests.events;

import pl.edu.pjwstk.mteam.tests.events.EventContainer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class EventManager extends Thread {

    public static final Logger LOG = Logger.getLogger(EventManager.class);

    private static final EventManager _instance = new EventManager();
    static {
        _instance.setDaemon(true);
        _instance.start();
    }

    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());//,
    //        new NamedThreadFactory("EventManagerPool"));

    private final PriorityBlockingQueue<EventContainer> eventsQueue = new PriorityBlockingQueue<EventContainer>(50);

    private final Map<String, List<IEventSubscriber>> subscribers = new ConcurrentHashMap<String, List<IEventSubscriber>>(20);


    private EventManager() {
        LOG.info("Initializing EventManager");
        this.poolExecutor.prestartAllCoreThreads();
    }

    public static EventManager getInstance() {
        return _instance;
    }


    public void addEventToQueue(String eventType, Object data) {

        if (LOG.isDebugEnabled()) {
            LOG.info("Adding  event to queue: eventType=" + eventType + " data=" + data);
        }

        this.eventsQueue.add(new EventContainer(eventType, data));

        synchronized (_instance) {
            _instance.notify();
        }

    }

    public void subscribe(String eventType, IEventSubscriber subscriber) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Subscribing " + subscriber + " to " + eventType);
        }

        if (!this.subscribers.containsKey(eventType)) {
            this.subscribers.put(eventType, new ArrayList<IEventSubscriber>(20));
        }

        Collection<IEventSubscriber> eventSubscribers = this.subscribers.get(eventType);
        if (!eventSubscribers.contains(subscriber)) {
            eventSubscribers.add(subscriber);
        }

    }


    public void run() {

        while (true) {

            try {
                EventContainer ec = null;
                while ((ec = this.eventsQueue.poll()) != null) {
                //while ((ec = this.eventsQueue.take()) != null) {
                    if (LOG.isTraceEnabled()) 
                    LOG.info("Processing event " + ec);
                    String ecEventType = ec.getEventType();
                    if (this.subscribers.containsKey(ecEventType)) {
                        for (IEventSubscriber subscriber : this.subscribers.get(ecEventType)) {
                            subscriber.handleEvent(ec.getEventType(), ec.getEventData());
                            //this.poolExecutor.submit(new EventExecutor(ec, subscriber));

                        }
                    }
                }
                synchronized (_instance) {
                    _instance.wait();
                }

                

            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

    }

}
