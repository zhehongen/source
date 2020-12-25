/*
 * Copyright 2002-2016 the original author or authors.
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
package org.springframework.security.web.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;

/**
 * Strategy used to handle a failed authentication attempt.
 * <p>
 * Typical behaviour might be to redirect the user to the authentication page (in the case
 * of a form login) to allow them to try again. More sophisticated logic might be
 * implemented depending on the type of the exception. For example, a
 * {@link CredentialsExpiredException} might cause a redirect to a web controller which
 * allowed the user to change their password.
 *
 * @author Luke Taylor
 * @since 3.0
 * 用于处理失败的身份验证尝试的策略。
 * 典型的行为可能是将用户重定向到身份验证页面（在表单登录的情况下）以允许他们重试。 根据异常的类型，可以实现更复杂的逻辑。
 * 例如，CredentialsExpiredException可能会导致重定向到Web控制器，从而允许用户更改其密码。
 */
public interface AuthenticationFailureHandler {

	/**
	 * Called when an authentication attempt fails.
	 * @param request the request during which the authentication attempt occurred.
	 * @param response the response.
	 * @param exception the exception which was thrown to reject the authentication
	 * request.
	 * 当认证失败的时候调用。咋用？登录失败算不算认证失败？
	 */
	void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException;
}
