package ai.aitia.radiator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, RadiatorConstants.BASE_PACKAGE})
public class RadiatorMain {
	public static void main(final String[] args) {
		SpringApplication.run(RadiatorMain.class, args);
	}	
}
