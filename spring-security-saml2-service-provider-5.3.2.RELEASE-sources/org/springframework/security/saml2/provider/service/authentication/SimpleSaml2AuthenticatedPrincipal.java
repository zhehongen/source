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

package org.springframework.security.saml2.provider.service.authentication;

import java.io.Serializable;

/**
 * Default implementation of a {@link Saml2AuthenticatedPrincipal}.
 *
 * @author Clement Stoquart
 * @since 5.2.2
 */
class SimpleSaml2AuthenticatedPrincipal implements Saml2AuthenticatedPrincipal, Serializable {

	private final String name;

	SimpleSaml2AuthenticatedPrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
