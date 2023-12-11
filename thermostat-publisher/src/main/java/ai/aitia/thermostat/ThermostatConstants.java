package ai.aitia.thermostat;

public class ThermostatConstants {
    public static final String BASE_PACKAGE = "ai.aitia";

    public static final String GET_THERMOSTAT_SERVICE_DEFINITION = "get-thermostat";
    public static final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
    public static final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
    public static final String HTTP_METHOD = "http-method";
    public static final String THERMOSTAT_URI = "/thermostat";

    public static final String SERVICE_LIMIT="service_limit";
    public static final int DEFAULT_SERVICE_LIMIT=1000;
    public static final String $SERVICE_LIMIT_WD="${"+SERVICE_LIMIT+":"+DEFAULT_SERVICE_LIMIT+"}";

    private ThermostatConstants() {
        throw new UnsupportedOperationException();
    }
}
