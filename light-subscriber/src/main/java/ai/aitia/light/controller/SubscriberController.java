package ai.aitia.light.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberDefaults;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.EventDTO;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping(SubscriberDefaults.DEFAULT_EVENT_NOTIFICATION_BASE_URI)
public class SubscriberController {

    @Resource( name = SubscriberConstants.NOTIFICATION_QUEUE )
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

	private final Logger logger = LogManager.getLogger(SubscriberController.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = SubscriberConstants.REQUEST_RECEIVED_NOTIFICATION_URI) 
	public void receiveEventRequestReceived(@RequestBody final EventDTO event ) {
		logger.debug("receiveEventRequestReceived started...");
		
		if (event.getEventType() != null) {
            notificationQueue.add(event);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = SubscriberConstants.PUBLISHER_DESTROYED_NOTIFICATION_URI)
	public void receiveEventDestroyed(@RequestBody final EventDTO event) {
		logger.debug("receiveEventDestroyed started... ");
		
		if (event.getEventType() == null) {
            notificationQueue.add(event);
		}
	}
}