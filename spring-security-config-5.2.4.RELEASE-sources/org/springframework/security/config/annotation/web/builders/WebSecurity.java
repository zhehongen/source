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
package org.springframework.security.config.annotation.web.builders;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.AbstractConfiguredSecurityBuilder;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.DefaultWebInvocationPrivilegeEvaluator;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.debug.DebugFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * <p>
 * The {@link WebSecurity} is created by {@link WebSecurityConfiguration} to create the
 * {@link FilterChainProxy} known as the Spring Security Filter Chain
 * (springSecurityFilterChain). The springSecurityFilterChain is the {@link Filter} that
 * the {@link DelegatingFilterProxy} delegates to.
 * </p>
 *
 * <p>
 * Customizations to the {@link WebSecurity} can be made by creating a
 * {@link WebSecurityConfigurer} or more likely by overriding
 * {@link WebSecurityConfigurerAdapter}.
 * </p>
 *
 * @see EnableWebSecurity
 * @see WebSecurityConfiguration
 *
 * @author Rob Winch
 * @since 3.2
 */
public final class WebSecurity extends
		AbstractConfiguredSecurityBuilder<Filter, WebSecurity> implements
		SecurityBuilder<Filter>, ApplicationContextAware {
	private final Log logger = LogFactory.getLog(getClass());

	private final List<RequestMatcher> ignoredRequests = new ArrayList<>();

	private final List<SecurityBuilder<? extends SecurityFilterChain>> securityFilterChainBuilders = new ArrayList<>();

	private IgnoredRequestConfigurer ignoredRequestRegistry;

	private FilterSecurityInterceptor filterSecurityInterceptor;

	private HttpFirewall httpFirewall;

	private boolean debugEnabled;

	private WebInvocationPrivilegeEvaluator privilegeEvaluator;

	private DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();

	private SecurityExpressionHandler<FilterInvocation> expressionHandler = defaultWebSecurityExpressionHandler;

	private Runnable postBuildAction = () -> {
	};

	/**
	 * Creates a new instance
	 * @param objectPostProcessor the {@link ObjectPostProcessor} to use
	 * @see WebSecurityConfiguration
	 */
	public WebSecurity(ObjectPostProcessor<Object> objectPostProcessor) {
		super(objectPostProcessor);
	}

	/**
	 * <p>
	 * Allows adding {@link RequestMatcher} instances that Spring Security
	 * should ignore. Web Security provided by Spring Security (including the
	 * {@link SecurityContext}) will not be available on {@link HttpServletRequest} that
	 * match. Typically the requests that are registered should be that of only static
	 * resources. For requests that are dynamic, consider mapping the request to allow all
	 * users instead.
	 * </p>
	 *
	 * Example Usage:
	 *
	 * <pre>
	 * webSecurityBuilder.ignoring()
	 * // ignore all URLs that start with /resources/ or /static/
	 * 		.antMatchers(&quot;/resources/**&quot;, &quot;/static/**&quot;);
	 * </pre>
	 *
	 * Alternatively this will accomplish the same result:
	 *
	 * <pre>
	 * webSecurityBuilder.ignoring()
	 * // ignore all URLs that start with /resources/ or /static/
	 * 		.antMatchers(&quot;/resources/**&quot;).antMatchers(&quot;/static/**&quot;);
	 * </pre>
	 *
	 * Multiple invocations of ignoring() are also additive, so the following is also
	 * equivalent to the previous two examples:
	 *
	 * <pre>
	 * webSecurityBuilder.ignoring()
	 * // ignore all URLs that start with /resources/
	 * 		.antMatchers(&quot;/resources/**&quot;);
	 * webSecurityBuilder.ignoring()
	 * // ignore all URLs that start with /static/
	 * 		.antMatchers(&quot;/static/**&quot;);
	 * // now both URLs that start with /resources/ and /static/ will be ignored
	 * </pre>
	 *
	 * @return the {@link IgnoredRequestConfigurer} to use for registering request that
	 * should be ignored
	 */
	public IgnoredRequestConfigurer ignoring() {
		return ignoredRequestRegistry;
	}

	/**
	 * Allows customizing the {@link HttpFirewall}. The default is
	 * {@link StrictHttpFirewall}.
	 *
	 * @param httpFirewall the custom {@link HttpFirewall}
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity httpFirewall(HttpFirewall httpFirewall) {
		this.httpFirewall = httpFirewall;
		return this;
	}

	/**
	 * Controls debugging support for Spring Security.
	 *
	 * @param debugEnabled if true, enables debug support with Spring Security. Default is
	 * false.
	 *
	 * @return the {@link WebSecurity} for further customization.
	 * @see EnableWebSecurity#debug()
	 */
	public WebSecurity debug(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
		return this;
	}

	/**
	 * <p>
	 * Adds builders to create {@link SecurityFilterChain} instances.
	 * </p>
	 *
	 * <p>
	 * Typically this method is invoked automatically within the framework from
	 * {@link WebSecurityConfigurerAdapter#init(WebSecurity)}
	 * </p>
	 *
	 * @param securityFilterChainBuilder the builder to use to create the
	 * {@link SecurityFilterChain} instances
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity addSecurityFilterChainBuilder(
			SecurityBuilder<? extends SecurityFilterChain> securityFilterChainBuilder) {
		this.securityFilterChainBuilders.add(securityFilterChainBuilder);
		return this;
	}

	/**
	 * Set the {@link WebInvocationPrivilegeEvaluator} to be used. If this is not specified,
	 * then a {@link DefaultWebInvocationPrivilegeEvaluator} will be created when
	 * {@link #securityInterceptor(FilterSecurityInterceptor)} is non null.
	 *
	 * @param privilegeEvaluator the {@link WebInvocationPrivilegeEvaluator} to use
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity privilegeEvaluator(
			WebInvocationPrivilegeEvaluator privilegeEvaluator) {
		this.privilegeEvaluator = privilegeEvaluator;
		return this;
	}

	/**
	 * Set the {@link SecurityExpressionHandler} to be used. If this is not specified,
	 * then a {@link DefaultWebSecurityExpressionHandler} will be used.
	 *
	 * @param expressionHandler the {@link SecurityExpressionHandler} to use
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity expressionHandler(
			SecurityExpressionHandler<FilterInvocation> expressionHandler) {
		Assert.notNull(expressionHandler, "expressionHandler cannot be null");
		this.expressionHandler = expressionHandler;
		return this;
	}

	/**
	 * Gets the {@link SecurityExpressionHandler} to be used.
	 * @return the {@link SecurityExpressionHandler} for further customizations
	 */
	public SecurityExpressionHandler<FilterInvocation> getExpressionHandler() {
		return expressionHandler;
	}

	/**
	 * Gets the {@link WebInvocationPrivilegeEvaluator} to be used.
	 * @return the {@link WebInvocationPrivilegeEvaluator} for further customizations
	 */
	public WebInvocationPrivilegeEvaluator getPrivilegeEvaluator() {
		if (privilegeEvaluator != null) {
			return privilegeEvaluator;
		}
		return filterSecurityInterceptor == null ? null
				: new DefaultWebInvocationPrivilegeEvaluator(filterSecurityInterceptor);
	}

	/**
	 * Sets the {@link FilterSecurityInterceptor}. This is typically invoked by
	 * {@link WebSecurityConfigurerAdapter}.
	 * @param securityInterceptor the {@link FilterSecurityInterceptor} to use
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity securityInterceptor(FilterSecurityInterceptor securityInterceptor) {
		this.filterSecurityInterceptor = securityInterceptor;
		return this;
	}

	/**
	 * Executes the Runnable immediately after the build takes place
	 *
	 * @param postBuildAction
	 * @return the {@link WebSecurity} for further customizations
	 */
	public WebSecurity postBuildAction(Runnable postBuildAction) {
		this.postBuildAction = postBuildAction;
		return this;
	}

	@Override
	protected Filter performBuild() throws Exception {
		Assert.state(
				!securityFilterChainBuilders.isEmpty(),
				() -> "At least one SecurityBuilder<? extends SecurityFilterChain> needs to be specified. "
						+ "Typically this done by adding a @Configuration that extends WebSecurityConfigurerAdapter. "
						+ "More advanced users can invoke "
						+ WebSecurity.class.getSimpleName()
						+ ".addSecurityFilterChainBuilder directly");
		int chainSize = ignoredRequests.size() + securityFilterChainBuilders.size();
		List<SecurityFilterChain> securityFilterChains = new ArrayList<>(
				chainSize);
		for (RequestMatcher ignoredRequest : ignoredRequests) {
			securityFilterChains.add(new DefaultSecurityFilterChain(ignoredRequest));
		}
		for (SecurityBuilder<? extends SecurityFilterChain> securityFilterChainBuilder : securityFilterChainBuilders) {
			securityFilterChains.add(securityFilterChainBuilder.build());
		}
		FilterChainProxy filterChainProxy = new FilterChainProxy(securityFilterChains);
		if (httpFirewall != null) {
			filterChainProxy.setFirewall(httpFirewall);
		}
		filterChainProxy.afterPropertiesSet();

		Filter result = filterChainProxy;
		if (debugEnabled) {
			logger.warn("\n\n"
					+ "********************************************************************\n"
					+ "**********        Security debugging is enabled.       *************\n"
					+ "**********    This may include sensitive information.  *************\n"
					+ "**********      Do not use in a production system!     *************\n"
					+ "********************************************************************\n\n");
			result = new DebugFilter(filterChainProxy);
		}
		postBuildAction.run();
		return result;
	}

	/**
	 * An {@link IgnoredRequestConfigurer} that allows optionally configuring the
	 * {@link MvcRequestMatcher#setMethod(HttpMethod)}
	 *
	 * @author Rob Winch
	 */
	public final class MvcMatchersIgnoredRequestConfigurer
			extends IgnoredRequestConfigurer {
		private final List<MvcRequestMatcher> mvcMatchers;

		private MvcMatchersIgnoredRequestConfigurer(ApplicationContext context,
				List<MvcRequestMatcher> mvcMatchers) {
			super(context);
			this.mvcMatchers = mvcMatchers;
		}

		public IgnoredRequestConfigurer servletPath(String servletPath) {
			for (MvcRequestMatcher matcher : this.mvcMatchers) {
				matcher.setServletPath(servletPath);
			}
			return this;
		}
	}

	/**
	 * Allows registering {@link RequestMatcher} instances that should be ignored by
	 * Spring Security.
	 *
	 * @author Rob Winch
	 * @since 3.2
	 */
	public class IgnoredRequestConfigurer
			extends AbstractRequestMatcherRegistry<IgnoredRequestConfigurer> {

		private IgnoredRequestConfigurer(ApplicationContext context) {
			setApplicationContext(context);
		}

		@Override
		public MvcMatchersIgnoredRequestConfigurer mvcMatchers(HttpMethod method,
				String... mvcPatterns) {
			List<MvcRequestMatcher> mvcMatchers = createMvcMatchers(method, mvcPatterns);
			WebSecurity.this.ignoredRequests.addAll(mvcMatchers);
			return new MvcMatchersIgnoredRequestConfigurer(getApplicationContext(),
					mvcMatchers);
		}

		@Override
		public MvcMatchersIgnoredRequestConfigurer mvcMatchers(String... mvcPatterns) {
			return mvcMatchers(null, mvcPatterns);
		}

		@Override
		protected IgnoredRequestConfigurer chainRequestMatchers(
				List<RequestMatcher> requestMatchers) {
			WebSecurity.this.ignoredRequests.addAll(requestMatchers);
			return this;
		}

		/**
		 * Returns the {@link WebSecurity} to be returned for chaining.
		 */
		public WebSecurity and() {
			return WebSecurity.this;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.defaultWebSecurityExpressionHandler
				.setApplicationContext(applicationContext);
		try {
			this.defaultWebSecurityExpressionHandler.setPermissionEvaluator(applicationContext.getBean(
					PermissionEvaluator.class));
		} catch(NoSuchBeanDefinitionException e) {}

		this.ignoredRequestRegistry = new IgnoredRequestConfigurer(applicationContext);
		try {
			this.httpFirewall = applicationContext.getBean(HttpFirewall.class);
		} catch(NoSuchBeanDefinitionException e) {}
	}
}
