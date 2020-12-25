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
package org.springframework.security.web.util.matcher;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Matches any supplied request.
 *
 * @author Luke Taylor
 * @since 3.1
 */
public final class AnyRequestMatcher implements RequestMatcher {
	public static final RequestMatcher INSTANCE = new AnyRequestMatcher();

	public boolean matches(HttpServletRequest request) {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean equals(Object obj) {
		return obj instanceof AnyRequestMatcher
				|| obj instanceof org.springframework.security.web.util.matcher.AnyRequestMatcher;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public String toString() {
		return "any request";
	}

	private AnyRequestMatcher() {
	}
}
