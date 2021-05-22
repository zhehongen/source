/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.web.servlet.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Default implementation of {@link ErrorAttributes}. Provides the following attributes
 * when possible:
 * <ul>
 * <li>timestamp - The time that the errors were extracted</li>
 * <li>status - The status code</li>
 * <li>error - The error reason</li>
 * <li>exception - The class name of the root exception (if configured)</li>
 * <li>message - The exception message (if configured)</li>
 * <li>errors - Any {@link ObjectError}s from a {@link BindingResult} exception (if
 * configured)</li>
 * <li>trace - The exception stack trace (if configured)</li>
 * <li>path - The URL path when the exception was raised</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Vedran Pavic
 * @author Scott Frederick
 * @since 2.0.0
 * @see ErrorAttributes
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultErrorAttributes implements ErrorAttributes, HandlerExceptionResolver, Ordered {

	private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

	private final Boolean includeException;

	/**
	 * Create a new {@link DefaultErrorAttributes} instance.
	 */
	public DefaultErrorAttributes() {
		this.includeException = null;
	}

	/**
	 * Create a new {@link DefaultErrorAttributes} instance.
	 * @param includeException whether to include the "exception" attribute
	 * @deprecated since 2.3.0 in favor of
	 * {@link ErrorAttributeOptions#including(Include...)}
	 */
	@Deprecated
	public DefaultErrorAttributes(boolean includeException) {
		this.includeException = includeException;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		storeErrorAttributes(request, ex);
		return null;
	}

	private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
		request.setAttribute(ERROR_ATTRIBUTE, ex);
	}

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
		Map<String, Object> errorAttributes = getErrorAttributes(webRequest, options.isIncluded(Include.STACK_TRACE));
		if (this.includeException != null) {
			options = options.including(Include.EXCEPTION);
		}
		if (!options.isIncluded(Include.EXCEPTION)) {
			errorAttributes.remove("exception");
		}
		if (!options.isIncluded(Include.STACK_TRACE)) {
			errorAttributes.remove("trace");
		}
		if (!options.isIncluded(Include.MESSAGE) && errorAttributes.get("message") != null) {
			errorAttributes.put("message", "");
		}
		if (!options.isIncluded(Include.BINDING_ERRORS)) {
			errorAttributes.remove("errors");
		}
		return errorAttributes;
	}

	@Override
	@Deprecated
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		addStatus(errorAttributes, webRequest);
		addErrorDetails(errorAttributes, webRequest, includeStackTrace);
		addPath(errorAttributes, webRequest);
		return errorAttributes;
	}

	private void addStatus(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
		Integer status = getAttribute(requestAttributes, RequestDispatcher.ERROR_STATUS_CODE);
		if (status == null) {
			errorAttributes.put("status", 999);
			errorAttributes.put("error", "None");
			return;
		}
		errorAttributes.put("status", status);
		try {
			errorAttributes.put("error", HttpStatus.valueOf(status).getReasonPhrase());
		}
		catch (Exception ex) {
			// Unable to obtain a reason
			errorAttributes.put("error", "Http Status " + status);
		}
	}

	private void addErrorDetails(Map<String, Object> errorAttributes, WebRequest webRequest,
			boolean includeStackTrace) {
		Throwable error = getError(webRequest);
		if (error != null) {
			while (error instanceof ServletException && error.getCause() != null) {
				error = error.getCause();
			}
			errorAttributes.put("exception", error.getClass().getName());
			if (includeStackTrace) {
				addStackTrace(errorAttributes, error);
			}
		}
		addErrorMessage(errorAttributes, webRequest, error);
	}

	private void addErrorMessage(Map<String, Object> errorAttributes, WebRequest webRequest, Throwable error) {
		BindingResult result = extractBindingResult(error);
		if (result == null) {
			addExceptionErrorMessage(errorAttributes, webRequest, error);
		}
		else {
			addBindingResultErrorMessage(errorAttributes, result);
		}
	}

	private void addExceptionErrorMessage(Map<String, Object> errorAttributes, WebRequest webRequest, Throwable error) {
		Object message = getAttribute(webRequest, RequestDispatcher.ERROR_MESSAGE);
		if (StringUtils.isEmpty(message) && error != null) {
			message = error.getMessage();
		}
		if (StringUtils.isEmpty(message)) {
			message = "No message available";
		}
		errorAttributes.put("message", message);
	}

	private void addBindingResultErrorMessage(Map<String, Object> errorAttributes, BindingResult result) {
		errorAttributes.put("message", "Validation failed for object='" + result.getObjectName() + "'. "
				+ "Error count: " + result.getErrorCount());
		errorAttributes.put("errors", result.getAllErrors());
	}

	private BindingResult extractBindingResult(Throwable error) {
		if (error instanceof BindingResult) {
			return (BindingResult) error;
		}
		if (error instanceof MethodArgumentNotValidException) {
			return ((MethodArgumentNotValidException) error).getBindingResult();
		}
		return null;
	}

	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

	private void addPath(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
		String path = getAttribute(requestAttributes, RequestDispatcher.ERROR_REQUEST_URI);
		if (path != null) {
			errorAttributes.put("path", path);
		}
	}

	@Override
	public Throwable getError(WebRequest webRequest) {
		Throwable exception = getAttribute(webRequest, ERROR_ATTRIBUTE);
		return (exception != null) ? exception : getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
	}

	@SuppressWarnings("unchecked")
	private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
		return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
	}

}
