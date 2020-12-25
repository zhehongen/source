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

import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * Simple container collecting information to describe a binding. Takes String destination and exchange names as
 * arguments to facilitate wiring using code based configuration. Can be used in conjunction with {@link AmqpAdmin}, or
 * created via a {@link BindingBuilder}.
 * 简单的容器用来收集描述绑定的信息。 将String目标和交换名称作为参数，以方便使用基于代码的配置进行接线。 可以与AmqpAdmin结合使用，或通过BindingBuilder创建。
 * @author Mark Pollack
 * @author Mark Fisher
 * @author Dave Syer
 * @author Gary Russell
 *
 * @see AmqpAdmin
 */
public class Binding extends AbstractDeclarable {

	/**
	 * The binding destination.
	 */
	public enum DestinationType {

		/**
		 * Queue destination.
		 */
		QUEUE,

		/**
		 * Exchange destination.
		 */
		EXCHANGE;
	}

	private final String destination;

	private final String exchange;

	private final String routingKey;

	private final DestinationType destinationType;

	public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
			@Nullable Map<String, Object> arguments) {

		super(arguments);
		this.destination = destination;
		this.destinationType = destinationType;
		this.exchange = exchange;
		this.routingKey = routingKey;
	}

	public String getDestination() {
		return this.destination;
	}

	public DestinationType getDestinationType() {
		return this.destinationType;
	}

	public String getExchange() {
		return this.exchange;
	}

	public String getRoutingKey() {
		return this.routingKey;
	}

	public boolean isDestinationQueue() {
		return DestinationType.QUEUE.equals(this.destinationType);
	}

	@Override
	public String toString() {
		return "Binding [destination=" + this.destination + ", exchange=" + this.exchange + ", routingKey="
					+ this.routingKey + ", arguments=" + getArguments() + "]";
	}

}
