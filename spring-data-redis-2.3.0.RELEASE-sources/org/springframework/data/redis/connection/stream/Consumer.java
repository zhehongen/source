/*
 * Copyright 2018-2020 the original author or authors.
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
package org.springframework.data.redis.connection.stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.util.Assert;

/**
 * Value object representing a Stream consumer within a consumer group. Group name and consumer name are encoded as
 * keys.
 * 
 * @author Mark Paluch
 * @see 2.2
 */
@EqualsAndHashCode
@Getter
public class Consumer {

	private final String group;
	private final String name;

	private Consumer(String group, String name) {
		this.group = group;
		this.name = name;
	}

	/**
	 * Create a new consumer.
	 *
	 * @param group name of the consumer group, must not be {@literal null} or empty.
	 * @param name name of the consumer, must not be {@literal null} or empty.
	 * @return the consumer {@link io.lettuce.core.Consumer} object.
	 */
	public static Consumer from(String group, String name) {

		Assert.hasText(group, "Group must not be null");
		Assert.hasText(name, "Name must not be null");

		return new Consumer(group, name);
	}

	@Override
	public String toString() {
		return String.format("%s:%s", group, name);
	}
}
