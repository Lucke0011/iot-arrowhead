package eu.arrowhead.application.skeleton.publisher;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.publisher.constants.PublisherConstants;
import eu.arrowhead.application.skeleton.publisher.event.PresetEventType;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, "ai.aitia"}) //TODO: add custom packages if any
public class PublisherMain implements ApplicationRunner {

    //=================================================================================================
	// members
	
	@Value(ApplicationCommonConstants.$APPLICATION_SYSTEM_NAME)
	private String applicationSystemName;
	
	@Value(ApplicationCommonConstants.$APPLICATION_SERVER_ADDRESS_WD)
	private String applicationSystemAddress;
	
	@Value(ApplicationCommonConstants.$APPLICATION_SERVER_PORT_WD)
	private int applicationSystemPort;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	private final Logger logger = LogManager.getLogger(PublisherApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		SpringApplication.run(PublisherMain.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.debug("run started...");		
		publishRunStartedEvent();		
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	//Sample implementation of event publishing when application run started
	private void publishRunStartedEvent() {
		logger.debug( "publishRunStartedEvent started..." );
		
		final String eventType = PresetEventType.START_RUN.getEventTypeName();
		
		final SystemRequestDTO source = new SystemRequestDTO();
		source.setSystemName(applicationSystemName);
		source.setAddress(applicationSystemAddress);
		source.setPort(applicationSystemPort);
		if (sslEnabled) {
			source.setAuthenticationInfo(Base64.getEncoder().encodeToString( arrowheadService.getMyPublicKey().getEncoded()));
		}

		final Map<String,String> metadata = null;		
		final String payload = PublisherConstants.START_RUN_EVENT_PAYLOAD;		
		final String timeStamp = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());		
		final EventPublishRequestDTO publishRequestDTO = new EventPublishRequestDTO(eventType, source, metadata, payload, timeStamp);
		
		arrowheadService.publishToEventHandler(publishRequestDTO);				
	}	
}
