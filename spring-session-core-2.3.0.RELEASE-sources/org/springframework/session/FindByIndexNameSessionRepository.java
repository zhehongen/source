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

import java.util.Map;

/**
 * Extends a basic {@link SessionRepository} to allow finding sessions by the specified
 * index name and index value.
 * 扩展基本SessionRepository以允许按指定的索引名和索引值查找会话。
 * @param <S> the type of Session being managed by this
 * {@link FindByIndexNameSessionRepository}此FindByIndexNameSessionRepository管理的会话类型
 * @author Rob Winch
 * @author Vedran Pavic
 */
public interface FindByIndexNameSessionRepository<S extends Session> extends SessionRepository<S> {

	/**包含当前主体名称（即用户名）的会话索引。开发者有责任确保填充索引，因为Spring Session不知道所使用的身份验证机制
	 * A session index that contains the current principal name (i.e. username).
	 * <p>
	 * It is the responsibility of the developer to ensure the index is populated since
	 * Spring Session is not aware of the authentication mechanism being used.
	 *
	 * @since 1.1
	 */
	String PRINCIPAL_NAME_INDEX_NAME = FindByIndexNameSessionRepository.class.getName()//org.springframework.session.FindByIndexNameSessionRepository
			.concat(".PRINCIPAL_NAME_INDEX_NAME");//看过了

	/**
	 * Find a {@link Map} of the session id to the {@link Session} of all sessions that
	 * contain the specified index name index value.
	 * @param indexName the name of the index (i.e.
	 * {@link FindByIndexNameSessionRepository#PRINCIPAL_NAME_INDEX_NAME})
	 * @param indexValue the value of the index to search for.
	 * @return a {@code Map} (never {@code null}) of the session id to the {@code Session}
	 * of all sessions that contain the specified index name and index value. If no
	 * results are found, an empty {@code Map} is returned.
	 */
	Map<String, S> findByIndexNameAndIndexValue(String indexName, String indexValue);

	/**查找会话ID到所有包含名称为PRINCIPAL_NAME_INDEX_NAME的索引和指定的主体名称的所有会话的Session的映射。
	 * Find a {@link Map} of the session id to the {@link Session} of all sessions that
	 * contain the index with the name
	 * {@link FindByIndexNameSessionRepository#PRINCIPAL_NAME_INDEX_NAME} and the
	 * specified principal name.
	 * @param principalName the principal name
	 * @return a {@code Map} (never {@code null}) of the session id to the {@code Session}
	 * of all sessions that contain the specified principal name. If no results are found,
	 * an empty {@code Map} is returned.会话ID到包含指定主体名称的所有会话的Session的映射（绝不为null）。 如果未找到结果，则返回一个空的Map。
	 * @since 2.1.0
	 */
	default Map<String, S> findByPrincipalName(String principalName) {
		//说明：一个用户有多个session。sessionid->session的map
		return findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, principalName);

	}

}
