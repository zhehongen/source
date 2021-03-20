/*
 * Copyright 2014-2017 the original author or authors.
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

import org.springframework.session.Session;

/**
 * Base class for events fired when a {@link Session} is destroyed explicitly.
 *
 * @author Rob Winch
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SessionDestroyedEvent extends AbstractSessionEvent {

	/**
	 * Create a new {@link SessionDestroyedEvent}.
	 * @param source the source of the event
	 * @param session the session that was created
	 */
	public SessionDestroyedEvent(Object source, Session session) {
		super(source, session);
	}

}
