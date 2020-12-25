/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.web.servlet;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * A {@link ServletContextInitializer} to register {@link DelegatingFilterProxy}s in a
 * Servlet 3.0+ container. Similar to the {@link ServletContext#addFilter(String, Filter)
 * registration} features provided by {@link ServletContext} but with a Spring Bean
 * friendly design.
 * <p>
 * The bean name of the actual delegate {@link Filter} should be specified using the
 * {@code targetBeanName} constructor argument. Unlike the {@link FilterRegistrationBean},
 * referenced filters are not instantiated early. In fact, if the delegate filter bean is
 * marked {@code @Lazy} it won't be instantiated at all until the filter is called.
 * <p>
 * Registrations can be associated with {@link #setUrlPatterns URL patterns} and/or
 * servlets (either by {@link #setServletNames name} or via a
 * {@link #setServletRegistrationBeans ServletRegistrationBean}s. When no URL pattern or
 * servlets are specified the filter will be associated to '/*'. The targetBeanName will
 * be used as the filter name if not otherwise specified.
 *
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletContextInitializer
 * @see ServletContext#addFilter(String, Filter)
 * @see FilterRegistrationBean
 * @see DelegatingFilterProxy
 * 一个ServletContextInitializer，用于在Servlet 3.0+容器中注册DelegatingFilterProxys。
 * 与ServletContext提供的注册功能相似，但具有Spring Bean友好的设计。
 * 实际的委托过滤器的Bean名称应使用targetBeanName构造函数参数指定。
 * 与FilterRegistrationBean不同，引用的过滤器不会提早实例化。
 * 实际上，如果将委托过滤器bean标记为@Lazy，则在调用过滤器之前根本不会实例化它。
 * 可以将注册与URL模式和/或servlet相关联（通过名称或通过ServletRegistrationBeans进行关联。
 * 当未指定URL模式或servlet时，过滤器将与'/ *'相关联。如果未指定，则targetBeanName将用作过滤器名称
 */
public class DelegatingFilterProxyRegistrationBean extends AbstractFilterRegistrationBean<DelegatingFilterProxy>
		implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	private final String targetBeanName;

	/**
	 * Create a new {@link DelegatingFilterProxyRegistrationBean} instance to be
	 * registered with the specified {@link ServletRegistrationBean}s.
	 * @param targetBeanName name of the target filter bean to look up in the Spring
	 * application context (must not be {@code null}).
	 * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s
	 * 创建一个新的DelegatingFilterProxyRegistrationBean实例，以将其注册到指定的ServletRegistrationBeans。
	 *
	 * 参数：
	 * targetBeanName –要在Spring应用程序上下文中查找的目标过滤器bean的名称（不得为null）。
	 * servletRegistrationBeans –关联的ServletRegistrationBeans
	 */
	public DelegatingFilterProxyRegistrationBean(String targetBeanName,
			ServletRegistrationBean<?>... servletRegistrationBeans) {
		super(servletRegistrationBeans);
		Assert.hasLength(targetBeanName, "TargetBeanName must not be null or empty");
		this.targetBeanName = targetBeanName;
		setName(targetBeanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected String getTargetBeanName() {
		return this.targetBeanName;
	}

	@Override
	public DelegatingFilterProxy getFilter() {
		return new DelegatingFilterProxy(this.targetBeanName, getWebApplicationContext()) {

			@Override
			protected void initFilterBean() throws ServletException {
				// Don't initialize filter bean on init()
			}

		};
	}

	private WebApplicationContext getWebApplicationContext() {
		Assert.notNull(this.applicationContext, "ApplicationContext be injected");
		Assert.isInstanceOf(WebApplicationContext.class, this.applicationContext);
		return (WebApplicationContext) this.applicationContext;
	}

}
