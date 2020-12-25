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

package org.springframework.session.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

/**
 * For {@link SessionRepository} implementations that support it, this event is fired when
 * a {@link Session} is updated.
 *
 * @author Rob Winch
 * @since 1.1
 * 对于支持它的SessionRepository实现，更新Session时将触发此事件。
 */
@SuppressWarnings("serial")
public abstract class AbstractSessionEvent extends ApplicationEvent {

	private final String sessionId;

	private final Session session;

	AbstractSessionEvent(Object source, Session session) {
		super(source);
		this.session = session;
		this.sessionId = session.getId();
	}

	/**
	 * Gets the {@link Session} that was destroyed. For some {@link SessionRepository}
	 * implementations it may not be possible to get the original session in which case
	 * this may be null.
	 * @param <S> the type of Session
	 * @return the expired {@link Session} or null if the data store does not support
	 * obtaining it
	 * 获取被破坏的会话。 对于某些SessionRepository实现，可能无法获取原始会话，在这种情况下，该值为null。
	 *
	 * 类型参数：
	 * <S> –会话类型
	 * 返回值：
	 * 过期的会话；如果数据存储不支持获取它，则返回null
	 */
	@SuppressWarnings("unchecked")
	public <S extends Session> S getSession() {
		return (S) this.session;
	}

	public String getSessionId() {
		return this.sessionId;
	}

}
