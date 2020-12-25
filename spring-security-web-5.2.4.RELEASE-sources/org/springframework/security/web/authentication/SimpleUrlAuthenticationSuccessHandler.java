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
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;

/**
 * <tt>AuthenticationSuccessHandler</tt> which can be configured with a default URL which
 * users should be sent to upon successful authentication.
 * <p>
 * The logic used is that of the {@link AbstractAuthenticationTargetUrlRequestHandler
 * parent class}.
 *
 * @author Luke Taylor
 * @since 3.0
 * 可以使用默认URL配置AuthenticationSuccessHandler，成功身份验证后应将默认URL发送给用户。
 * 使用的逻辑是父类的逻辑。
 */
public class SimpleUrlAuthenticationSuccessHandler extends
		AbstractAuthenticationTargetUrlRequestHandler implements
		AuthenticationSuccessHandler {

	public SimpleUrlAuthenticationSuccessHandler() {
	}

	/**
	 * Constructor which sets the <tt>defaultTargetUrl</tt> property of the base class.
	 * @param defaultTargetUrl the URL to which the user should be redirected on
	 * successful authentication.
	 * 构造函数，用于设置基类的defaultTargetUrl属性。
	 *
	 * 参数：
	 * defaultTargetUrl –成功认证后应将用户重定向到的URL。
	 */
	public SimpleUrlAuthenticationSuccessHandler(String defaultTargetUrl) {
		setDefaultTargetUrl(defaultTargetUrl);
	}

	/**
	 * Calls the parent class {@code handle()} method to forward or redirect to the target
	 * URL, and then calls {@code clearAuthenticationAttributes()} to remove any leftover
	 * session data.
	 * 调用父类的handle（）方法转发或重定向到目标URL，
	 * 然后调用clearAuthenticationAttributes（）删除所有剩余的会话数据。
	 *
	 * 指定者：
	 * 接口AuthenticationSuccessHandler中的onAuthenticationSuccess
	 * 参数：
	 * request –导致成功认证的请求
	 * 回应–回应
	 * 身份验证–在身份验证过程中创建的身份验证对象。
	 */
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		handle(request, response, authentication);
		clearAuthenticationAttributes(request);
	}

	/**
	 * Removes temporary authentication-related data which may have been stored in the
	 * session during the authentication process.
	 */
	protected final void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session == null) {
			return;
		}

		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
}
