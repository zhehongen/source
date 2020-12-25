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
package org.springframework.security.web.header.writers.frameoptions;

import org.springframework.util.Assert;

import java.util.regex.Pattern;

/**
 * Implementation which uses a regular expression to validate the supplied origin. If the
 * value of the HTTP parameter matches the pattern, then the result will be ALLOW-FROM
 * &lt;paramter-value&gt;.
 *
 * @author Marten Deinum
 * @since 3.2
 */
public final class RegExpAllowFromStrategy extends
		AbstractRequestParameterAllowFromStrategy {

	private final Pattern pattern;

	/**
	 * Creates a new instance
	 *
	 * @param pattern the Pattern to compare against the HTTP parameter value. If the
	 * pattern matches, the domain will be allowed, else denied.
	 */
	public RegExpAllowFromStrategy(String pattern) {
		Assert.hasText(pattern, "Pattern cannot be empty.");
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	protected boolean allowed(String allowFromOrigin) {
		return pattern.matcher(allowFromOrigin).matches();
	}
}
