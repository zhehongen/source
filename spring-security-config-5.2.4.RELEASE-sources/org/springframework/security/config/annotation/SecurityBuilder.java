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
 * Interface for building an Object
 *
 * @author Rob Winch
 * @since 3.2
 *
 * @param <O> The type of the Object being built
 * 构建一个对象的接口
 * 类型参数：
 * <O> –正在构建的对象的类型
 *  一般是用来构建一个过滤器吗？
 */
public interface SecurityBuilder<O> {

	/**
	 * Builds the object and returns it or null.
	 *
	 * @return the Object to be built or null if the implementation allows it.
	 * @throws Exception if an error occurred when building the Object
	 * 生成对象并返回它或为null。
	 *
	 * 返回值：
	 * 要构建的对象；如果实现允许，则为null。
	 * 抛出：
	 * 例外–如果在构建对象时发生错误
	 */
	O build() throws Exception;
}
