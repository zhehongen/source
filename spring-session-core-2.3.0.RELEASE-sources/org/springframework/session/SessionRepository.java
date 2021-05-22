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

/**
 * A repository interface for managing {@link Session} instances.
 * 用于管理会话实例的存储库接口。
 * @param <S> the {@link Session} type
 * @author Rob Winch
 * @since 1.0
 */
public interface SessionRepository<S extends Session> {

	/**创建一个新会话，该会话能够被此SessionRepository持久化。
	 * Creates a new {@link Session} that is capable of being persisted by this
	 * {@link SessionRepository}.
	 *这允许优化和自定义会话的持久化方式。例如，返回的实现可能会跟踪更改，以确保在保存时只需要保留增量。
	 * <p>
	 * This allows optimizations and customizations in how the {@link Session} is
	 * persisted. For example, the implementation returned might keep track of the changes
	 * ensuring that only the delta needs to be persisted on a save.
	 * </p>
	 * @return a new {@link Session} that is capable of being persisted by this
	 * {@link SessionRepository}
	 */
	S createSession();

	/**确保已保存由createSession（）创建的Session。某些实现可能选择保存，因为在Session更新时，它会返回一个可以立即保留所有更改的Session。 在这种情况下，此方法可能实际上不执行任何操作。
	 * Ensures the {@link Session} created by
	 * {@link org.springframework.session.SessionRepository#createSession()} is saved.
	 *
	 * <p>
	 * Some implementations may choose to save as the {@link Session} is updated by
	 * returning a {@link Session} that immediately persists any changes. In this case,
	 * this method may not actually do anything.
	 * </p>
	 * @param session the {@link Session} to save
	 */
	void save(S session);

	/**
	 * Gets the {@link Session} by the {@link Session#getId()} or null if no
	 * {@link Session} is found.
	 * @param id the {@link org.springframework.session.Session#getId()} to lookup
	 * @return the {@link Session} by the {@link Session#getId()} or null if no
	 * {@link Session} is found.
	 */
	S findById(String id);

	/**
	 * Deletes the {@link Session} with the given {@link Session#getId()} or does nothing
	 * if the {@link Session} is not found.
	 * @param id the {@link org.springframework.session.Session#getId()} to delete
	 */
	void deleteById(String id);

}
