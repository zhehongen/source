/*
 * Copyright 2002-2016 the original author or authors.
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
package org.springframework.security.core;

/**
 * Indicates that the implementing object contains sensitive data, which can be erased
 * using the {@code eraseCredentials} method. Implementations are expected to invoke the
 * method on any internal objects which may also implement this interface.
 * <p>
 * For internal framework use only. Users who are writing their own
 * {@code AuthenticationProvider} implementations should create and return an appropriate
 * {@code Authentication} object there, minus any sensitive data, rather than using this
 * interface.
 *
 * @author Luke Taylor
 * @since 3.0.3
 * 指示实现对象包含敏感数据，可以使用deleteCredentials方法将其删除。
 * 期望实现可以在也可以实现此接口的任何内部对象上调用该方法。
 * 仅用于内部框架。
 * 编写自己的AuthenticationProvider实现的用户应在此处创建
 * 并返回适当的Authentication对象，减去任何敏感数据，
 * 而不要使用此接口。
 */
public interface CredentialsContainer {
	void eraseCredentials();
}
