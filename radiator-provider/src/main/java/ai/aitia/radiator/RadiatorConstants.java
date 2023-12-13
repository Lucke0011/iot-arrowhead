package ai.aitia.radiator;

public class RadiatorConstants {
    public static final String BASE_PACKAGE = "ai.aitia";

    public static final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
    public static final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
    public static final String HTTP_METHOD = "http-method";

    public static final String SERVICE_LIMIT="service_limit";
    public static final int DEFAULT_SERVICE_LIMIT=1000;
    public static final String $SERVICE_LIMIT_WD="${"+SERVICE_LIMIT+":"+DEFAULT_SERVICE_LIMIT+"}";

    public static final String RADIATOR_URI = "/radiator";
    public static final String TURN_ON_RADIATOR_SERVICE_URI = "radiator-turn-on";
    public static final String TURN_OFF_RADIATOR_SERVICE_URI = "radiator-turn-off";

    private RadiatorConstants() {
        throw new UnsupportedOperationException();
    }
}
