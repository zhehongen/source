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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for AllowFromStrategy implementations which use a request parameter to
 * retrieve the origin. By default the parameter named <code>x-frames-allow-from</code> is
 * read from the request.
 *
 * @author Marten Deinum
 * @since 3.2
 */
abstract class AbstractRequestParameterAllowFromStrategy implements AllowFromStrategy {

	private static final String DEFAULT_ORIGIN_REQUEST_PARAMETER = "x-frames-allow-from";

	private String allowFromParameterName = DEFAULT_ORIGIN_REQUEST_PARAMETER;

	/** Logger for use by subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	public String getAllowFromValue(HttpServletRequest request) {
		String allowFromOrigin = request.getParameter(allowFromParameterName);
		if (log.isDebugEnabled()) {
			log.debug("Supplied origin '" + allowFromOrigin + "'");
		}
		if (StringUtils.hasText(allowFromOrigin) && allowed(allowFromOrigin)) {
			return allowFromOrigin;
		}
		else {
			return "DENY";
		}
	}

	/**
	 * Sets the HTTP parameter used to retrieve the value for the origin that is allowed
	 * from. The value of the parameter should be a valid URL. The default parameter name
	 * is "x-frames-allow-from".
	 *
	 * @param allowFromParameterName the name of the HTTP parameter to
	 */
	public void setAllowFromParameterName(String allowFromParameterName) {
		Assert.notNull(allowFromParameterName, "allowFromParameterName cannot be null");
		this.allowFromParameterName = allowFromParameterName;
	}

	/**
	 * Method to be implemented by base classes, used to determine if the supplied origin
	 * is allowed.
	 *
	 * @param allowFromOrigin the supplied origin
	 * @return <code>true</code> if the supplied origin is allowed.
	 */
	protected abstract boolean allowed(String allowFromOrigin);
}
