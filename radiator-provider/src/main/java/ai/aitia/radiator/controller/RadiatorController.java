package ai.aitia.radiator.controller;

import ai.aitia.radiator.RadiatorConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(RadiatorConstants.RADIATOR_URI)
public class RadiatorController {

    @PostMapping(path = RadiatorConstants.TURN_ON_RADIATOR_SERVICE_DEFINITION)
    public String turnOn() {
        return "Radiator turned on!";
    }

    @PostMapping(path = RadiatorConstants.TURN_OFF_RADIATOR_SERVICE_DEFINITION)
    public String turnOff() {
        return "Radiator turned off!";
    }
}
