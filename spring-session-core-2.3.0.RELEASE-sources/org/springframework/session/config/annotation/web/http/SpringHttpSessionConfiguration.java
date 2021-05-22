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

package org.springframework.session.config.annotation.web.http;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionEventHttpSessionListenerAdapter;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**配置在Web环境中设置Spring Session的基础。为了使用它，您必须提供一个SessionRepository。
 * Configures the basics for setting up Spring Session in a web environment. In order to
 * use it, you must provide a {@link SessionRepository}. For example:
 *
 * <pre>
 * {@literal @Configuration}
 * {@literal @EnableSpringHttpSession}
 * public class SpringHttpSessionConfig {
 *
 *     {@literal @Bean}
 *     public MapSessionRepository sessionRepository() {
 *         return new MapSessionRepository(new ConcurrentHashMap<>());
 *     }
 *
 * }
 * </pre>
 *
 * <p>
 * It is important to note that no infrastructure for session expirations is configured
 * for you out of the box. This is because things like session expiration are highly
 * implementation dependent. This means if you require cleaning up expired sessions, you
 * are responsible for cleaning up the expired sessions.
 * </p>
 *
 * <p>
 * The following is provided for you with the base configuration:
 * </p>
 *重要的是要注意，没有为您配置开箱即用的会话过期基础结构。这是因为像会话到期这样的事情在很大程度上取决于实现。这意味着如果您需要清理过期的会话，则有责任清理过期的会话。基本配置为您提供了以下内容：SessionRepositoryFilter-负责使用由SessionRepository支持的HttpSession实现包装HttpServletRequest .SessionEventHttpSessionListenerAdapter-负责将Spring Session事件转换为HttpSessionEvent。为了使其正常工作，您提供的SessionRepository的实现必须支持SessionCreatedEvent和SessionDestroyedEvent。
 * <ul>
 * <li>SessionRepositoryFilter - is responsible for wrapping the HttpServletRequest with
 * an implementation of HttpSession that is backed by a SessionRepository</li>
 * <li>SessionEventHttpSessionListenerAdapter - is responsible for translating Spring
 * Session events into HttpSessionEvent. In order for it to work, the implementation of
 * SessionRepository you provide must support {@link SessionCreatedEvent} and
 * {@link SessionDestroyedEvent}.</li>
 * <li>
 * </ul>
 *
 * @author Rob Winch
 * @author Vedran Pavic
 * @since 1.1
 * @see EnableSpringHttpSession
 */
@Configuration(proxyBeanMethods = false)
public class SpringHttpSessionConfiguration implements ApplicationContextAware {

	private final Log logger = LogFactory.getLog(getClass());

	private CookieHttpSessionIdResolver defaultHttpSessionIdResolver = new CookieHttpSessionIdResolver();

	private boolean usesSpringSessionRememberMeServices;

	private ServletContext servletContext;//说明：自动注入

	private CookieSerializer cookieSerializer;//说明：自动注入

	private HttpSessionIdResolver httpSessionIdResolver = this.defaultHttpSessionIdResolver;//说明：自动注入

	private List<HttpSessionListener> httpSessionListeners = new ArrayList<>();//说明：自动注入

	@PostConstruct
	public void init() {//说明：使用注入的cookieSerializer
		CookieSerializer cookieSerializer = (this.cookieSerializer != null) ? this.cookieSerializer
				: createDefaultCookieSerializer();
		this.defaultHttpSessionIdResolver.setCookieSerializer(cookieSerializer);
	}

	@Bean
	public SessionEventHttpSessionListenerAdapter sessionEventHttpSessionListenerAdapter() {
		return new SessionEventHttpSessionListenerAdapter(this.httpSessionListeners);
	}

	@Bean
	public <S extends Session> SessionRepositoryFilter<? extends Session> springSessionRepositoryFilter(
			SessionRepository<S> sessionRepository) {
		SessionRepositoryFilter<S> sessionRepositoryFilter = new SessionRepositoryFilter<>(sessionRepository);
		sessionRepositoryFilter.setHttpSessionIdResolver(this.httpSessionIdResolver);
		return sessionRepositoryFilter;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (ClassUtils.isPresent("org.springframework.security.web.authentication.RememberMeServices", null)) {
			this.usesSpringSessionRememberMeServices = !ObjectUtils
					.isEmpty(applicationContext.getBeanNamesForType(SpringSessionRememberMeServices.class));
		}
	}
//说明：自动注入
	@Autowired(required = false)
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Autowired(required = false)
	public void setCookieSerializer(CookieSerializer cookieSerializer) {
		this.cookieSerializer = cookieSerializer;
	}

	@Autowired(required = false)
	public void setHttpSessionIdResolver(HttpSessionIdResolver httpSessionIdResolver) {
		this.httpSessionIdResolver = httpSessionIdResolver;
	}

	@Autowired(required = false)
	public void setHttpSessionListeners(List<HttpSessionListener> listeners) {
		this.httpSessionListeners = listeners;
	}

	private CookieSerializer createDefaultCookieSerializer() {
		DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
		if (this.servletContext != null) {
			SessionCookieConfig sessionCookieConfig = null;
			try {
				sessionCookieConfig = this.servletContext.getSessionCookieConfig();
			}
			catch (UnsupportedOperationException ex) {
				this.logger.warn("Unable to obtain SessionCookieConfig: " + ex.getMessage());
			}
			if (sessionCookieConfig != null) {
				if (sessionCookieConfig.getName() != null) {
					cookieSerializer.setCookieName(sessionCookieConfig.getName());
				}
				if (sessionCookieConfig.getDomain() != null) {
					cookieSerializer.setDomainName(sessionCookieConfig.getDomain());
				}
				if (sessionCookieConfig.getPath() != null) {
					cookieSerializer.setCookiePath(sessionCookieConfig.getPath());
				}
				if (sessionCookieConfig.getMaxAge() != -1) {
					cookieSerializer.setCookieMaxAge(sessionCookieConfig.getMaxAge());
				}
			}
		}
		if (this.usesSpringSessionRememberMeServices) {
			cookieSerializer.setRememberMeRequestAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
		}
		return cookieSerializer;
	}

}