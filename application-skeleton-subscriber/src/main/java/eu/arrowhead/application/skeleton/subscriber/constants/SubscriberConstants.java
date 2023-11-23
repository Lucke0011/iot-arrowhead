package eu.arrowhead.application.skeleton.subscriber.constants;

public class SubscriberConstants {
	//=================================================================================================
	// members

	public static final String START_INIT_NOTIFICATION_URI = "/" + "startinit";
	public static final String START_RUN_NOTIFICATION_URI = "/" + "startrun";
	public static final String REQUEST_RECEIVED_NOTIFICATION_URI = "/" + "requestreceived";
	public static final String PRESET_EVENT_TYPES = "preset_events";
	public static final String $PRESET_EVENT_TYPES_WD = "${" + PRESET_EVENT_TYPES + ":" + SubscriberDefaults.DEFAULT_PRESET_EVENT_TYPES + "}";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SubscriberConstants() {
		throw new UnsupportedOperationException();
	}
}
