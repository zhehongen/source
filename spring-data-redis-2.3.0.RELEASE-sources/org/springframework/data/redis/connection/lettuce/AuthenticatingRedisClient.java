/*
 * Copyright 2013-2020 the original author or authors.
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
package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * Extension of {@link RedisClient} that calls auth on all new connections using the supplied credentials
 *
 * @author Jennifer Hickey
 * @author Mark Paluch
 * @author Christoph Strobl
 * @deprecated since 1.6 - Please use {@link RedisURI#setPassword(String)}
 */
@Deprecated
public class AuthenticatingRedisClient extends RedisClient {

	public AuthenticatingRedisClient(String host, int port, String password) {
		super(null, RedisURI.builder().withHost(host).withPort(port).withPassword(password).build());
	}

	public AuthenticatingRedisClient(String host, String password) {
		super(null, RedisURI.builder().withHost(host).withPassword(password).build());
	}

	/*
	 * (non-Javadoc)
	 * @see io.lettuce.core.RedisClient#connect(io.lettuce.core.codec.RedisCodec)
	 */
	@Override
	public <K, V> StatefulRedisConnection<K, V> connect(RedisCodec<K, V> codec) {
		return super.connect(codec);
	}

	/*
	 * (non-Javadoc)
	 * @see io.lettuce.core.RedisClient#connectPubSub(io.lettuce.core.codec.RedisCodec)
	 */
	@Override
	public <K, V> StatefulRedisPubSubConnection<K, V> connectPubSub(RedisCodec<K, V> codec) {
		return super.connectPubSub(codec);
	}
}
