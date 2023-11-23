package eu.arrowhead.application.skeleton.publisher.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import ai.aitia.arrowhead.application.library.config.DefaultSecurityConfig;
import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;

@Configuration
@EnableWebSecurity
public class PublisherSecurityConfig extends DefaultSecurityConfig {
	
	//=================================================================================================
	// members
	
	@Value(ApplicationCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	private PublisherTokenSecurityFilter tokenSecurityFilter;
	
	//=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		if (tokenSecurityFilterEnabled) {
			tokenSecurityFilter = new PublisherTokenSecurityFilter();
			http.addFilterAfter(tokenSecurityFilter, SecurityContextHolderAwareRequestFilter.class);			
		}
	}

	//-------------------------------------------------------------------------------------------------
	public PublisherTokenSecurityFilter getTokenSecurityFilter() {
		return tokenSecurityFilter;
	}	
}