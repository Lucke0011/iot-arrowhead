package ai.aitia.radiatorcontroller;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.arrowhead.application.skeleton.subscriber.ConfigEventProperites;
import eu.arrowhead.application.skeleton.subscriber.SubscriberUtilities;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.dto.shared.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.config.ApplicationInitListener;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.subscriber.security.SubscriberSecurityConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Component
public class SubscriberApplicationInitListener extends ApplicationInitListener {

	@Autowired
	private ArrowheadService arrowheadService;

	@Autowired
	private SubscriberSecurityConfig subscriberSecurityConfig;

	@Value(ApplicationCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;

	@Value(ApplicationCommonConstants.$APPLICATION_SYSTEM_NAME)
	private String applicationSystemName;

	@Value(ApplicationCommonConstants.$APPLICATION_SERVER_ADDRESS_WD)
	private String applicationSystemAddress;

	@Value(ApplicationCommonConstants.$APPLICATION_SERVER_PORT_WD)
	private int applicationSystemPort;

	private final Logger logger = LogManager.getLogger(SubscriberApplicationInitListener.class);

	@Autowired
	private ConfigEventProperites configEventProperites;

	@Autowired
	private ApplicationContext applicationContext;

    @Bean(SubscriberConstants.SUBSCRIBER_TASK)
    private RadiatorControllerSubscriptionTask lightSubscriptionTask() {
		return new RadiatorControllerSubscriptionTask();
	}

    @Bean( SubscriberConstants.NOTIFICATION_QUEUE )
    public ConcurrentLinkedQueue<EventDTO> getNotificationQueue() {
        return new ConcurrentLinkedQueue<>();
    }

	//-------------------------------------------------------------------------------------------------

	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		checkConfiguration();
		
		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICEREGISTRY);

		checkCoreSystemReachability(CoreSystem.ORCHESTRATOR);
		arrowheadService.updateCoreServiceURIs(CoreSystem.ORCHESTRATOR);

		if (sslEnabled) {

			if (tokenSecurityFilterEnabled) {
				checkCoreSystemReachability(CoreSystem.AUTHORIZATION);

				//Initialize Arrowhead Context
				arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);

				setTokenSecurityFilter();

			} else {
				logger.info("TokenSecurityFilter in not active");
			}

			setNotificationFilter();

		}

		//Register system into ServiceRegistry manually

		if (arrowheadService.echoCoreSystem(CoreSystem.EVENTHANDLER)) {
			arrowheadService.updateCoreServiceURIs(CoreSystem.EVENTHANDLER);
			subscribeToPresetEvents();
		}

		RadiatorControllerSubscriptionTask subscriptionTask = applicationContext.getBean(SubscriberConstants.SUBSCRIBER_TASK, RadiatorControllerSubscriptionTask.class);
		subscriptionTask.start();
	}

	//-------------------------------------------------------------------------------------------------

    private void subscribeToPresetEvents() {

		final Map<String, String> eventTypeMap = configEventProperites.getEventTypeURIMap();

		if(eventTypeMap == null) {
			logger.info("No preset events to subscribe.");
		} else {
			final SystemRequestDTO subscriber = new SystemRequestDTO();
			subscriber.setSystemName(applicationSystemName);
			subscriber.setAddress(applicationSystemAddress);
			subscriber.setPort(applicationSystemPort);
			if (sslEnabled) {
				subscriber.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			}

			for (final String eventType  : eventTypeMap.keySet()) {
				try {
					arrowheadService.unsubscribeFromEventHandler(eventType, applicationSystemName, applicationSystemAddress, applicationSystemPort);
				} catch (final Exception ex) {
					logger.debug("Exception happend in subscription initalization " + ex);
				}

				try {
					arrowheadService.subscribeToEventHandler(SubscriberUtilities.createSubscriptionRequestDTO(eventType, subscriber, eventTypeMap.get(eventType)));
				} catch ( final InvalidParameterException ex) {
					if( ex.getMessage().contains("Subscription violates uniqueConstraint rules")) {
						logger.debug("Subscription is already in DB");
					}
				} catch (final Exception ex) {
					logger.debug("Could not subscribe to EventType: " + eventType);
				}
			}
		}
	}

	//-------------------------------------------------------------------------------------------------

    private void setTokenSecurityFilter() {

        final PublicKey authorizationPublicKey = arrowheadService.queryAuthorizationPublicKey();
        if (authorizationPublicKey == null) {
            throw new ArrowheadException("Authorization public key is null");
        }

        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
            keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            throw new ArrowheadException(ex.getMessage());
        }
        final PrivateKey subscriberPrivateKey = Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());

        final Map<String, String> eventTypeMap = configEventProperites.getEventTypeURIMap();

        subscriberSecurityConfig.getTokenSecurityFilter().setEventTypeMap( eventTypeMap );
        subscriberSecurityConfig.getTokenSecurityFilter().setAuthorizationPublicKey(authorizationPublicKey);
        subscriberSecurityConfig.getTokenSecurityFilter().setMyPrivateKey(subscriberPrivateKey);

    }

    private void setNotificationFilter() {
		logger.debug("setNotificationFilter started...");

		final Map<String, String> eventTypeMap = configEventProperites.getEventTypeURIMap();

		subscriberSecurityConfig.getNotificationFilter().setEventTypeMap( eventTypeMap );
		subscriberSecurityConfig.getNotificationFilter().setServerCN(arrowheadService.getServerCN());
	}

    private void checkConfiguration() {
        if (!sslEnabled && tokenSecurityFilterEnabled) {
            logger.warn("Contradictory configuration:");
            logger.warn("token.security.filter.enabled=true while server.ssl.enabled=false");
        }
    }
}
