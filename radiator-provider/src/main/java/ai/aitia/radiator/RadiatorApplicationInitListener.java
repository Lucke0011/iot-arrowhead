package ai.aitia.radiator;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.config.ApplicationInitListener;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.application.skeleton.provider.security.ProviderSecurityConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class RadiatorApplicationInitListener extends ApplicationInitListener {
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	private ProviderSecurityConfig providerSecurityConfig;
	
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
	
	private final Logger logger = LogManager.getLogger(RadiatorApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		checkConfiguration();
		
		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICEREGISTRY);
		if (sslEnabled && tokenSecurityFilterEnabled) {
			checkCoreSystemReachability(CoreSystem.AUTHORIZATION);			

			//Initialize Arrowhead Context
			arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);			
		
			setTokenSecurityFilter();
		
		}else {
			logger.info("TokenSecurityFilter in not active");
		}

        //Register services into ServiceRegistry
        //final ServiceRegistryRequestDTO radiatorOnServiceRequest = createServiceRegistryRequest(RadiatorConstants.TURN_ON_RADIATOR_SERVICE_URI, RadiatorConstants.RADIATOR_URI + RadiatorConstants.TURN_ON_RADIATOR_SERVICE, HttpMethod.POST);
        final ServiceRegistryRequestDTO radiatorOnServiceRequest = createServiceRegistryRequest(RadiatorConstants.TURN_ON_RADIATOR_SERVICE_URI, RadiatorConstants.RADIATOR_URI, HttpMethod.POST);
        arrowheadService.forceRegisterServiceToServiceRegistry(radiatorOnServiceRequest);

        //final ServiceRegistryRequestDTO radiatorOnServiceRequest = createServiceRegistryRequest(RadiatorConstants.TURN_OFF_RADIATOR_SERVICE_URI, RadiatorConstants.RADIATOR_URI + RadiatorConstants.TURN_OFF_RADIATOR_SERVICE, HttpMethod.POST);
        final ServiceRegistryRequestDTO radiatorOffServiceRequest = createServiceRegistryRequest(RadiatorConstants.TURN_OFF_RADIATOR_SERVICE_URI, RadiatorConstants.RADIATOR_URI, HttpMethod.POST);
        arrowheadService.forceRegisterServiceToServiceRegistry(radiatorOffServiceRequest);

        if (arrowheadService.echoCoreSystem(CoreSystem.EVENTHANDLER)) {
            arrowheadService.updateCoreServiceURIs(CoreSystem.EVENTHANDLER);
        }
	}

    private ServiceRegistryRequestDTO createServiceRegistryRequest(final String serviceDefinition, final String serviceUri, final HttpMethod httpMethod) {
        final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
        serviceRegistryRequest.setServiceDefinition(serviceDefinition);
        final SystemRequestDTO systemRequest = new SystemRequestDTO();
        systemRequest.setSystemName(applicationSystemName);
        systemRequest.setAddress(applicationSystemAddress);
        systemRequest.setPort(applicationSystemPort);

        if (sslEnabled && tokenSecurityFilterEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(ServiceSecurityType.TOKEN.name());
            serviceRegistryRequest.setInterfaces(List.of(RadiatorConstants.INTERFACE_SECURE));
        } else if (sslEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(ServiceSecurityType.CERTIFICATE.name());
            serviceRegistryRequest.setInterfaces(List.of(RadiatorConstants.INTERFACE_SECURE));
        } else {
            serviceRegistryRequest.setSecure(ServiceSecurityType.NOT_SECURE.name());
            serviceRegistryRequest.setInterfaces(List.of(RadiatorConstants.INTERFACE_INSECURE));
        }
        serviceRegistryRequest.setProviderSystem(systemRequest);
        serviceRegistryRequest.setServiceUri(serviceUri);
        serviceRegistryRequest.setMetadata(new HashMap<>());
        serviceRegistryRequest.getMetadata().put(RadiatorConstants.HTTP_METHOD, httpMethod.name());
        return serviceRegistryRequest;
    }

	//-------------------------------------------------------------------------------------------------
	private void checkConfiguration() {
		if (!sslEnabled && tokenSecurityFilterEnabled) {			 
			logger.warn("Contradictory configuration:");
			logger.warn("token.security.filter.enabled=true while server.ssl.enabled=false");
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
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());
		
		providerSecurityConfig.getTokenSecurityFilter().setAuthorizationPublicKey(authorizationPublicKey);
		providerSecurityConfig.getTokenSecurityFilter().setMyPrivateKey(providerPrivateKey);

	}
}
