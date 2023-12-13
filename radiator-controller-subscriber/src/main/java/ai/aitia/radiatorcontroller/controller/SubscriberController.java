package ai.aitia.radiatorcontroller.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberDefaults;
import eu.arrowhead.common.dto.shared.EventDTO;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping(SubscriberDefaults.DEFAULT_EVENT_NOTIFICATION_BASE_URI)
public class SubscriberController {

    @Resource( name = SubscriberConstants.NOTIFICATION_QUEUE )
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

	private final Logger logger = LogManager.getLogger(SubscriberController.class);
	
	//-------------------------------------------------------------------------------------------------

	@PostMapping(path = SubscriberConstants.EVENT_RECEIVED_NOTIFICATION_URI)
	public void receiveEvent(@RequestBody final EventDTO event ) {
		logger.info("Received Event");
		
		if (event.getEventType() != null) {
            notificationQueue.add(event);
		}
	}
}