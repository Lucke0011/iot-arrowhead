package ai.aitia.radiatorcontroller;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import eu.arrowhead.application.skeleton.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

public class RadiatorControllerSubscriptionTask extends Thread {

    private String status = "none";
    private final Logger logger = LogManager.getLogger(RadiatorControllerSubscriptionTask.class);

    @Autowired
    private ArrowheadService arrowheadService;
    @Autowired
    protected SSLProperties sslProperties;

    @Resource(name = SubscriberConstants.NOTIFICATION_QUEUE)
    private ConcurrentLinkedQueue<EventDTO> notificationQueue;

    @Override
    public void run() {
        logger.info("SubscriberTask.run started...");

        while (true) {
            try {
                if (notificationQueue.isEmpty()) {
                    Thread.sleep(500);
                    continue;
                }

                EventDTO eventDTO = notificationQueue.poll();
                String eventType = eventDTO.getEventType();

                if (!Objects.equals(eventType, RadiatorControllerConstants.EVENT_TYPE_NOTIFICATION)) {
                    throw new InvalidParameterException("Incorrect event type");
                }

                if (Objects.equals(eventDTO.getPayload(), "warm")) {
                    if (!Objects.equals(status, "on")) {
                        status = "on";
                        callRadiator(RadiatorControllerConstants.TURN_ON_RADIATOR_SERVICE_DEFINITION);
                        logger.info("Radiator is turned on!");
                    }
                } else if (Objects.equals(eventDTO.getPayload(), "cold")) {
                    if (!Objects.equals(status, "off")) {
                        status = "off";
                        callRadiator(RadiatorControllerConstants.TURN_OFF_RADIATOR_SERVICE_DEFINITION);
                        logger.info("Radiator is turned off!");
                    }
                }

            } catch (final Throwable e) {
                logger.error(e.getMessage());
                System.exit(0);
            }
        }
    }

    private void callRadiator(final String service) {
        OrchestrationResultDTO orchestrationResult = orchestrateService(service);

        logger.info("Change radiator status:");
        assert orchestrationResult != null;
        final String authToken = orchestrationResult.getAuthorizationTokens() == null ? null : orchestrationResult.getAuthorizationTokens().get(getInterface());

        boolean result = false;

        if (Objects.equals(service, RadiatorControllerConstants.TURN_ON_RADIATOR_SERVICE_DEFINITION)) {
            result = arrowheadService.consumeServiceHTTP(Boolean.class, HttpMethod.valueOf(orchestrationResult.getMetadata().get(RadiatorControllerConstants.HTTP_METHOD)),
                    orchestrationResult.getProvider().getAddress(), orchestrationResult.getProvider().getPort(), orchestrationResult.getServiceUri(),
                    getInterface(), authToken, null);
        } else if (Objects.equals(service, RadiatorControllerConstants.TURN_OFF_RADIATOR_SERVICE_DEFINITION)) {
            result = arrowheadService.consumeServiceHTTP(Boolean.class, HttpMethod.valueOf(orchestrationResult.getMetadata().get(RadiatorControllerConstants.HTTP_METHOD)),
                    orchestrationResult.getProvider().getAddress(), orchestrationResult.getProvider().getPort(), orchestrationResult.getServiceUri(),
                    getInterface(), authToken, null);
        }

        logger.info(service + " request successful: " + result);
    }

    private OrchestrationResultDTO orchestrateService(final String service) {
        logger.info("Orchestration request for " + service + " service:");

        final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO.Builder(service)
                .interfaces(getInterface())
                .build();

        final OrchestrationFormRequestDTO.Builder orchestrationFormRequest = arrowheadService.getOrchestrationFormBuilder();
        final OrchestrationFormRequestDTO orchestrationFormRequestDTO = orchestrationFormRequest.requestedService(serviceQueryForm)
                .flag(Flag.MATCHMAKING, false)
                .flag(Flag.OVERRIDE_STORE, true)
                .flag(Flag.PING_PROVIDERS, true)
                .build();

        System.out.println(Utilities.toPrettyJson(Utilities.toJson(orchestrationFormRequestDTO)));

        final OrchestrationResponseDTO orchestrationResponse = arrowheadService.proceedOrchestration(orchestrationFormRequestDTO);
        logger.info("Orchestration response:");
        System.out.println(Utilities.toPrettyJson(Utilities.toJson(orchestrationResponse)));

        if (orchestrationResponse == null) {
            logger.info("No orchestration response received");
        } else if (orchestrationResponse.getResponse().isEmpty()) {
            logger.info("No provider found during the orchestration");
        } else {
            final OrchestrationResultDTO orchestrationResult = orchestrationResponse.getResponse().get(0);
            validateOrchestrationResult(orchestrationResult, service);

            return orchestrationResult;

        }
        return null;
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

    private String getInterface() {
        return sslProperties.isSslEnabled() ? RadiatorControllerConstants.INTERFACE_SECURE : RadiatorControllerConstants.INTERFACE_INSECURE;
    }
}
