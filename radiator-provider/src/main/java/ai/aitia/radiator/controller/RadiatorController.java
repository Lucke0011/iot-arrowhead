package ai.aitia.radiator.controller;

import ai.aitia.radiator.RadiatorConstants;
import org.springframework.web.bind.annotation.*;

import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(RadiatorConstants.RADIATOR_URI)
public class RadiatorController {

    @PostMapping() //@PostMapping(path = RadiatorConstants.TURN_ON_RADIATOR_SERVICE_URI)
    public String turnOn() {
        return "Radiator turned on!";
    }

    @PutMapping() //@PostMapping(path = RadiatorConstants.TURN_OFF_RADIATOR_SERVICE_URI)
    public String turnOff() {
        return "Radiator turned off!";
    }
}
