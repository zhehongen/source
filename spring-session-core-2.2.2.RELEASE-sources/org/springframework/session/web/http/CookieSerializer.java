/*
 * Copyright 2014-2019 the original author or authors.
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

package org.springframework.session.web.http;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Strategy for reading and writing a cookie value to the {@link HttpServletResponse}.
 *
 * @author Rob Winch
 * @since 1.1
 */
public interface CookieSerializer {

	/**
	 * Writes a given {@link CookieValue} to the provided {@link HttpServletResponse}.
	 * @param cookieValue the {@link CookieValue} to write to
	 * {@link CookieValue#getResponse()}. Cannot be null.
	 * 将给定的CookieSerializer.CookieValue写入提供的HttpServletResponse。
	 *
	 * 参数：
	 * cookieValue –要写入CookieSerializer.CookieValue.getResponse（）的CookieSerializer.CookieValue。 不能为null。                      
	 */
	void writeCookieValue(CookieValue cookieValue);

	/**
	 * Reads all the matching cookies from the {@link HttpServletRequest}. The result is a
	 * List since there can be multiple {@link Cookie} in a single request with a matching
	 * name. For example, one Cookie may have a path of / and another of /context, but the
	 * path is not transmitted in the request.
	 * @param request the {@link HttpServletRequest} to read the cookie from. Cannot be
	 * null.
	 * @return the values of all the matching cookies
	 * 从HttpServletRequest读取所有匹配的cookie。 结果是一个列表，因为单个请求中可以有多个具有匹配名称的Cookie。 
	 * 例如，一个Cookie可能具有/的路径，而另一个具有/ context的路径，但是该路径未在请求中传输。
	 *
	 * 参数：
	 * request –从中读取cookie的HttpServletRequest。 不能为null。
	 * 返回值：
	 * 所有匹配Cookie的值
	 */
	List<String> readCookieValues(HttpServletRequest request);

	/**
	 * Contains the information necessary to write a value to the
	 * {@link HttpServletResponse}.
	 *
	 * @author Rob Winch
	 * @author Vedran Pavic
	 * @since 1.1
	 * 包含将值写入HttpServletResponse所需的信息。
	 */
	class CookieValue {

		private final HttpServletRequest request;

		private final HttpServletResponse response;

		private final String cookieValue;

		private int cookieMaxAge = -1;

		/**
		 * Creates a new instance.
		 * @param request the {@link HttpServletRequest} to use. Useful for determining
		 * the context in which the cookie is set. Cannot be null.
		 * @param response the {@link HttpServletResponse} to use.
		 * @param cookieValue the value of the cookie to be written. This value may be
		 * modified by the {@link CookieSerializer} when writing to the actual cookie so
		 * long as the original value is returned when the cookie is read.
		 *  创建一个新实例。
		 *
		 * 参数：
		 * request –要使用的HttpServletRequest。 用于确定设置cookie的上下文。 不能为null。
		 * response –使用的HttpServletResponse。
		 * cookieValue –要写入的cookie的值。 
		  只要写入Cookie时返回原始值，就可以在写入实际cookie时由CookieSerializer修改此值。                        
		 */
		public CookieValue(HttpServletRequest request, HttpServletResponse response, String cookieValue) {
			this.request = request;
			this.response = response;
			this.cookieValue = cookieValue;
			if ("".equals(this.cookieValue)) {
				this.cookieMaxAge = 0;
			}
		}

		/**
		 * Gets the request to use.
		 * @return the request to use. Cannot be null.
		 */
		public HttpServletRequest getRequest() {
			return this.request;
		}

		/**
		 * Gets the response to write to.
		 * @return the response to write to. Cannot be null.
		 */
		public HttpServletResponse getResponse() {
			return this.response;
		}

		/**
		 * The value to be written. This value may be modified by the
		 * {@link CookieSerializer} before written to the cookie. However, the value must
		 * be the same as the original when it is read back in.
		 * @return the value to be written
		 * 要写入的值。 CookieSerializer可以在将此值写入Cookie之前对其进行修改。 
		 * 但是，读回时，该值必须与原始值相同。
		 *
		 * 返回值：
		 * 要写的值
		 */
		public String getCookieValue() {
			return this.cookieValue;
		}

		/**
		 * Get the cookie max age. The default is -1 which signals to delete the cookie
		 * when the browser is closed, or 0 if cookie value is empty.
		 * @return the cookie max age
		 * 获取Cookie的最长使用期限。 默认值为-1，表示关闭浏览器时删除cookie；如果cookie值为空，则默认为0。
		 *
		 * 返回值：
		 * cookie的最大年龄
		 */
		public int getCookieMaxAge() {
			return this.cookieMaxAge;
		}

		/**
		 * Set the cookie max age.
		 * @param cookieMaxAge the cookie max age
		 */
		public void setCookieMaxAge(int cookieMaxAge) {
			this.cookieMaxAge = cookieMaxAge;
		}

	}

}
