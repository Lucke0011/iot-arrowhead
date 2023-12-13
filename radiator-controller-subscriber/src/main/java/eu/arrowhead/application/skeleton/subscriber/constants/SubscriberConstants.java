package eu.arrowhead.application.skeleton.subscriber.constants;

public class SubscriberConstants {
    public static final String NOTIFICATION_QUEUE = "notification";
    public static final String EVENT_RECEIVED_NOTIFICATION_URI = "/" + "notification";
    public static final String PUBLISHER_DESTROYED_NOTIFICATION_URI = "/" + "publisherdestroyed";
    public static final String PRESET_EVENT_TYPES = "preset_events";
    public static final String $PRESET_EVENT_TYPES_WD = "${" + PRESET_EVENT_TYPES + ":" + SubscriberDefaults.DEFAULT_PRESET_EVENT_TYPES + "}";
    public static final String REQUEST_RECEIVED_EVENT_TYPE = "REQUEST_RECEIVED";
    public static final String PUBLISHER_DESTROYED_EVENT_TYPE = "PUBLISHER_DESTROYED";

    public static final String SUBSCRIBER_TASK = "subscribertask";

	private SubscriberConstants() {
		throw new UnsupportedOperationException();
	}
}
