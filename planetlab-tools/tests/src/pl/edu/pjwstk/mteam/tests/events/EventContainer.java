package pl.edu.pjwstk.mteam.tests.events;


public class EventContainer implements Comparable<EventContainer> {

    private String eventType;
    private Object eventData;

    public EventContainer(String eventType, Object data) {

        this.eventType = eventType;
        this.eventData = data;
    }

    public String getEventType() {
        return this.eventType;
    }

    public Object getEventData() {
        return this.eventData;
    }

    @Override
    public String toString() {
        StringBuilder strb = new StringBuilder("EventContainer[type=");
        strb.append(this.eventType).append(", data=").append(this.eventData).append("]");
        return strb.toString();
    }

    public int compareTo(EventContainer o) {
        return 1;
    }

}
