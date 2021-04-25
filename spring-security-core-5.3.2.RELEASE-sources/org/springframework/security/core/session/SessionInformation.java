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

package org.springframework.security.core.session;

import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

import java.util.Date;
import java.io.Serializable;

/**表示Spring Security框架中的会话记录。
 * Represents a record of a session within the Spring Security framework.
 * <p>这主要用于并发会话支持。
 * This is primarily used for concurrent session support.
 * <p>会话具有三种状态：活动，过期和销毁。 通过session.invalidate（）或通过Servlet容器管理使会话罐失效的会话罐被视为“销毁”。
 * Sessions have three states: active, expired, and destroyed. A session can that is
 * invalidated by <code>session.invalidate()</code> or via Servlet Container management is
 * considered "destroyed". An "expired" session, on the other hand, is a session that
 * Spring Security wants to end because it was selected for removal for some reason
 * (generally as it was the least recently used session and the maximum sessions for the
 * user were reached). An "expired" session is removed as soon as possible by a
 * <code>Filter</code>.另一方面，“过期”会话是Spring Security想要终止的会话，因为出于某种原因（由于它是最近最少使用的会话，并且达到了用户的最大会话）而被选择删除。
 * 过滤器会尽快删除“过期”会话。
 * @author Ben Alex
 */
public class SessionInformation implements Serializable {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	// ~ Instance fields
	// ================================================================================================

	private Date lastRequest;
	private final Object principal;
	private final String sessionId;
	private boolean expired = false;

	// ~ Constructors
	// ===================================================================================================

	public SessionInformation(Object principal, String sessionId, Date lastRequest) {
		Assert.notNull(principal, "Principal required");
		Assert.hasText(sessionId, "SessionId required");
		Assert.notNull(lastRequest, "LastRequest required");
		this.principal = principal;
		this.sessionId = sessionId;
		this.lastRequest = lastRequest;
	}

	// ~ Methods
	// ========================================================================================================

	public void expireNow() {
		this.expired = true;
	}

	public Date getLastRequest() {
		return lastRequest;
	}

	public Object getPrincipal() {
		return principal;
	}

	public String getSessionId() {
		return sessionId;
	}

	public boolean isExpired() {
		return expired;
	}

	/**
	 * Refreshes the internal lastRequest to the current date and time.
	 */
	public void refreshLastRequest() {
		this.lastRequest = new Date();
	}
}
