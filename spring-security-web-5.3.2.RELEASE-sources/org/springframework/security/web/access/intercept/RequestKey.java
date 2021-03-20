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
package org.springframework.security.web.access.intercept;

import org.springframework.util.Assert;

/**
 * @author Luke Taylor
 * @since 2.0
 */
public class RequestKey {
	private final String url;
	private final String method;

	public RequestKey(String url) {
		this(url, null);
	}

	public RequestKey(String url, String method) {
		Assert.notNull(url, "url cannot be null");
		this.url = url;
		this.method = method;
	}

	String getUrl() {
		return url;
	}

	String getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		int result = this.url.hashCode();
		result = 31 * result + (this.method != null ? this.method.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RequestKey)) {
			return false;
		}

		RequestKey key = (RequestKey) obj;

		if (!url.equals(key.url)) {
			return false;
		}

		if (method == null) {
			return key.method == null;
		}

		return method.equals(key.method);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(url.length() + 7);
		sb.append("[");
		if (method != null) {
			sb.append(method).append(",");
		}
		sb.append(url);
		sb.append("]");

		return sb.toString();
	}
}
