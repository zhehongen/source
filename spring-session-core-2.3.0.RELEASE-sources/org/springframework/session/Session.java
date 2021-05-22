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

package org.springframework.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Provides a way to identify a user in an agnostic way. This allows the session to be
 * used by an HttpSession, WebSocket Session, or even non web related sessions.
 * 提供一种以不可知的方式标识用户的方法。这允许HttpSession、WebSocket会话甚至非web相关会话使用会话。
 * @author Rob Winch
 * @author Vedran Pavic
 * @since 1.0
 */
public interface Session {

	/**获取标识会话的唯一字符串。
	 * Gets a unique string that identifies the {@link Session}.
	 * @return a unique string that identifies the {@link Session}标识会话的唯一字符串
	 */
	String getId();

	/**更改会话id。调用getId（）后将返回一个新标识符。
	 * Changes the session id. After invoking the {@link #getId()} will return a new
	 * identifier.
	 * @return the new session id which {@link #getId()} will now return
	 */
	String changeSessionId();

	/**获取与指定名称关联的对象，如果没有对象与该名称关联，则为null。
	 * Gets the Object associated with the specified name or null if no Object is
	 * associated to that name.
	 * @param <T> the return type of the attribute
	 * @param attributeName the name of the attribute to get
	 * @return the Object associated with the specified name or null if no Object is
	 * associated to that name
	 */
	<T> T getAttribute(String attributeName);//看过了

	/**
	 * Return the session attribute value or if not present raise an
	 * {@link IllegalArgumentException}.
	 * @param name the attribute name
	 * @param <T> the attribute type
	 * @return the attribute value
	 */
	@SuppressWarnings("unchecked")
	default <T> T getRequiredAttribute(String name) {
		T result = getAttribute(name);
		if (result == null) {
			throw new IllegalArgumentException("Required attribute '" + name + "' is missing.");
		}
		return result;
	}

	/**
	 * Return the session attribute value, or a default, fallback value.
	 * @param name the attribute name
	 * @param defaultValue a default value to return instead
	 * @param <T> the attribute type
	 * @return the attribute value
	 */
	@SuppressWarnings("unchecked")
	default <T> T getAttributeOrDefault(String name, T defaultValue) {//看过了
		T result = getAttribute(name);
		return (result != null) ? result : defaultValue;
	}

	/**获取具有关联值的属性名称。可以将每个值传递到getAttribute（String）中以获取属性值。
	 * Gets the attribute names that have a value associated with it. Each value can be
	 * passed into {@link org.springframework.session.Session#getAttribute(String)} to
	 * obtain the attribute value.
	 * @return the attribute names that have a value associated with it.
	 * @see #getAttribute(String)
	 */
	Set<String> getAttributeNames();

	/**为提供的属性名设置属性值。如果attributeValue为null，则其结果与使用removeAttribute（String）删除属性相同。
	 * Sets the attribute value for the provided attribute name. If the attributeValue is
	 * null, it has the same result as removing the attribute with
	 * {@link org.springframework.session.Session#removeAttribute(String)} .
	 * @param attributeName the attribute name to set
	 * @param attributeValue the value of the attribute to set. If null, the attribute
	 * will be removed.
	 */
	void setAttribute(String attributeName, Object attributeValue);//看过了

	/**删除具有提供的属性名称的属性。
	 * Removes the attribute with the provided attribute name.
	 * @param attributeName the name of the attribute to remove
	 */
	void removeAttribute(String attributeName);

	/**
	 * Gets the time when this session was created.
	 * @return the time when this session was created.
	 */
	Instant getCreationTime();//说明：固有属性

	/**
	 * Sets the last accessed time.
	 * @param lastAccessedTime the last accessed time
	 */
	void setLastAccessedTime(Instant lastAccessedTime);

	/**
	 * Gets the last time this {@link Session} was accessed.
	 * @return the last time the client sent a request associated with the session
	 */
	Instant getLastAccessedTime();//说明：固有属性

	/**设置此会话将无效之前请求之间的最大非活动间隔。负时间表示会话永远不会超时。
	 * Sets the maximum inactive interval between requests before this session will be
	 * invalidated. A negative time indicates that the session will never timeout.
	 * @param interval the amount of time that the {@link Session} should be kept alive
	 * between client requests.在客户端请求之间会话应保持活动状态的时间量。
	 */
	void setMaxInactiveInterval(Duration interval);//说明：固有属性

	/**获取此会话将无效之前请求之间的最大非活动间隔。负时间表示会话永远不会超时。
	 * Gets the maximum inactive interval between requests before this session will be
	 * invalidated. A negative time indicates that the session will never timeout.
	 * @return the maximum inactive interval between requests before this session will be
	 * invalidated. A negative time indicates that the session will never timeout.
	 */
	Duration getMaxInactiveInterval();

	/**
	 * Returns true if the session is expired.
	 * @return true if the session is expired, else false.
	 */
	boolean isExpired();

}
