package ai.aitia.light;

import eu.arrowhead.application.skeleton.subscriber.ConfigEventProperites;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@EnableConfigurationProperties(ConfigEventProperites.class)
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, LightConstants.BASE_PACKAGE})
public class LightMain {

	public static void main(final String[] args) {
		SpringApplication.run(LightMain.class, args);
	}
}
