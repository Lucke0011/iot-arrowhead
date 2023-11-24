package eu.arrowhead.application.skeleton.subscriber;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.EventDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;


public class LightSubscriptionTask extends Thread{

    private final Logger logger = LogManager.getLogger(LightSubscriptionTask.class);

    @Resource(name = SubscriberConstants.NOTIFICATION_QUEUE)
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

    @Autowired
    private ArrowheadService arrowheadService;

    @Autowired
    protected SSLProperties sslProperties;

    @Value(ApplicationCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
    private boolean tokenSecurityFilterEnabled;

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean sslEnabled;

    @Value(ApplicationCommonConstants.$APPLICATION_SYSTEM_NAME)
    private String applicationSystemName;

    @Value(ApplicationCommonConstants.$APPLICATION_SERVER_ADDRESS_WD)
    private String applicationSystemAddress;

    @Value(ApplicationCommonConstants.$APPLICATION_SERVER_PORT_WD)
    private int applicationSystemPort;

    @Value(CarConsumerConstants.$REORCHESTRATION_WD)
    private boolean reorchestration;

    @Value(CarConsumerConstants.$MAX_RETRY_WD)
    private int max_retry;


}
