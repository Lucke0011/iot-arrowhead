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

    @GetMapping(path = "temperature")
	public Double getTemperature() throws IOException {
        counter++;

		List<Double> temperatureData = dataService.readTemperatureData();
		Double temperature = temperatureData.get(counter);

		if (temperature >= 20.0) {
			publisherService.publish(
					PresetEventType.NOTIFICATION,
					Map.of(EventTypeConstants.NOTIFICATION, HttpMethod.GET.name()),
					ThermostatConstants.WARM);
		} else {
			publisherService.publish(
					PresetEventType.NOTIFICATION,
					Map.of(EventTypeConstants.NOTIFICATION, HttpMethod.GET.name()),
					ThermostatConstants.COLD);
		}
        return temperature;
	}
}