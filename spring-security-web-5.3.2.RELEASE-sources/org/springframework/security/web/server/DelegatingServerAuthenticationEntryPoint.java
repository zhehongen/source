/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.security.web.server;

import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link ServerAuthenticationEntryPoint} which delegates to multiple {@link ServerAuthenticationEntryPoint} based
 * on a {@link ServerWebExchangeMatcher}
 *
 * @author Rob Winch
 * @since 5.0
 */
public class DelegatingServerAuthenticationEntryPoint
	implements ServerAuthenticationEntryPoint {
	private final List<DelegateEntry> entryPoints;

	private ServerAuthenticationEntryPoint defaultEntryPoint = (exchange, e) -> {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	};

	public DelegatingServerAuthenticationEntryPoint(
		DelegateEntry... entryPoints) {
		this(Arrays.asList(entryPoints));
	}

	public DelegatingServerAuthenticationEntryPoint(
		List<DelegateEntry> entryPoints) {
		Assert.notEmpty(entryPoints, "entryPoints cannot be null");
		this.entryPoints = entryPoints;
	}

	public Mono<Void> commence(ServerWebExchange exchange,
		AuthenticationException e) {
		return Flux.fromIterable(this.entryPoints)
				.filterWhen( entry -> isMatch(exchange, entry))
				.next()
				.map( entry -> entry.getEntryPoint())
				.defaultIfEmpty(this.defaultEntryPoint)
				.flatMap( entryPoint -> entryPoint.commence(exchange, e));
	}

	private Mono<Boolean> isMatch(ServerWebExchange exchange, DelegateEntry entry) {
		ServerWebExchangeMatcher matcher = entry.getMatcher();
		return matcher.matches(exchange)
			.map( result -> result.isMatch());
	}

	/**
	 * EntryPoint which is used when no RequestMatcher returned true
	 */
	public void setDefaultEntryPoint(
		ServerAuthenticationEntryPoint defaultEntryPoint) {
		this.defaultEntryPoint = defaultEntryPoint;
	}

	public static class DelegateEntry {
		private final ServerWebExchangeMatcher matcher;
		private final ServerAuthenticationEntryPoint entryPoint;

		public DelegateEntry(ServerWebExchangeMatcher matcher,
			ServerAuthenticationEntryPoint entryPoint) {
			this.matcher = matcher;
			this.entryPoint = entryPoint;
		}

		public ServerWebExchangeMatcher getMatcher() {
			return this.matcher;
		}

		public ServerAuthenticationEntryPoint getEntryPoint() {
			return this.entryPoint;
		}
	}
}
