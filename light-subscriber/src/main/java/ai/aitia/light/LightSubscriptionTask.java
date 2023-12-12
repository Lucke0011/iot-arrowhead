package ai.aitia.light;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.subscriber.SubscriberUtilities;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import ai.aitia.light.LightConstants;

public class LightSubscriptionTask extends Thread {

    private boolean interrupted = false;

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

    @Override
    public void run() {
        logger.info("SubscriberTask.run started...");

        try {
            while (true) {
                if (notificationQueue.isEmpty()) {
                    continue;
                }

                EventDTO eventDTO = notificationQueue.poll();
                String eventType = eventDTO.getEventType();

                if (!Objects.equals(eventType, LightConstants.EVENT_TYPE_NOTIFICATION)) {
                    throw new InvalidParameterException("Incorrect event type");
                }

                if (Objects.equals(eventDTO.getPayload(), "warm")) {
                    logger.info("Light is turned on!");
                } else if (Objects.equals(eventDTO.getPayload(), "cold")) {
                    logger.info("Light is turned off!");
                }
            }

        } catch (final Throwable e) {
            logger.error(e.getMessage());
            System.exit(0);
        }
    }
}
