/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.data.redis.connection;

import java.util.List;
import java.util.Properties;

import org.springframework.data.redis.core.types.RedisClientInfo;

/**
 * @author Mark Paluch
 * @since 2.0
 */
public interface RedisClusterServerCommands extends RedisServerCommands {

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#bgReWriteAof()
	 */
	void bgReWriteAof(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#bgSave()
	 */
	void bgSave(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @return
	 * @see RedisServerCommands#lastSave()
	 */
	Long lastSave(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#save()
	 */
	void save(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @return
	 * @see RedisServerCommands#dbSize()
	 */
	Long dbSize(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#flushDb()
	 */
	void flushDb(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#flushAll()
	 */
	void flushAll(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @return
	 * @see RedisServerCommands#info()
	 */
	Properties info(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @param section
	 * @return
	 * @see RedisServerCommands#info(String)
	 */
	Properties info(RedisClusterNode node, String section);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#shutdown()
	 */
	void shutdown(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @param pattern
	 * @return
	 * @see RedisServerCommands#getConfig(String)
	 */
	Properties getConfig(RedisClusterNode node, String pattern);

	/**
	 * @param node must not be {@literal null}.
	 * @param param
	 * @param value
	 * @see RedisServerCommands#setConfig(String, String)
	 */
	void setConfig(RedisClusterNode node, String param, String value);

	/**
	 * @param node must not be {@literal null}.
	 * @see RedisServerCommands#resetConfigStats()
	 */
	void resetConfigStats(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @return
	 * @see RedisServerCommands#time()
	 */
	Long time(RedisClusterNode node);

	/**
	 * @param node must not be {@literal null}.
	 * @return
	 * @see RedisServerCommands#getClientList()
	 */
	List<RedisClientInfo> getClientList(RedisClusterNode node);
}
