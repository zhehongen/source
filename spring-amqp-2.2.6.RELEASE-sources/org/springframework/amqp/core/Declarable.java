/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.amqp.core;

import java.util.Collection;

import org.springframework.lang.Nullable;

/**
 * Classes implementing this interface can be auto-declared
 * with the broker during context initialization by an {@code AmqpAdmin}.
 * Registration can be limited to specific {@code AmqpAdmin}s.
 *实现此接口的类可以在AmqpAdmin进行上下文初始化期间与代理自动声明。 注册可以限于特定的AmqpAdmins。
 * @author Gary Russell
 * @since 1.2
 *
 */
public interface Declarable {

	/**
	 * Whether or not this object should be automatically declared此对象是否应由任何AmqpAdmin自动声明。
	 * by any {@code AmqpAdmin}.
	 * @return true if the object should be declared.返回值：如果应声明该对象，则为true。
	 */
	boolean shouldDeclare();

	/**
	 * The collection of {@code AmqpAdmin}s that should declare this
	 * object; if empty, all admins should declare.
	 * @return the collection.应该声明该对象的AmqpAdmins的集合； 如果为空，则所有管理员都应声明。
	 */
	Collection<?> getDeclaringAdmins();

	/**
	 * Should ignore exceptions (such as mismatched args) when declaring.
	 * @return true if should ignore.
	 * @since 1.6声明时应忽略异常（例如不匹配的args）。
	 */
	boolean isIgnoreDeclarationExceptions();

	/**
	 * Add an argument to the declarable.
	 * @param name the argument name.
	 * @param value the argument value.
	 * @since 2.2.2
	 */
	default void addArgument(String name, Object value) {
		// default no-op
	}

	/**
	 * Remove an argument from the declarable.
	 * @param name the argument name.
	 * @return the argument value or null if not present.
	 * @since 2.2.2
	 */
	default @Nullable Object removeArgument(String name) {
		return null;
	}

}
