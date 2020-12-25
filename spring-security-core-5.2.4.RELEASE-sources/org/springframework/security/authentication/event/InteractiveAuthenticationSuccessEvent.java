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

import org.springframework.util.Assert;

/**
 * Indicates an interactive authentication was successful.
 * <P>
 * The <code>ApplicationEvent</code>'s <code>source</code> will be the
 * <code>Authentication</code> object.
 * </p>
 * <p>
 * This does not extend from <code>AuthenticationSuccessEvent</code> to avoid duplicate
 * <code>AuthenticationSuccessEvent</code>s being sent to any listeners.
 * </p>
 *
 * @author Ben Alex
 * 表示交互式身份验证成功。
 * ApplicationEvent的源将是Authentication对象。
 * 这不是从AuthenticationSuccessEvent扩展而来的，以避免将重复的AuthenticationSuccessEvents发送给任何侦听器。
 */
public class InteractiveAuthenticationSuccessEvent extends AbstractAuthenticationEvent {
	// ~ Instance fields
	// ================================================================================================

	private final Class<?> generatedBy;

	// ~ Constructors
	// ===================================================================================================

	public InteractiveAuthenticationSuccessEvent(Authentication authentication,
			Class<?> generatedBy) {
		super(authentication);
		Assert.notNull(generatedBy, "generatedBy cannot be null");
		this.generatedBy = generatedBy;
	}

	// ~ Methods
	// ========================================================================================================

	/**
	 * Getter for the <code>Class</code> that generated this event. This can be useful for
	 * generating additional logging information.
	 *
	 * @return the class
	 */
	public Class<?> getGeneratedBy() {
		return generatedBy;
	}
}
