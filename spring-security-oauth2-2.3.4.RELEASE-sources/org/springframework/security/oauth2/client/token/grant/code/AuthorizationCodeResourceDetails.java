package org.springframework.security.oauth2.client.token.grant.code;

import org.springframework.security.oauth2.client.token.grant.redirect.AbstractRedirectResourceDetails;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 * 这尼玛咋初始化的？ 在OAuth2ProtectedResourceDetailsConfiguration里面
 */
public class AuthorizationCodeResourceDetails extends AbstractRedirectResourceDetails {

	public AuthorizationCodeResourceDetails() {
		setGrantType("authorization_code");
	}

}
