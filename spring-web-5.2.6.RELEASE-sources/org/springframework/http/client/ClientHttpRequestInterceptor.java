/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.http.client;

import java.io.IOException;

import org.springframework.http.HttpRequest;

/**
 * Intercepts client-side HTTP requests. Implementations of this interface can be
 * {@linkplain org.springframework.web.client.RestTemplate#setInterceptors registered}
 * with the {@link org.springframework.web.client.RestTemplate RestTemplate},
 * as to modify the outgoing {@link ClientHttpRequest} and/or the incoming
 * {@link ClientHttpResponse}.
 * 拦截客户端HTTP请求。 可以向RestTemplate注册此接口的实现，以修改传出的ClientHttpRequest和/或传入的ClientHttpResponse。
 * <p>The main entry point for interceptors is
 * {@link #intercept(HttpRequest, byte[], ClientHttpRequestExecution)}.
 * 拦截器的主要入口点是intercept（HttpRequest，byte []，ClientHttpRequestExecution）。
 * @author Arjen Poutsma
 * @since 3.1
 */
@FunctionalInterface
public interface ClientHttpRequestInterceptor {

	/**拦截给定的请求，并返回响应。给定的ClientHttpRequestExecution允许拦截器将请求和响应传递给链中的下一个实体。
	 * Intercept the given request, and return a response. The given
	 * {@link ClientHttpRequestExecution} allows the interceptor to pass on the
	 * request and response to the next entity in the chain.
	 * <p>A typical implementation of this method would follow the following pattern:
	 * <ol>
	 * <li>Examine the {@linkplain HttpRequest request} and body</li>
	 * <li>Optionally {@linkplain org.springframework.http.client.support.HttpRequestWrapper
	 * wrap} the request to filter HTTP attributes.</li>
	 * <li>Optionally modify the body of the request.</li>
	 * <li><strong>Either</strong>
	 * <ul>
	 * <li>execute the request using
	 * {@link ClientHttpRequestExecution#execute(org.springframework.http.HttpRequest, byte[])},</li>
	 * <strong>or</strong>
	 * <li>do not execute the request to block the execution altogether.</li>
	 * </ul>
	 * <li>Optionally wrap the response to filter HTTP attributes.</li>
	 * </ol>
	 * @param request the request, containing method, URI, and headers
	 * @param body the body of the request
	 * @param execution the request execution
	 * @return the response
	 * @throws IOException in case of I/O errors
	 */
	ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException;

}
/**
 * 此方法的典型实现将遵循以下模式：
 * 检查请求和正文
 * （可选）包装请求以过滤HTTP属性。
 * （可选）修改请求的主体。
 * 要么
 * 使用ClientHttpRequestExecution.execute（HttpRequest，byte []）执行请求，
 * 要么
 * 不要执行请求以完全阻止执行。
 * （可选）包装响应以过滤HTTP属性。
 *
 * 参数：
 * request –请求，包含方法，URI和标头
 * 正文–请求的正文
 * 执行–请求执行
 */
