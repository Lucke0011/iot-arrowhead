package ai.aitia.thermostat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, ThermostatConstants.BASE_PACKAGE})
public class ThermostatMain {
    public static void main(final String[] args) {
        SpringApplication.run(ThermostatMain.class, args);
    }
}
