/*
 * Copyright 2016-2020 the original author or authors.
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

import reactor.core.publisher.Mono;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
public interface ReactiveRedisClusterConnection extends ReactiveRedisConnection {

	@Override
	ReactiveClusterKeyCommands keyCommands();

	@Override
	ReactiveClusterStringCommands stringCommands();

	@Override
	ReactiveClusterNumberCommands numberCommands();

	@Override
	ReactiveClusterListCommands listCommands();

	@Override
	ReactiveClusterSetCommands setCommands();

	@Override
	ReactiveClusterZSetCommands zSetCommands();

	@Override
	ReactiveClusterHashCommands hashCommands();

	@Override
	ReactiveClusterGeoCommands geoCommands();

	@Override
	ReactiveClusterHyperLogLogCommands hyperLogLogCommands();

	@Override
	ReactiveClusterServerCommands serverCommands();

	@Override
	ReactiveClusterStreamCommands streamCommands();

	/**
	 * Test the connection to a specific Redis cluster node.
	 *
	 * @param node must not be {@literal null}.
	 * @return {@link Mono} wrapping server response message - usually {@literal PONG}.
	 * @throws IllegalArgumentException when {@code node} is {@literal null}.
	 * @see RedisConnectionCommands#ping()
	 */
	Mono<String> ping(RedisClusterNode node);
}
