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

package org.springframework.security.web.access.channel;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

/**
 * Ensures channel security is active by review of
 * <code>HttpServletRequest.isSecure()</code> responses.
 * <p>
 * The class responds to one case-sensitive keyword, {@link #getSecureKeyword}. If this
 * keyword is detected, <code>HttpServletRequest.isSecure()</code> is used to determine
 * the channel security offered. If channel security is not present, the configured
 * <code>ChannelEntryPoint</code> is called. By default the entry point is
 * {@link RetryWithHttpsEntryPoint}.
 * <p>
 * The default <code>secureKeyword</code> is <code>REQUIRES_SECURE_CHANNEL</code>.
 *
 * @author Ben Alex
 */
public class SecureChannelProcessor implements InitializingBean, ChannelProcessor {
	// ~ Instance fields
	// ================================================================================================

	private ChannelEntryPoint entryPoint = new RetryWithHttpsEntryPoint();
	private String secureKeyword = "REQUIRES_SECURE_CHANNEL";

	// ~ Methods
	// ========================================================================================================

	public void afterPropertiesSet() {
		Assert.hasLength(secureKeyword, "secureKeyword required");
		Assert.notNull(entryPoint, "entryPoint required");
	}

	public void decide(FilterInvocation invocation, Collection<ConfigAttribute> config)
			throws IOException, ServletException {
		Assert.isTrue((invocation != null) && (config != null),
				"Nulls cannot be provided");

		for (ConfigAttribute attribute : config) {
			if (supports(attribute)) {
				if (!invocation.getHttpRequest().isSecure()) {
					entryPoint
							.commence(invocation.getRequest(), invocation.getResponse());
				}
			}
		}
	}

	public ChannelEntryPoint getEntryPoint() {
		return entryPoint;
	}

	public String getSecureKeyword() {
		return secureKeyword;
	}

	public void setEntryPoint(ChannelEntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	public void setSecureKeyword(String secureKeyword) {
		this.secureKeyword = secureKeyword;
	}

	public boolean supports(ConfigAttribute attribute) {
		return (attribute != null) && (attribute.getAttribute() != null)
				&& attribute.getAttribute().equals(getSecureKeyword());
	}
}
