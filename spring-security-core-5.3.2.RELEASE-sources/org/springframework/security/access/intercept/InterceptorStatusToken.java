/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
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

package org.springframework.security.access.intercept;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.context.SecurityContext;

/**
 * A return object received by {@link AbstractSecurityInterceptor} subclasses.
 * <p>
 * This class reflects the status of the security interception, so that the final call to
 * {@link org.springframework.security.access.intercept.AbstractSecurityInterceptor#afterInvocation(InterceptorStatusToken, Object)}
 * can tidy up correctly.
 *
 * @author Ben Alex
 */
public class InterceptorStatusToken {
	// ~ Instance fields
	// ================================================================================================

	private SecurityContext securityContext;
	private Collection<ConfigAttribute> attr;
	private Object secureObject;
	private boolean contextHolderRefreshRequired;

	// ~ Constructors
	// ===================================================================================================

	public InterceptorStatusToken(SecurityContext securityContext,
			boolean contextHolderRefreshRequired, Collection<ConfigAttribute> attributes,
			Object secureObject) {
		this.securityContext = securityContext;
		this.contextHolderRefreshRequired = contextHolderRefreshRequired;
		this.attr = attributes;
		this.secureObject = secureObject;
	}

	// ~ Methods
	// ========================================================================================================

	public Collection<ConfigAttribute> getAttributes() {
		return attr;
	}

	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	public Object getSecureObject() {
		return secureObject;
	}

	public boolean isContextHolderRefreshRequired() {
		return contextHolderRefreshRequired;
	}
}
