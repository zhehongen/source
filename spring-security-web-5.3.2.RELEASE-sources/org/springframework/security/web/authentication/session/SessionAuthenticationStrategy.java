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
package org.springframework.security.web.authentication.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

/**进行身份验证时，可插入支持与HttpSession相关的行为。
 * Allows pluggable support for HttpSession-related behaviour when an authentication
 * occurs.
 * <p>
 * Typical use would be to make sure a session exists or to change the session Id to guard
 * against session-fixation attacks.
 * 通常的用途是确保会话存在或更改会话ID以防止会话固定攻击。防止别人抓取到会话id进行攻击？
 * @author Luke Taylor
 * @since
 */
public interface SessionAuthenticationStrategy {

	/**发生新的身份验证时，执行与Http会话相关的功能。
	 * Performs Http session-related functionality when a new authentication occurs.
	 * 如果确定不允许会话认证。 这通常是因为用户一次打开了太多会话。
	 * @throws SessionAuthenticationException if it is decided that the authentication is
	 * not allowed for the session. This will typically be because the user has too many
	 * sessions open at once.
	 */
	void onAuthentication(Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) throws SessionAuthenticationException;

}
