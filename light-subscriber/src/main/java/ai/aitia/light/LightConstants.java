package ai.aitia.light;

public class LightConstants {
    public static final String BASE_PACKAGE = "ai.aitia";

    public static final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
    public static final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
    public static final String HTTP_METHOD = "http-method";

    public static final String GET_THERMOSTAT_SERVICE_DEFINITION = "get-thermostat";
    public static final String $REORCHESTRATION_WD = "${reorchestration:false}";
    public static final String $MAX_RETRY_WD = "${max_retry:300}";

    private LightConstants() {
        throw new UnsupportedOperationException();
    }
}
