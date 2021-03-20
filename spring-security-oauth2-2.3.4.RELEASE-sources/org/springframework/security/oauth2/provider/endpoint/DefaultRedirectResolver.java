/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.provider.endpoint;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation for a redirect resolver.
 *
 * @author Ryan Heaton
 * @author Dave Syer
 */
public class DefaultRedirectResolver implements RedirectResolver {

	private Collection<String> redirectGrantTypes = Arrays.asList("implicit", "authorization_code");

	private boolean matchSubdomains = true;

	private boolean matchPorts = true;

	/**
	 * Flag to indicate that requested URIs will match if they are a subdomain of the registered value.
	 *如果请求的URI是注册值的子域，则该标志指示所请求的URI将匹配。
	 * @param matchSubdomains the flag value to set (deafult true)
	 */
	public void setMatchSubdomains(boolean matchSubdomains) {
		this.matchSubdomains = matchSubdomains;
	}

	/**
	 * Flag that enables/disables port matching between the requested redirect URI and the registered redirect URI(s).
	 * 是否要求端口也匹配
	 * @param matchPorts true to enable port matching, false to disable (defaults to true)
	 */
	public void setMatchPorts(boolean matchPorts) {
		this.matchPorts = matchPorts;
	}

	/**允许具有重定向uri的授权类型。
	 * Grant types that are permitted to have a redirect uri.
	 *
	 * @param redirectGrantTypes the redirect grant types to set
	 */
	public void setRedirectGrantTypes(Collection<String> redirectGrantTypes) {
		this.redirectGrantTypes = new HashSet<String>(redirectGrantTypes);
	}

	public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {

		Set<String> authorizedGrantTypes = client.getAuthorizedGrantTypes();
		if (authorizedGrantTypes.isEmpty()) {
			throw new InvalidGrantException("A client must have at least one authorized grant type.");
		}
		if (!containsRedirectGrantType(authorizedGrantTypes)) {
			throw new InvalidGrantException(
					"A redirect_uri can only be used by implicit or authorization_code grant types.");
		}

		Set<String> registeredRedirectUris = client.getRegisteredRedirectUri();
		if (registeredRedirectUris == null || registeredRedirectUris.isEmpty()) {
			throw new InvalidRequestException("At least one redirect_uri must be registered with the client.");
		}
		return obtainMatchingRedirect(registeredRedirectUris, requestedRedirect);
	}

	/**
	 * @param grantTypes some grant types
	 * @return true if the supplied grant types includes one or more of the redirect types
	 */
	private boolean containsRedirectGrantType(Set<String> grantTypes) {
		for (String type : grantTypes) {
			if (redirectGrantTypes.contains(type)) {
				return true;
			}
		}
		return false;
	}

	/**请求的重定向URI是否“匹配”指定的重定向URI。 对于URL，此实现测试用户请求的重定向是否从注册的重定向开始，
	 * Whether the requested redirect URI "matches" the specified redirect URI. For a URL, this implementation tests if
	 * the user requested redirect starts with the registered redirect, so it would have the same host and root path if
	 * it is an HTTP URL. The port is also matched.因此如果它是HTTP URL，则它将具有相同的主机和根路径。 端口也匹配。
	 * <p>对于其他（非URL）情况，例如某些隐式客户端，redirect_uri必须完全匹配。
	 * For other (non-URL) cases, such as for some implicit clients, the redirect_uri must be an exact match.
	 *
	 * @param requestedRedirect The requested redirect URI.请求的重定向URI
	 * @param redirectUri The registered redirect URI.注册的重定向URI
	 * @return Whether the requested redirect URI "matches" the specified redirect URI.请求的重定向URI是否“匹配”指定的重定向URI
	 */
	protected boolean redirectMatches(String requestedRedirect, String redirectUri) {
		UriComponents requestedRedirectUri = UriComponentsBuilder.fromUriString(requestedRedirect).build();
		String requestedRedirectUriScheme = (requestedRedirectUri.getScheme() != null ? requestedRedirectUri.getScheme() : "");
		String requestedRedirectUriHost = (requestedRedirectUri.getHost() != null ? requestedRedirectUri.getHost() : "");
		String requestedRedirectUriPath = (requestedRedirectUri.getPath() != null ? requestedRedirectUri.getPath() : "");

		UriComponents registeredRedirectUri = UriComponentsBuilder.fromUriString(redirectUri).build();
		String registeredRedirectUriScheme = (registeredRedirectUri.getScheme() != null ? registeredRedirectUri.getScheme() : "");
		String registeredRedirectUriHost = (registeredRedirectUri.getHost() != null ? registeredRedirectUri.getHost() : "");
		String registeredRedirectUriPath = (registeredRedirectUri.getPath() != null ? registeredRedirectUri.getPath() : "");

		boolean portsMatch = this.matchPorts ? (registeredRedirectUri.getPort() == requestedRedirectUri.getPort()) : true;

		return registeredRedirectUriScheme.equals(requestedRedirectUriScheme) &&
				hostMatches(registeredRedirectUriHost, requestedRedirectUriHost) &&
				portsMatch &&
				// Ensure exact path matching
				registeredRedirectUriPath.equals(StringUtils.cleanPath(requestedRedirectUriPath));
	}

	/**
	 * Check if host matches the registered value.
	 *
	 * @param registered the registered host
	 * @param requested the requested host
	 * @return true if they match什么玩意？是子域名就行
	 */
	protected boolean hostMatches(String registered, String requested) {
		if (matchSubdomains) {
			return registered.equals(requested) || requested.endsWith("." + registered);
		}
		return registered.equals(requested);
	}

	/**
	 * Attempt to match one of the registered URIs to the that of the requested one.
	 * 尝试将一个已注册的URI与请求的URI匹配。         用于尝试查找匹配项的已注册uri集。不能为null或空。
	 * @param redirectUris the set of the registered URIs to try and find a match. This cannot be null or empty.
	 * @param requestedRedirect the URI used as part of the request
	 * @return the matching URI
	 * @throws RedirectMismatchException if no match was found
	 */
	private String obtainMatchingRedirect(Set<String> redirectUris, String requestedRedirect) {
		Assert.notEmpty(redirectUris, "Redirect URIs cannot be empty");

		if (redirectUris.size() == 1 && requestedRedirect == null) {
			return redirectUris.iterator().next();
		}
		for (String redirectUri : redirectUris) {
			if (requestedRedirect != null && redirectMatches(requestedRedirect, redirectUri)) {
				return requestedRedirect;
			}
		}
		throw new RedirectMismatchException("Invalid redirect: " + requestedRedirect
				+ " does not match one of the registered values.");
	}
}
