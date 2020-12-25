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
package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Defines a provider for Lettuce connections.
 * <p />
 * This interface is typically used to encapsulate a native factory which returns a {@link StatefulConnection
 * connection} of on each invocation.
 * <p/>
 * Connection providers may create a new connection on each invocation or return pooled instances. Each obtained
 * connection must be released through its connection provider to allow disposal or release back to the pool.
 * <p/>
 * Connection providers are usually associated with a {@link io.lettuce.core.codec.RedisCodec} to create connections
 * with an appropriate codec.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 * @see StatefulConnection
 */
@FunctionalInterface
public interface LettuceConnectionProvider {

	/**
	 * Request a connection given {@code connectionType}. Providing a connection type allows specialization to provide a
	 * more specific connection type.
	 *
	 * @param connectionType must not be {@literal null}.
	 * @return the requested connection. Must be {@link #release(StatefulConnection) released} if the connection is no
	 *         longer in use.
	 */
	default <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {
		return LettuceFutureUtils.join(getConnectionAsync(connectionType));
	}

	/**
	 * Request asynchronously a connection given {@code connectionType}. Providing a connection type allows specialization
	 * to provide a more specific connection type.
	 *
	 * @param connectionType must not be {@literal null}.
	 * @return a {@link CompletionStage} that is notified with the connection progress. Must be
	 *         {@link #releaseAsync(StatefulConnection) released} if the connection is no longer in use.
	 * @since 2.2
	 */
	<T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType);

	/**
	 * Release the {@link StatefulConnection connection}. Closes connection {@link StatefulConnection#close()} by default.
	 * Implementations may choose whether they override this method and return the connection to a pool.
	 *
	 * @param connection must not be {@literal null}.
	 */
	default void release(StatefulConnection<?, ?> connection) {
		LettuceFutureUtils.join(releaseAsync(connection));
	}

	/**
	 * Release asynchronously the {@link StatefulConnection connection}. Closes connection
	 * {@link StatefulConnection#closeAsync()} by default. Implementations may choose whether they override this method
	 * and return the connection to a pool.
	 *
	 * @param connection must not be {@literal null}.
	 * @return Close {@link CompletableFuture future} notified once the connection is released.
	 * @since 2.2
	 */
	default CompletableFuture<Void> releaseAsync(StatefulConnection<?, ?> connection) {
		return connection.closeAsync();
	}

	/**
	 * Extension to {@link LettuceConnectionProvider} for providers that allow connection creation to specific nodes.
	 */
	@FunctionalInterface
	interface TargetAware {

		/**
		 * Request a connection given {@code connectionType} for a specific {@link RedisURI}. Providing a connection type
		 * allows specialization to provide a more specific connection type.
		 *
		 * @param connectionType must not be {@literal null}.
		 * @param redisURI must not be {@literal null}.
		 * @return the requested connection.
		 */
		default <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType, RedisURI redisURI) {
			return LettuceFutureUtils.join(getConnectionAsync(connectionType, redisURI));
		}

		/**
		 * Request asynchronously a connection given {@code connectionType} for a specific {@link RedisURI}. Providing a
		 * connection type allows specialization to provide a more specific connection type.
		 *
		 * @param connectionType must not be {@literal null}.
		 * @param redisURI must not be {@literal null}.
		 * @return a {@link CompletionStage} that is notified with the connection progress.
		 * @since 2.2
		 */
		<T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType,
				RedisURI redisURI);
	}
}
