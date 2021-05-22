/*
 * Copyright 2014-2019 the original author or authors.
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

package org.springframework.session.web.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.session.web.http.CookieSerializer.CookieValue;

/**使用cookie从中获取会话的HttpSessionIdResolver。具体来说，此实现将允许使用setCookieSerializer（CookieSerializer）指定cookie序列化策略。默认的cookie名称是“ SESSION”。创建会话时，HTTP响应将包含一个具有指定Cookie名称和会话ID值的Cookie。该cookie将被标记为会话cookie，使用上下文路径作为cookie的路径，标记为HTTPOnly，并且如果HttpServletRequest.isSecure（）返回true，则该cookie将被标记为安全。
 * A {@link HttpSessionIdResolver} that uses a cookie to obtain the session from.
 * Specifically, this implementation will allow specifying a cookie serialization strategy
 * using {@link CookieHttpSessionIdResolver#setCookieSerializer(CookieSerializer)}. The
 * default is cookie name is "SESSION".
 *
 * When a session is created, the HTTP response will have a cookie with the specified
 * cookie name and the value of the session id. The cookie will be marked as a session
 * cookie, use the context path for the path of the cookie, marked as HTTPOnly, and if
 * {@link javax.servlet.http.HttpServletRequest#isSecure()} returns true, the cookie will
 * be marked as secure. For example:
 *
 * <pre>
 * HTTP/1.1 200 OK
 * Set-Cookie: SESSION=f81d4fae-7dec-11d0-a765-00a0c91e6bf6; Path=/context-root; Secure; HttpOnly
 * </pre>
 *
 * The client should now include the session in each request by specifying the same cookie
 * in their request. For example:
 *
 * <pre>
 * GET /messages/ HTTP/1.1
 * Host: example.com
 * Cookie: SESSION=f81d4fae-7dec-11d0-a765-00a0c91e6bf6
 * </pre>
 *
 * When the session is invalidated, the server will send an HTTP response that expires the
 * cookie. For example:
 *
 * <pre>
 * HTTP/1.1 200 OK
 * Set-Cookie: SESSION=f81d4fae-7dec-11d0-a765-00a0c91e6bf6; Expires=Thur, 1 Jan 1970 00:00:00 GMT; Secure; HttpOnly
 * </pre>
 *
 * @author Rob Winch
 * @author Vedran Pavic
 * @since 1.0
 */
public final class CookieHttpSessionIdResolver implements HttpSessionIdResolver {

	private static final String WRITTEN_SESSION_ID_ATTR = CookieHttpSessionIdResolver.class.getName()
			.concat(".WRITTEN_SESSION_ID_ATTR");

	private CookieSerializer cookieSerializer = new DefaultCookieSerializer();

	@Override
	public List<String> resolveSessionIds(HttpServletRequest request) {
		return this.cookieSerializer.readCookieValues(request);
	}

	@Override
	public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
		if (sessionId.equals(request.getAttribute(WRITTEN_SESSION_ID_ATTR))) {
			return;
		}
		request.setAttribute(WRITTEN_SESSION_ID_ATTR, sessionId);//说明：为啥要放到request.attribute
		this.cookieSerializer.writeCookieValue(new CookieValue(request, response, sessionId));
	}

	@Override
	public void expireSession(HttpServletRequest request, HttpServletResponse response) {
		this.cookieSerializer.writeCookieValue(new CookieValue(request, response, ""));
	}

	/**
	 * Sets the {@link CookieSerializer} to be used.
	 * @param cookieSerializer the cookieSerializer to set. Cannot be null.
	 */
	public void setCookieSerializer(CookieSerializer cookieSerializer) {
		if (cookieSerializer == null) {//说明：org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration.init
			throw new IllegalArgumentException("cookieSerializer cannot be null");
		}
		this.cookieSerializer = cookieSerializer;
	}

}
