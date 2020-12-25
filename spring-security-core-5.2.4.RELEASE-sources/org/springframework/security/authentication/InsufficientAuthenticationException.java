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

package org.springframework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown if an authentication request is rejected because the credentials are not
 * sufficiently trusted.
 * <p>
 * {@link org.springframework.security.access.AccessDecisionVoter}s will typically throw
 * this exception if they are dissatisfied with the level of the authentication, such as
 * if performed using a remember-me mechanism or anonymously. The
 * {@code ExceptionTranslationFilter} will then typically cause the
 * {@code AuthenticationEntryPoint} to be called, allowing the principal to authenticate
 * with a stronger level of authentication.
 *
 * @author Ben Alex
 * 如果由于凭据未充分信任而拒绝身份验证请求，则抛出该异常。
 * 如果org.springframework.security.access.AccessDecisionVoters对身份验证级别不满意，
 * 例如使用“记住我”机制或匿名执行，则通常会引发此异常。 
 * 然后，ExceptionTranslationFilter通常将导致AuthenticationEntryPoint被调用，
 * 从而允许主体以更强的身份验证级别进行身份验证。
 */
public class InsufficientAuthenticationException extends AuthenticationException {
	// ~ Constructors
	// ===================================================================================================

	/**
	 * Constructs an <code>InsufficientAuthenticationException</code> with the specified
	 * message.
	 *
	 * @param msg the detail message
	 */
	public InsufficientAuthenticationException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an <code>InsufficientAuthenticationException</code> with the specified
	 * message and root cause.
	 *
	 * @param msg the detail message
	 * @param t root cause
	 */
	public InsufficientAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}
}
