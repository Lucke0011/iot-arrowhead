package eu.arrowhead.application.skeleton.publisher.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.arrowhead.application.skeleton.publisher.constants.PublisherConstants;
import eu.arrowhead.application.skeleton.publisher.service.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.application.skeleton.publisher.event.EventTypeConstants;
import eu.arrowhead.application.skeleton.publisher.event.PresetEventType;
import eu.arrowhead.application.skeleton.publisher.service.PublisherService;
import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(PublisherConstants.THERMOSTAT_URI)
public class PublisherController {

	private int counter = -1;

	private final Logger logger = LogManager.getLogger(PublisherController.class);
	
	@Autowired
	private PublisherService publisherService;

	@Autowired
	private DataService dataService;

	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		logger.debug("echoService started...");
		
		publisherService.publish(PresetEventType.REQUEST_RECEIVED, Map.of(EventTypeConstants.EVENT_TYPE_REQUEST_RECEIVED_METADATA_REQUEST_TYPE, HttpMethod.GET.name()), CommonConstants.ECHO_URI);

		return "Got it!";
	}

	@GetMapping
	public Double getTemperature() throws IOException {
		publisherService.publish(
				PresetEventType.REQUEST_RECEIVED,
				Map.of(EventTypeConstants.EVENT_TYPE_REQUEST_RECEIVED_METADATA_REQUEST_TYPE, HttpMethod.GET.name()),
				PublisherConstants.THERMOSTAT_URI);

		List<Double> temperatureData = dataService.readTemperatureData();
		counter++;

		return temperatureData.get(counter);
	}
}