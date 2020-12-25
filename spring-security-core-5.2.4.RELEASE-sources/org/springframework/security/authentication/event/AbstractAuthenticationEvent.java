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

package org.springframework.security.authentication.event;

import org.springframework.security.core.Authentication;

import org.springframework.context.ApplicationEvent;

/**
 * Represents an application authentication event.
 * <P>
 * The <code>ApplicationEvent</code>'s <code>source</code> will be the
 * <code>Authentication</code> object.
 * </p>
 *
 * @author Ben Alex
 * 表示应用程序身份验证事件。
 * ApplicationEvent的源将是Authentication对象。
 */
public abstract class AbstractAuthenticationEvent extends ApplicationEvent {
	// ~ Constructors
	// ===================================================================================================

	public AbstractAuthenticationEvent(Authentication authentication) {
		super(authentication);
	}

	// ~ Methods
	// ========================================================================================================

	/**
	 * Getters for the <code>Authentication</code> request that caused the event. Also
	 * available from <code>super.getSource()</code>.
	 *
	 * @return the authentication request
	 * 导致事件的身份验证请求的获取器。 也可以从super.getSource（）获得。
	 *
	 * 返回值：
	 * 认证请求
	 */
	public Authentication getAuthentication() {
		return (Authentication) super.getSource();
	}
}
