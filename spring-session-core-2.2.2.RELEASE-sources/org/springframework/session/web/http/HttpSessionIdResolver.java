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

/**
 * Contract for session id resolution strategies. Allows for session id resolution through
 * the request and for sending the session id or expiring the session through the
 * response.
 *
 * @author Rob Winch
 * @author Vedran Pavic
 * @since 2.0.0
 * 会话ID解析策略合同。 允许通过请求解析会话ID，并允许通过响应发送会话ID或使会话过期。
 */
public interface HttpSessionIdResolver {

	/**
	 * Resolve the session ids associated with the provided {@link HttpServletRequest}.
	 * For example, the session id might come from a cookie or a request header.
	 * @param request the current request
	 * @return the session ids
	 * 解决与提供的HttpServletRequest关联的会话ID。 例如，会话ID可能来自Cookie或请求标头。
	 *
	 * 参数：
	 * 请求–当前请求
	 * 返回值：
	 * 会话ID
	 */
	List<String> resolveSessionIds(HttpServletRequest request);

	/**
	 * Send the given session id to the client. This method is invoked when a new session
	 * is created and should inform a client what the new session id is. For example, it
	 * might create a new cookie with the session id in it or set an HTTP response header
	 * with the value of the new session id.
	 * @param request the current request
	 * @param response the current response
	 * @param sessionId the session id
	 *  将给定的会话ID发送给客户端。 创建新会话时将调用此方法，并且应通知客户端新会话ID是什么。
	 * 例如，它可能会在其中创建带有会话ID的新Cookie，或者使用新会话ID的值设置HTTP响应标头。
	 *
	 * 参数：
	 * 请求–当前请求
	 * 响应–当前响应
	 * sessionId –会话ID
	 */
	void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId);

	/**
	 * Instruct the client to end the current session. This method is invoked when a
	 * session is invalidated and should inform a client that the session id is no longer
	 * valid. For example, it might remove a cookie with the session id in it or set an
	 * HTTP response header with an empty value indicating to the client to no longer
	 * submit that session id.
	 * @param request the current request
	 * @param response the current response
	 *指示客户端结束当前会话。 当会话无效时，将调用此方法，并且应通知客户端该会话ID不再有效。 例如，它可能会删除其中包含会话ID的cookie，或者将HTTP响应标头设置为空值，以指示客户端不再提交该会话ID。
	 *
	 * 参数：
	 * 请求–当前请求
	 * 响应–当前响应
	 */
	void expireSession(HttpServletRequest request, HttpServletResponse response);

}
