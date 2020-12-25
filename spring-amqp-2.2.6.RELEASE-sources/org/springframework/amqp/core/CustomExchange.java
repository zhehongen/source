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

/**
 * Simple container collecting information to describe a custom exchange. Custom exchange types are allowed by the AMQP
 * specification, and their names should start with "x-" (but this is not enforced here). Used in conjunction with
 * administrative operations.简单的容器用来收集信息以描述自定义交换。 AMQP规范允许自定义交换类型，其名称应以“ x-”开头（但此处未强制执行）。 与管理操作结合使用。
 * @author Dave Syer
 * @see AmqpAdmin
 */
public class CustomExchange extends AbstractExchange {

	private final String type;

	public CustomExchange(String name, String type) {
		super(name);
		this.type = type;
	}

	public CustomExchange(String name, String type, boolean durable, boolean autoDelete) {
		super(name, durable, autoDelete);
		this.type = type;
	}

	public CustomExchange(String name, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
		super(name, durable, autoDelete, arguments);
		this.type = type;
	}

	@Override
	public final String getType() {
		return this.type;
	}

}
