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
package org.springframework.data.redis.connection.jedis;

import redis.clients.jedis.Builder;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.Queable;
import redis.clients.jedis.Response;
import redis.clients.jedis.util.SafeEncoder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.util.ReflectionUtils;

/**
 * Utility class to dispatch arbitrary Redis commands using Jedis commands.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Serhii Siryi
 * @since 2.1
 */
@SuppressWarnings({ "unchecked", "ConstantConditions" })
class JedisClientUtils {

	private static final Method GET_RESPONSE;
	private static final Set<String> KNOWN_COMMANDS;
	private static final Builder<Object> OBJECT_BUILDER;

	static {

		GET_RESPONSE = ReflectionUtils.findMethod(Queable.class, "getResponse", Builder.class);
		ReflectionUtils.makeAccessible(GET_RESPONSE);

		KNOWN_COMMANDS = Arrays.stream(Command.values()).map(Enum::name).collect(Collectors.toSet());

		OBJECT_BUILDER = new Builder<Object>() {
			public Object build(Object data) {
				return data;
			}

			public String toString() {
				return "Object";
			}
		};
	}

	/**
	 * Execute an arbitrary on the supplied {@link Jedis} instance.
	 *
	 * @param command the command.
	 * @param keys must not be {@literal null}, may be empty.
	 * @param args must not be {@literal null}, may be empty.
	 * @param jedis must not be {@literal null}.
	 * @return the response, can be be {@literal null}.
	 */
	static <T> T execute(String command, byte[][] keys, byte[][] args, Supplier<Jedis> jedis) {
		return execute(command, keys, args, jedis, it -> (T) it.getOne());
	}

	/**
	 * Execute an arbitrary on the supplied {@link Jedis} instance.
	 *
	 * @param command the command.
	 * @param keys must not be {@literal null}, may be empty.
	 * @param args must not be {@literal null}, may be empty.
	 * @param jedis must not be {@literal null}.
	 * @param responseMapper must not be {@literal null}.
	 * @return the response, can be be {@literal null}.
	 * @since 2.1
	 */
	static <T> T execute(String command, byte[][] keys, byte[][] args, Supplier<Jedis> jedis,
			Function<Client, T> responseMapper) {

		byte[][] commandArgs = getCommandArguments(keys, args);

		Client client = sendCommand(command, commandArgs, jedis.get());

		return responseMapper.apply(client);
	}

	/**
	 * Send a Redis command and retrieve the {@link Client} for response retrieval.
	 *
	 * @param command the command.
	 * @param args must not be {@literal null}, may be empty.
	 * @param jedis must not be {@literal null}.
	 * @return the {@link Client} instance used to send the command.
	 */
	static Client sendCommand(String command, byte[][] args, Jedis jedis) {

		Client client = jedis.getClient();

		sendCommand(client, command, args);

		return client;
	}

	private static void sendCommand(Client client, String command, byte[][] args) {

		if (isKnownCommand(command)) {
			client.sendCommand(Command.valueOf(command.trim().toUpperCase()), args);
		} else {
			client.sendCommand(() -> SafeEncoder.encode(command.trim().toUpperCase()), args);
		}
	}

	private static boolean isKnownCommand(String command) {
		return KNOWN_COMMANDS.contains(command);
	}

	private static byte[][] getCommandArguments(byte[][] keys, byte[][] args) {

		if (keys.length == 0) {
			return args;
		}

		if (args.length == 0) {
			return keys;
		}

		byte[][] commandArgs = new byte[keys.length + args.length][];

		System.arraycopy(keys, 0, commandArgs, 0, keys.length);
		System.arraycopy(args, 0, commandArgs, keys.length, args.length);

		return commandArgs;
	}

	/**
	 * @param jedis the client instance.
	 * @return {@literal true} if the connection has entered {@literal MULTI} state.
	 */
	static boolean isInMulti(Jedis jedis) {
		return jedis.getClient().isInMulti();
	}

	/**
	 * Retrieve the {@link Response} object from a {@link redis.clients.jedis.Transaction} or a
	 * {@link redis.clients.jedis.Pipeline} for response synchronization.
	 *
	 * @param target a {@link redis.clients.jedis.Transaction} or {@link redis.clients.jedis.Pipeline}, must not be
	 *          {@literal null}.
	 * @return the {@link Response} wrapper object.
	 */
	static Response<Object> getResponse(Object target) {
		return (Response<Object>) ReflectionUtils.invokeMethod(GET_RESPONSE, target, OBJECT_BUILDER);
	}

}
