package ai.aitia.radiatorcontroller;

public class RadiatorControllerConstants {
    public static final String BASE_PACKAGE = "ai.aitia";

    public static final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
    public static final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
    public static final String HTTP_METHOD = "http-method";

    public static final String EVENT_TYPE_NOTIFICATION = "NOTIFICATION";

    public static final String TURN_ON_RADIATOR_SERVICE_DEFINITION = "radiator-turn-on";
    public static final String TURN_OFF_RADIATOR_SERVICE_DEFINITION = "radiator-turn-off";

    private RadiatorControllerConstants() {
        throw new UnsupportedOperationException();
    }
}
