package ai.aitia.radiator.controller;

import ai.aitia.radiator.RadiatorConstants;
import org.springframework.web.bind.annotation.*;

import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(RadiatorConstants.RADIATOR_URI)
public class RadiatorController {

    @PostMapping()
    public String turnOn() {
        return "Radiator turned on!";
    }

    @PutMapping()
    public String turnOff() {
        return "Radiator turned off!";
    }
}
