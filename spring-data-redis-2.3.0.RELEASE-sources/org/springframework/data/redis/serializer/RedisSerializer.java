/*
 * Copyright 2011-2020 the original author or authors.
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
package org.springframework.data.redis.serializer;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**对象的基本接口序列化和反序列化为字节数组（二进制数据）。 建议将实现设计为在序列化和反序列化方面处理空对象/空数组。 请注意，Redis不接受空键或值，但可以返回空回复（对于不存在的键）。
 * Basic interface serialization and deserialization of Objects to byte arrays (binary data). It is recommended that
 * implementations are designed to handle null objects/empty arrays on serialization and deserialization side. Note that
 * Redis does not accept null keys or values but can return null replies (for non existing keys).
 *
 * @author Mark Pollack
 * @author Costin Leau
 * @author Christoph Strobl
 */
public interface RedisSerializer<T> {

	/**
	 * Serialize the given object to binary data.
	 *
	 * @param t object to serialize. Can be {@literal null}.
	 * @return the equivalent binary data. Can be {@literal null}.
	 */
	@Nullable
	byte[] serialize(@Nullable T t) throws SerializationException;

	/**
	 * Deserialize an object from the given binary data.
	 *
	 * @param bytes object binary representation. Can be {@literal null}.
	 * @return the equivalent object instance. Can be {@literal null}.
	 */
	@Nullable
	T deserialize(@Nullable byte[] bytes) throws SerializationException;

	/**
	 * Obtain a {@link RedisSerializer} using java serialization.<br />
	 * <strong>Note:</strong> Ensure that your domain objects are actually {@link java.io.Serializable serializable}.
	 *
	 * @return never {@literal null}.
	 * @since 2.1
	 */
	static RedisSerializer<Object> java() {
		return java(null);
	}

	/**
	 * Obtain a {@link RedisSerializer} using java serialization with the given {@link ClassLoader}.<br />
	 * <strong>Note:</strong> Ensure that your domain objects are actually {@link java.io.Serializable serializable}.
	 *
	 * @param classLoader the {@link ClassLoader} to use for deserialization. Can be {@literal null}.
	 * @return new instance of {@link RedisSerializer}. Never {@literal null}.
	 * @since 2.1
	 */
	static RedisSerializer<Object> java(@Nullable ClassLoader classLoader) {
		return new JdkSerializationRedisSerializer(classLoader);
	}

	/**
	 * Obtain a {@link RedisSerializer} that can read and write JSON using
	 * <a href="https://github.com/FasterXML/jackson-core">Jackson</a>.
	 *
	 * @return never {@literal null}.
	 * @since 2.1
	 */
	static RedisSerializer<Object> json() {
		return new GenericJackson2JsonRedisSerializer();
	}

	/**
	 * Obtain a simple {@link java.lang.String} to {@literal byte[]} (and back) serializer using
	 * {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8} as the default {@link java.nio.charset.Charset}.
	 *
	 * @return never {@literal null}.
	 * @since 2.1
	 */
	static RedisSerializer<String> string() {
		return StringRedisSerializer.UTF_8;
	}

	/**
	 * Obtain a {@link RedisSerializer} that passes thru {@code byte[]}.
	 *
	 * @return never {@literal null}.
	 * @since 2.2
	 */
	static RedisSerializer<byte[]> byteArray() {
		return ByteArrayRedisSerializer.INSTANCE;
	}

	default boolean canSerialize(Class<?> type) {
		return ClassUtils.isAssignable(getTargetType(), type);
	}

	default Class<?> getTargetType() {
		return Object.class;
	}
}
