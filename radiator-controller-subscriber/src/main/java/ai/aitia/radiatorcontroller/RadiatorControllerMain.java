package ai.aitia.radiatorcontroller;

import eu.arrowhead.application.skeleton.subscriber.ConfigEventProperites;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@EnableConfigurationProperties(ConfigEventProperites.class)
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, RadiatorControllerConstants.BASE_PACKAGE})
public class RadiatorControllerMain {

	public static void main(final String[] args) {
		SpringApplication.run(RadiatorControllerMain.class, args);
	}
}
