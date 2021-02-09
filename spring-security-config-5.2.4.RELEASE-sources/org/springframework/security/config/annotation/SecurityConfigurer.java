/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.security.config.annotation;

/**
 * Allows for configuring a {@link SecurityBuilder}. All {@link SecurityConfigurer} first
 * have their {@link #init(SecurityBuilder)} method invoked. After all
 * {@link #init(SecurityBuilder)} methods have been invoked, each
 * {@link #configure(SecurityBuilder)} method is invoked.
 * 允许配置SecurityBuilder。 首先，所有SecurityConfigurer都会调用其init（SecurityBuilder）方法。 调用所有init（SecurityBuilder）方法之后，将调用每个configure（SecurityBuilder）方法。
 * @see AbstractConfiguredSecurityBuilder
 *
 * @author Rob Winch
 *
 * @param <O> The object being built by the {@link SecurityBuilder} B     SecurityBuilder B正在构建的对象
 * @param <B> The {@link SecurityBuilder} that builds objects of type O. This is also the
 * {@link SecurityBuilder} that is being configured. 构建类型为O的对象的SecurityBuilder。这也是正在配置的SecurityBuilder。
 */
public interface SecurityConfigurer<O, B extends SecurityBuilder<O>> {
	/**
	 * Initialize the {@link SecurityBuilder}. Here only shared state should be created
	 * and modified, but not properties on the {@link SecurityBuilder} used for building
	 * the object. This ensures that the {@link #configure(SecurityBuilder)} method uses
	 * the correct shared objects when building. Configurers should be applied here.
	 * 初始化SecurityBuilder。 在这里，仅应创建和修改共享状态，而不能在用于构建对象的SecurityBuilder上创建和修改属性。 这样可以确保在构建时configure（SecurityBuilder）方法使用正确的共享库。 配置器应在此处应用。
	 * @param builder
	 * @throws Exception
	 */
	void init(B builder) throws Exception;

	/**
	 * Configure the {@link SecurityBuilder} by setting the necessary properties on the
	 * {@link SecurityBuilder}.
	 * 通过在SecurityBuilder上设置必要的属性来配置SecurityBuilder。
	 * @param builder
	 * @throws Exception
	 */
	void configure(B builder) throws Exception;
}
