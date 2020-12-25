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

package org.springframework.security.web.server.authentication.logout;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

/**
 * Strategy for when log out was successfully performed (typically after {@link ServerLogoutHandler} is invoked).
 * @author Rob Winch
 * @since 5.0
 * @see ServerLogoutHandler
 */
public interface ServerLogoutSuccessHandler {

	/**
	 * Invoked after log out was successful
	 * @param exchange the exchange
	 * @param authentication the {@link Authentication}
	 * @return a completion notification (success or error)
	 */
	Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication);
}
