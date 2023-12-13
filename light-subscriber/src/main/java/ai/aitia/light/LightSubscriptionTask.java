package ai.aitia.light;

import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;

public class LightSubscriptionTask extends Thread {

    private String status = "none";

    private final Logger logger = LogManager.getLogger(LightSubscriptionTask.class);

    @Resource(name = SubscriberConstants.NOTIFICATION_QUEUE)
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

    @Override
    public void run() {
        logger.info("SubscriberTask.run started...");

        while (true) {
            try {
                if (notificationQueue.isEmpty()) {
                    Thread.sleep(500);
                    continue;
                }

                EventDTO eventDTO = notificationQueue.poll();
                String eventType = eventDTO.getEventType();

                if (!Objects.equals(eventType, LightConstants.EVENT_TYPE_NOTIFICATION)) {
                    throw new InvalidParameterException("Incorrect event type");
                }

                if (Objects.equals(eventDTO.getPayload(), "warm")) {
                    if (!Objects.equals(status, "on")) {
                        status = "on";
                        logger.info("Light is turned on!");
                    }
                } else if (Objects.equals(eventDTO.getPayload(), "cold")) {
                    if (!Objects.equals(status, "off")) {
                        status = "off";
                        logger.info("Light is turned off!");
                    }
                }

            } catch (final Throwable e) {
                logger.error(e.getMessage());
                System.exit(0);
            }
        }
    }
}
