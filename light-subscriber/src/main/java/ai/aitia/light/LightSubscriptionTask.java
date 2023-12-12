package ai.aitia.light;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.subscriber.SubscriberUtilities;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

public class LightSubscriptionTask extends Thread {

    private boolean interrupted = false;

    private final Logger logger = LogManager.getLogger(LightSubscriptionTask.class);

    @Resource(name = SubscriberConstants.NOTIFICATION_QUEUE)
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

    @Autowired
    private ArrowheadService arrowheadService;

    @Autowired
    protected SSLProperties sslProperties;

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

    @Override
    public void run() {
        logger.info("SubscriberTask.run started...");

        interrupted = Thread.currentThread().isInterrupted();

        OrchestrationResultDTO thermostatRequestingService = null;

        while (!interrupted) {
            try {
                // If there is a destroy event, exit
                if (notificationQueue.peek() != null) {
                    for (final EventDTO event : notificationQueue) {
                        if (SubscriberConstants.PUBLISHER_DESTROYED_EVENT_TYPE.equalsIgnoreCase(event.getEventType())) {
                            logger.info("Recieved publisher destroyed event - started shuting down.");
                            System.exit(0);
                        } else {
                            logger.info("SubscriberTask recevied event - with type: " + event.getEventType() + ", and payload: " + event.getPayload() + ".");
                        }
                    }

                    notificationQueue.clear();
                }

                if (thermostatRequestingService != null) {
                    callThermostatRequestingService(thermostatRequestingService);
                } else {
                    thermostatRequestingService = orchestrateGetCarService();

                    if (thermostatRequestingService != null) {
                        final Set<SystemResponseDTO> sources = new HashSet<SystemResponseDTO>();

                        sources.add(thermostatRequestingService.getProvider());

                        subscribeToDestroyEvents(sources);
                    }
                }
            } catch (final Throwable ex) {
                logger.debug(ex.getMessage());

                thermostatRequestingService = null;
            }
        }
        System.exit(0);
    }

    private void subscribeToDestroyEvents(final Set<SystemResponseDTO> providers) {
        final Set<SystemRequestDTO> sources = new HashSet<>(providers.size());

        for (final SystemResponseDTO provider : providers) {
            final SystemRequestDTO source = new SystemRequestDTO();
            source.setSystemName(provider.getSystemName());
            source.setAddress(provider.getAddress());
            source.setPort(provider.getPort());

            sources.add(source);
        }

        final SystemRequestDTO subscriber = new SystemRequestDTO();
        subscriber.setSystemName(applicationSystemName);
        subscriber.setAddress(applicationSystemAddress);
        subscriber.setPort(applicationSystemPort);

        if (sslEnabled) {
            subscriber.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
        }

        try {
            arrowheadService.unsubscribeFromEventHandler(SubscriberConstants.PUBLISHER_DESTROYED_EVENT_TYPE, applicationSystemName, applicationSystemAddress, applicationSystemPort);
        } catch (final Exception ex) {
            logger.debug("Exception happened in subscription initialization " + ex);
        }

        try {
            final SubscriptionRequestDTO subscription = SubscriberUtilities.createSubscriptionRequestDTO(SubscriberConstants.PUBLISHER_DESTROYED_EVENT_TYPE, subscriber, SubscriberConstants.PUBLISHER_DESTROYED_NOTIFICATION_URI);
            subscription.setSources(sources);

            arrowheadService.subscribeToEventHandler(subscription);
        } catch (final InvalidParameterException ex) {

            if (ex.getMessage().contains("Subscription violates uniqueConstraint rules")) {
                logger.debug("Subscription is already in DB");
            } else {
                logger.debug(ex.getMessage());
                logger.debug(ex);
            }
        } catch (final Exception ex) {
            logger.debug("Could not subscribe to EventType: " + SubscriberConstants.PUBLISHER_DESTROYED_EVENT_TYPE);
        }
    }

    public void destroy() {
        logger.debug("ConsumerTask.destroy started...");

        interrupted = true;
    }

    // Dynamic orchestration where the orchestrator searches the whole local cloud to find matching providers.
    private OrchestrationResultDTO orchestrateGetCarService() {
        logger.info("Orchestration request for " + LightConstants.GET_THERMOSTAT_SERVICE_DEFINITION + " service:");
        final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO.Builder(LightConstants.GET_THERMOSTAT_SERVICE_DEFINITION)
                .interfaces(getInterface())
                .build();

        final OrchestrationFormRequestDTO.Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
        final OrchestrationFormRequestDTO orchestrationFormRequest = orchestrationFormBuilder.requestedService(serviceQueryForm)
                .flag(OrchestrationFlags.Flag.MATCHMAKING, false)
                .flag(OrchestrationFlags.Flag.OVERRIDE_STORE, true)
                .flag(OrchestrationFlags.Flag.PING_PROVIDERS, true)
                .build();

        printOut(orchestrationFormRequest);

        final OrchestrationResponseDTO orchestrationResponse = arrowheadService.proceedOrchestration(orchestrationFormRequest);

        logger.info("Orchestration response:");
        printOut(orchestrationResponse);

        if (orchestrationResponse == null) {
            logger.info("No orchestration response received");
        } else if (orchestrationResponse.getResponse().isEmpty()) {
            logger.info("No provider found during the orchestration");
        } else {
            final OrchestrationResultDTO orchestrationResult = orchestrationResponse.getResponse().get(0);
            validateOrchestrationResult(orchestrationResult, LightConstants.GET_THERMOSTAT_SERVICE_DEFINITION);

            return orchestrationResult;
        }

        return null;
    }

    private void callThermostatRequestingService(final OrchestrationResultDTO orchestrationResult) {
        validateOrchestrationResult(orchestrationResult, LightConstants.GET_THERMOSTAT_SERVICE_DEFINITION);

        logger.info("Get temperature:");
        final String token = orchestrationResult.getAuthorizationTokens() == null ? null : orchestrationResult.getAuthorizationTokens().get(getInterface());
        @SuppressWarnings("unchecked") final Double temperature = arrowheadService.consumeServiceHTTP(Double.class,
                HttpMethod.valueOf(orchestrationResult.getMetadata().get(LightConstants.HTTP_METHOD)),
                orchestrationResult.getProvider().getAddress(),
                orchestrationResult.getProvider().getPort(),
                orchestrationResult.getServiceUri(),
                getInterface(),
                token,
                null);

        printOut(temperature);
    }

    private String getInterface() {
        return sslProperties.isSslEnabled() ? LightConstants.INTERFACE_SECURE : LightConstants.INTERFACE_INSECURE;
    }

    private void validateOrchestrationResult(final OrchestrationResultDTO orchestrationResult, final String serviceDefinition) {
        if (!orchestrationResult.getService().getServiceDefinition().equalsIgnoreCase(serviceDefinition)) {
            throw new InvalidParameterException("Requested and orchestrated service definition do not match");
        }

        boolean hasValidInterface = false;
        for (final ServiceInterfaceResponseDTO serviceInterface : orchestrationResult.getInterfaces()) {
            if (serviceInterface.getInterfaceName().equalsIgnoreCase(getInterface())) {
                hasValidInterface = true;
                break;
            }
        }
        if (!hasValidInterface) {
            throw new InvalidParameterException("Requested and orchestrated interface do not match");
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void printOut(final Object object) {
        System.out.println(Utilities.toPrettyJson(Utilities.toJson(object)));
    }


}
