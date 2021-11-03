/*
 * Copyright 2020 the original author or authors.
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

package org.springframework.amqp.rabbit.listener.adapter;

import java.util.function.BiFunction;

import org.springframework.amqp.core.Message;

/**
 * A post processor for replies. The first parameter to the function is the request
 * message, the second is the response message; it must return the modified (or a new)
 * message. Use this, for example, if you want to copy additional headers from the request
 * message.
 *
 * @author Gary Russell
 * @since 2.2.5
 *
 */
public interface ReplyPostProcessor extends BiFunction<Message, Message, Message> {

}
