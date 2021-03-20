/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.security.web.server.csrf;

import java.util.UUID;

import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A {@link ServerCsrfTokenRepository} that persists the CSRF token in a cookie named "XSRF-TOKEN" and
 * reads from the header "X-XSRF-TOKEN" following the conventions of AngularJS. When using with
 * AngularJS be sure to use {@link #withHttpOnlyFalse()} .
 *
 * @author Eric Deandrea
 * @since 5.1
 */
public final class CookieServerCsrfTokenRepository implements ServerCsrfTokenRepository {
	static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
	static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
	static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

	private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;
	private String headerName = DEFAULT_CSRF_HEADER_NAME;
	private String cookiePath;
	private String cookieDomain;
	private String cookieName = DEFAULT_CSRF_COOKIE_NAME;
	private boolean cookieHttpOnly = true;

	/**
	 * Factory method to conveniently create an instance that has
	 * {@link #setCookieHttpOnly(boolean)} set to false.
	 *
	 * @return an instance of CookieCsrfTokenRepository with
	 * {@link #setCookieHttpOnly(boolean)} set to false
	 */
	public static CookieServerCsrfTokenRepository withHttpOnlyFalse() {
		CookieServerCsrfTokenRepository result = new CookieServerCsrfTokenRepository();
		result.setCookieHttpOnly(false);
		return result;
	}

	@Override
	public Mono<CsrfToken> generateToken(ServerWebExchange exchange) {
		return Mono.fromCallable(this::createCsrfToken);
	}

	@Override
	public Mono<Void> saveToken(ServerWebExchange exchange, CsrfToken token) {
		return Mono.fromRunnable(() -> {
			String tokenValue = token != null ? token.getToken() : "";
			int maxAge = !tokenValue.isEmpty() ? -1 : 0;
			String path = this.cookiePath != null ? this.cookiePath : getRequestContext(exchange.getRequest());
			boolean secure = exchange.getRequest().getSslInfo() != null;

			ResponseCookie cookie = ResponseCookie.from(this.cookieName, tokenValue)
					.domain(this.cookieDomain)
					.httpOnly(this.cookieHttpOnly)
					.maxAge(maxAge)
					.path(path)
					.secure(secure)
					.build();

			exchange.getResponse().addCookie(cookie);
		});
	}

	@Override
	public Mono<CsrfToken> loadToken(ServerWebExchange exchange) {
		return Mono.fromCallable(() -> {
			HttpCookie csrfCookie = exchange.getRequest().getCookies().getFirst(this.cookieName);
			if ((csrfCookie == null) || !StringUtils.hasText(csrfCookie.getValue())) {
				return null;
			}
			return createCsrfToken(csrfCookie.getValue());
		});
	}

	/**
	 * Sets the HttpOnly attribute on the cookie containing the CSRF token
	 * @param cookieHttpOnly True to mark the cookie as http only. False otherwise.
	 */
	public void setCookieHttpOnly(boolean cookieHttpOnly) {
		this.cookieHttpOnly = cookieHttpOnly;
	}

	/**
	 * Sets the cookie name
	 * @param cookieName The cookie name
	 */
	public void setCookieName(String cookieName) {
		Assert.hasLength(cookieName, "cookieName can't be null");
		this.cookieName = cookieName;
	}

	/**
	 * Sets the parameter name
	 * @param parameterName The parameter name
	 */
	public void setParameterName(String parameterName) {
		Assert.hasLength(parameterName, "parameterName can't be null");
		this.parameterName = parameterName;
	}

	/**
	 * Sets the header name
	 * @param headerName The header name
	 */
	public void setHeaderName(String headerName) {
		Assert.hasLength(headerName, "headerName can't be null");
		this.headerName = headerName;
	}

	/**
	 * Sets the cookie path
	 * @param cookiePath The cookie path
	 */
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	/**
	 * Sets the cookie domain
	 * @param cookieDomain The cookie domain
	 */
	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}

	private CsrfToken createCsrfToken() {
		return createCsrfToken(createNewToken());
	}

	private CsrfToken createCsrfToken(String tokenValue) {
		return new DefaultCsrfToken(this.headerName, this.parameterName, tokenValue);
	}

	private String createNewToken() {
		return UUID.randomUUID().toString();
	}

	private String getRequestContext(ServerHttpRequest request) {
		String contextPath = request.getPath().contextPath().value();
		return StringUtils.hasLength(contextPath) ? contextPath : "/";
	}
}
