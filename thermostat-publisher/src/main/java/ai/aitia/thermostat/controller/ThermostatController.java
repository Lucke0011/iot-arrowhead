package ai.aitia.thermostat.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ai.aitia.thermostat.ThermostatConstants;
import ai.aitia.thermostat.service.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.application.skeleton.publisher.event.EventTypeConstants;
import eu.arrowhead.application.skeleton.publisher.event.PresetEventType;
import eu.arrowhead.application.skeleton.publisher.service.PublisherService;

@RestController
@RequestMapping(ThermostatConstants.THERMOSTAT_URI)
public class ThermostatController {

	private int counter = -1;
    @Autowired
	private PublisherService publisherService;
    @Autowired
	private DataService dataService;
    private final Logger logger = LogManager.getLogger(ThermostatController.class);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Double getTemperature() throws IOException {
        counter++;

		publisherService.publish(
				PresetEventType.REQUEST_RECEIVED,
				Map.of(EventTypeConstants.EVENT_TYPE_REQUEST_RECEIVED_METADATA_REQUEST_TYPE, HttpMethod.GET.name()),
                ThermostatConstants.THERMOSTAT_URI);

		List<Double> temperatureData = dataService.readTemperatureData();

        return temperatureData.get(counter);
	}
}