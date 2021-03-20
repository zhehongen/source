/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.web.authentication.preauth;

import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.util.Assert;

/**
 * <p>
 * This AuthenticationUserDetailsService implementation creates a UserDetails object based
 * solely on the information contained in the given PreAuthenticatedAuthenticationToken.
 * The user name is set to the name as returned by
 * PreAuthenticatedAuthenticationToken.getName(), the password is set to a fixed dummy
 * value (it will not be used by the PreAuthenticatedAuthenticationProvider anyway), and
 * the Granted Authorities are retrieved from the details object as returned by
 * PreAuthenticatedAuthenticationToken.getDetails().
 *
 * <p>
 * The details object as returned by PreAuthenticatedAuthenticationToken.getDetails() must
 * implement the {@link GrantedAuthoritiesContainer} interface for this implementation to
 * work.
 *
 * @author Ruud Senden
 * @since 2.0
 */
public class PreAuthenticatedGrantedAuthoritiesUserDetailsService implements
		AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	/**
	 * Get a UserDetails object based on the user name contained in the given token, and
	 * the GrantedAuthorities as returned by the GrantedAuthoritiesContainer
	 * implementation as returned by the token.getDetails() method.
	 */
	public final UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
			throws AuthenticationException {
		Assert.notNull(token.getDetails(), "token.getDetails() cannot be null");
		Assert.isInstanceOf(GrantedAuthoritiesContainer.class, token.getDetails());
		Collection<? extends GrantedAuthority> authorities = ((GrantedAuthoritiesContainer) token
				.getDetails()).getGrantedAuthorities();
		return createUserDetails(token, authorities);
	}

	/**
	 * Creates the final <tt>UserDetails</tt> object. Can be overridden to customize the
	 * contents.
	 *
	 * @param token the authentication request token
	 * @param authorities the pre-authenticated authorities.
	 */
	protected UserDetails createUserDetails(Authentication token,
			Collection<? extends GrantedAuthority> authorities) {
		return new User(token.getName(), "N/A", true, true, true, true, authorities);
	}
}
