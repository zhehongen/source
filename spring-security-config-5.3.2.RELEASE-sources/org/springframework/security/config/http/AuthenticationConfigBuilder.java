/*
 * Copyright 2002-2020 the original author or authors.
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
package org.springframework.security.config.http;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Elements;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleMappableAttributesRetriever;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

import static org.springframework.security.config.http.SecurityFilters.ANONYMOUS_FILTER;
import static org.springframework.security.config.http.SecurityFilters.BASIC_AUTH_FILTER;
import static org.springframework.security.config.http.SecurityFilters.BEARER_TOKEN_AUTH_FILTER;
import static org.springframework.security.config.http.SecurityFilters.EXCEPTION_TRANSLATION_FILTER;
import static org.springframework.security.config.http.SecurityFilters.FORM_LOGIN_FILTER;
import static org.springframework.security.config.http.SecurityFilters.LOGIN_PAGE_FILTER;
import static org.springframework.security.config.http.SecurityFilters.LOGOUT_FILTER;
import static org.springframework.security.config.http.SecurityFilters.LOGOUT_PAGE_FILTER;
import static org.springframework.security.config.http.SecurityFilters.OAUTH2_AUTHORIZATION_CODE_GRANT_FILTER;
import static org.springframework.security.config.http.SecurityFilters.OAUTH2_AUTHORIZATION_REQUEST_FILTER;
import static org.springframework.security.config.http.SecurityFilters.OAUTH2_LOGIN_FILTER;
import static org.springframework.security.config.http.SecurityFilters.OPENID_FILTER;
import static org.springframework.security.config.http.SecurityFilters.PRE_AUTH_FILTER;
import static org.springframework.security.config.http.SecurityFilters.REMEMBER_ME_FILTER;
import static org.springframework.security.config.http.SecurityFilters.X509_FILTER;

/**
 * Handles creation of authentication mechanism filters and related beans for &lt;http&gt;
 * parsing.
 *
 * @author Luke Taylor
 * @author Rob Winch
 * @since 3.0
 */
final class AuthenticationConfigBuilder {
	private final Log logger = LogFactory.getLog(getClass());

	private static final String ATT_REALM = "realm";
	private static final String DEF_REALM = "Realm";

	static final String OPEN_ID_AUTHENTICATION_PROCESSING_FILTER_CLASS = "org.springframework.security.openid.OpenIDAuthenticationFilter";
	static final String OPEN_ID_AUTHENTICATION_PROVIDER_CLASS = "org.springframework.security.openid.OpenIDAuthenticationProvider";
	private static final String OPEN_ID_CONSUMER_CLASS = "org.springframework.security.openid.OpenID4JavaConsumer";
	static final String OPEN_ID_ATTRIBUTE_CLASS = "org.springframework.security.openid.OpenIDAttribute";
	private static final String OPEN_ID_ATTRIBUTE_FACTORY_CLASS = "org.springframework.security.openid.RegexBasedAxFetchListFactory";
	static final String AUTHENTICATION_PROCESSING_FILTER_CLASS = "org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter";

	static final String ATT_AUTH_DETAILS_SOURCE_REF = "authentication-details-source-ref";

	private static final String ATT_AUTO_CONFIG = "auto-config";

	private static final String ATT_ACCESS_DENIED_ERROR_PAGE = "error-page";
	private static final String ATT_ENTRY_POINT_REF = "entry-point-ref";

	private static final String ATT_USER_SERVICE_REF = "user-service-ref";

	private static final String ATT_KEY = "key";

	private final Element httpElt;
	private final ParserContext pc;

	private final boolean autoConfig;
	private final boolean allowSessionCreation;

	private RootBeanDefinition anonymousFilter;
	private BeanReference anonymousProviderRef;
	private BeanDefinition rememberMeFilter;
	private String rememberMeServicesId;
	private BeanReference rememberMeProviderRef;
	private BeanDefinition basicFilter;
	private RuntimeBeanReference basicEntryPoint;
	private BeanDefinition formEntryPoint;
	private BeanDefinition openIDEntryPoint;
	private BeanReference openIDProviderRef;
	private String formFilterId = null;
	private String openIDFilterId = null;
	private BeanDefinition x509Filter;
	private BeanReference x509ProviderRef;
	private BeanDefinition jeeFilter;
	private BeanReference jeeProviderRef;
	private RootBeanDefinition preAuthEntryPoint;
	private BeanMetadataElement mainEntryPoint;
	private BeanMetadataElement accessDeniedHandler;

	private BeanDefinition bearerTokenAuthenticationFilter;

	private BeanDefinition logoutFilter;
	@SuppressWarnings("rawtypes")
	private ManagedList logoutHandlers;
	private BeanDefinition loginPageGenerationFilter;
	private BeanDefinition logoutPageGenerationFilter;
	private BeanDefinition etf;
	private final BeanReference requestCache;
	private final BeanReference portMapper;
	private final BeanReference portResolver;
	private final BeanMetadataElement csrfLogoutHandler;

	private String loginProcessingUrl;
	private String openidLoginProcessingUrl;

	private String formLoginPage;

	private String openIDLoginPage;

	private String oauth2LoginFilterId;
	private BeanDefinition oauth2AuthorizationRequestRedirectFilter;
	private BeanDefinition oauth2LoginEntryPoint;
	private BeanReference oauth2LoginAuthenticationProviderRef;
	private BeanReference oauth2LoginOidcAuthenticationProviderRef;
	private BeanDefinition oauth2LoginLinks;
	private BeanDefinition authorizationRequestRedirectFilter;
	private BeanDefinition authorizationCodeGrantFilter;
	private BeanReference authorizationCodeAuthenticationProviderRef;

	private final List<BeanReference> authenticationProviders = new ManagedList<>();
	private final Map<BeanDefinition, BeanMetadataElement> defaultDeniedHandlerMappings = new ManagedMap<>();
	private final Map<BeanDefinition, BeanMetadataElement> defaultEntryPointMappings = new ManagedMap<>();
	private final List<BeanDefinition> csrfIgnoreRequestMatchers = new ManagedList<>();

	AuthenticationConfigBuilder(Element element, boolean forceAutoConfig,
			ParserContext pc, SessionCreationPolicy sessionPolicy,
			BeanReference requestCache, BeanReference authenticationManager,
			BeanReference sessionStrategy, BeanReference portMapper,
			BeanReference portResolver, BeanMetadataElement csrfLogoutHandler) {
		this.httpElt = element;
		this.pc = pc;
		this.requestCache = requestCache;
		autoConfig = forceAutoConfig
				| "true".equals(element.getAttribute(ATT_AUTO_CONFIG));
		this.allowSessionCreation = sessionPolicy != SessionCreationPolicy.NEVER
				&& sessionPolicy != SessionCreationPolicy.STATELESS;
		this.portMapper = portMapper;
		this.portResolver = portResolver;
		this.csrfLogoutHandler = csrfLogoutHandler;

		createAnonymousFilter();
		createRememberMeFilter(authenticationManager);
		createBasicFilter(authenticationManager);
		createBearerTokenAuthenticationFilter(authenticationManager);
		createFormLoginFilter(sessionStrategy, authenticationManager);
		createOAuth2LoginFilter(sessionStrategy, authenticationManager);
		createOAuth2ClientFilter(requestCache, authenticationManager);
		createOpenIDLoginFilter(sessionStrategy, authenticationManager);
		createX509Filter(authenticationManager);
		createJeeFilter(authenticationManager);
		createLogoutFilter();
		createLoginPageFilterIfNeeded();
		createUserDetailsServiceFactory();
		createExceptionTranslationFilter();
	}

	void createRememberMeFilter(BeanReference authenticationManager) {

		// Parse remember me before logout as RememberMeServices is also a LogoutHandler
		// implementation.
		Element rememberMeElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.REMEMBER_ME);

		if (rememberMeElt != null) {
			String key = rememberMeElt.getAttribute(ATT_KEY);

			if (!StringUtils.hasText(key)) {
				key = createKey();
			}

			RememberMeBeanDefinitionParser rememberMeParser = new RememberMeBeanDefinitionParser(
					key, authenticationManager);
			rememberMeFilter = rememberMeParser.parse(rememberMeElt, pc);
			rememberMeServicesId = rememberMeParser.getRememberMeServicesId();
			createRememberMeProvider(key);
		}
	}

	private void createRememberMeProvider(String key) {
		RootBeanDefinition provider = new RootBeanDefinition(
				RememberMeAuthenticationProvider.class);
		provider.setSource(rememberMeFilter.getSource());

		provider.getConstructorArgumentValues().addGenericArgumentValue(key);

		String id = pc.getReaderContext().generateBeanName(provider);
		pc.registerBeanComponent(new BeanComponentDefinition(provider, id));

		rememberMeProviderRef = new RuntimeBeanReference(id);
	}

	void createFormLoginFilter(BeanReference sessionStrategy, BeanReference authManager) {

		Element formLoginElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.FORM_LOGIN);
		RootBeanDefinition formFilter = null;

		if (formLoginElt != null || autoConfig) {
			FormLoginBeanDefinitionParser parser = new FormLoginBeanDefinitionParser(
					"/login", "POST", AUTHENTICATION_PROCESSING_FILTER_CLASS,
					requestCache, sessionStrategy, allowSessionCreation, portMapper,
					portResolver);

			parser.parse(formLoginElt, pc);
			formFilter = parser.getFilterBean();
			formEntryPoint = parser.getEntryPointBean();
			loginProcessingUrl = parser.getLoginProcessingUrl();
			formLoginPage = parser.getLoginPage();
		}

		if (formFilter != null) {
			formFilter.getPropertyValues().addPropertyValue("allowSessionCreation",
					allowSessionCreation);
			formFilter.getPropertyValues().addPropertyValue("authenticationManager",
					authManager);

			// Id is required by login page filter
			formFilterId = pc.getReaderContext().generateBeanName(formFilter);
			pc.registerBeanComponent(new BeanComponentDefinition(formFilter, formFilterId));
			injectRememberMeServicesRef(formFilter, rememberMeServicesId);
		}
	}

	void createOAuth2LoginFilter(BeanReference sessionStrategy, BeanReference authManager) {
		Element oauth2LoginElt = DomUtils.getChildElementByTagName(this.httpElt, Elements.OAUTH2_LOGIN);
		if (oauth2LoginElt == null) {
			return;
		}

		OAuth2LoginBeanDefinitionParser parser = new OAuth2LoginBeanDefinitionParser(requestCache, portMapper,
				portResolver, sessionStrategy, allowSessionCreation);
		BeanDefinition oauth2LoginFilterBean = parser.parse(oauth2LoginElt, this.pc);
		oauth2LoginFilterBean.getPropertyValues().addPropertyValue("authenticationManager", authManager);

		// retrieve the other bean result
		BeanDefinition oauth2LoginAuthProvider = parser.getOAuth2LoginAuthenticationProvider();
		oauth2AuthorizationRequestRedirectFilter = parser.getOAuth2AuthorizationRequestRedirectFilter();
		oauth2LoginEntryPoint = parser.getOAuth2LoginAuthenticationEntryPoint();

		// generate bean name to be registered
		String oauth2LoginAuthProviderId = pc.getReaderContext()
				.generateBeanName(oauth2LoginAuthProvider);
		oauth2LoginFilterId = pc.getReaderContext().generateBeanName(oauth2LoginFilterBean);
		String oauth2AuthorizationRequestRedirectFilterId = pc.getReaderContext()
				.generateBeanName(oauth2AuthorizationRequestRedirectFilter);
		oauth2LoginLinks = parser.getOAuth2LoginLinks();

		// register the component
		pc.registerBeanComponent(new BeanComponentDefinition(oauth2LoginFilterBean, oauth2LoginFilterId));
		pc.registerBeanComponent(new BeanComponentDefinition(
				oauth2AuthorizationRequestRedirectFilter, oauth2AuthorizationRequestRedirectFilterId));
		pc.registerBeanComponent(new BeanComponentDefinition(oauth2LoginAuthProvider, oauth2LoginAuthProviderId));

		oauth2LoginAuthenticationProviderRef = new RuntimeBeanReference(oauth2LoginAuthProviderId);

		// oidc provider
		BeanDefinition oauth2LoginOidcAuthProvider = parser.getOAuth2LoginOidcAuthenticationProvider();
		String oauth2LoginOidcAuthProviderId = pc.getReaderContext().generateBeanName(oauth2LoginOidcAuthProvider);
		pc.registerBeanComponent(new BeanComponentDefinition(
				oauth2LoginOidcAuthProvider, oauth2LoginOidcAuthProviderId));
		oauth2LoginOidcAuthenticationProviderRef = new RuntimeBeanReference(oauth2LoginOidcAuthProviderId);
	}

	void createOAuth2ClientFilter(BeanReference requestCache, BeanReference authenticationManager) {
		Element oauth2ClientElt = DomUtils.getChildElementByTagName(this.httpElt, Elements.OAUTH2_CLIENT);
		if (oauth2ClientElt == null) {
			return;
		}

		OAuth2ClientBeanDefinitionParser parser = new OAuth2ClientBeanDefinitionParser(
				requestCache, authenticationManager);
		parser.parse(oauth2ClientElt, this.pc);

		this.authorizationRequestRedirectFilter = parser.getAuthorizationRequestRedirectFilter();
		String authorizationRequestRedirectFilterId = pc.getReaderContext()
				.generateBeanName(this.authorizationRequestRedirectFilter);
		this.pc.registerBeanComponent(new BeanComponentDefinition(
				this.authorizationRequestRedirectFilter, authorizationRequestRedirectFilterId));

		this.authorizationCodeGrantFilter = parser.getAuthorizationCodeGrantFilter();
		String authorizationCodeGrantFilterId = pc.getReaderContext()
				.generateBeanName(this.authorizationCodeGrantFilter);
		this.pc.registerBeanComponent(new BeanComponentDefinition(
				this.authorizationCodeGrantFilter, authorizationCodeGrantFilterId));

		BeanDefinition authorizationCodeAuthenticationProvider = parser.getAuthorizationCodeAuthenticationProvider();
		String authorizationCodeAuthenticationProviderId = pc.getReaderContext()
				.generateBeanName(authorizationCodeAuthenticationProvider);
		this.pc.registerBeanComponent(new BeanComponentDefinition(
				authorizationCodeAuthenticationProvider, authorizationCodeAuthenticationProviderId));
		this.authorizationCodeAuthenticationProviderRef = new RuntimeBeanReference(authorizationCodeAuthenticationProviderId);
	}

	void createOpenIDLoginFilter(BeanReference sessionStrategy, BeanReference authManager) {
		Element openIDLoginElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.OPENID_LOGIN);
		RootBeanDefinition openIDFilter = null;

		if (openIDLoginElt != null) {
			FormLoginBeanDefinitionParser parser = new FormLoginBeanDefinitionParser(
					"/login/openid", null,
					OPEN_ID_AUTHENTICATION_PROCESSING_FILTER_CLASS, requestCache,
					sessionStrategy, allowSessionCreation, portMapper, portResolver);

			parser.parse(openIDLoginElt, pc);
			openIDFilter = parser.getFilterBean();
			openIDEntryPoint = parser.getEntryPointBean();
			openidLoginProcessingUrl = parser.getLoginProcessingUrl();
			openIDLoginPage = parser.getLoginPage();

			List<Element> attrExElts = DomUtils.getChildElementsByTagName(openIDLoginElt,
					Elements.OPENID_ATTRIBUTE_EXCHANGE);

			if (!attrExElts.isEmpty()) {
				// Set up the consumer with the required attribute list
				BeanDefinitionBuilder consumerBldr = BeanDefinitionBuilder
						.rootBeanDefinition(OPEN_ID_CONSUMER_CLASS);
				BeanDefinitionBuilder axFactory = BeanDefinitionBuilder
						.rootBeanDefinition(OPEN_ID_ATTRIBUTE_FACTORY_CLASS);
				ManagedMap<String, ManagedList<BeanDefinition>> axMap = new ManagedMap<>();

				for (Element attrExElt : attrExElts) {
					String identifierMatch = attrExElt.getAttribute("identifier-match");

					if (!StringUtils.hasText(identifierMatch)) {
						if (attrExElts.size() > 1) {
							pc.getReaderContext().error(
									"You must supply an identifier-match attribute if using more"
											+ " than one "
											+ Elements.OPENID_ATTRIBUTE_EXCHANGE
											+ " element", attrExElt);
						}
						// Match anything
						identifierMatch = ".*";
					}

					axMap.put(identifierMatch, parseOpenIDAttributes(attrExElt));
				}
				axFactory.addConstructorArgValue(axMap);

				consumerBldr.addConstructorArgValue(axFactory.getBeanDefinition());
				openIDFilter.getPropertyValues().addPropertyValue("consumer",
						consumerBldr.getBeanDefinition());
			}
		}

		if (openIDFilter != null) {
			openIDFilter.getPropertyValues().addPropertyValue("allowSessionCreation",
					allowSessionCreation);
			openIDFilter.getPropertyValues().addPropertyValue("authenticationManager",
					authManager);
			// Required by login page filter
			openIDFilterId = pc.getReaderContext().generateBeanName(openIDFilter);
			pc.registerBeanComponent(new BeanComponentDefinition(openIDFilter,
					openIDFilterId));
			injectRememberMeServicesRef(openIDFilter, rememberMeServicesId);

			createOpenIDProvider();
		}
	}

	private ManagedList<BeanDefinition> parseOpenIDAttributes(Element attrExElt) {
		ManagedList<BeanDefinition> attributes = new ManagedList<>();
		for (Element attElt : DomUtils.getChildElementsByTagName(attrExElt,
				Elements.OPENID_ATTRIBUTE)) {
			String name = attElt.getAttribute("name");
			String type = attElt.getAttribute("type");
			String required = attElt.getAttribute("required");
			String count = attElt.getAttribute("count");
			BeanDefinitionBuilder attrBldr = BeanDefinitionBuilder
					.rootBeanDefinition(OPEN_ID_ATTRIBUTE_CLASS);
			attrBldr.addConstructorArgValue(name);
			attrBldr.addConstructorArgValue(type);
			if (StringUtils.hasLength(required)) {
				attrBldr.addPropertyValue("required", Boolean.valueOf(required));
			}

			if (StringUtils.hasLength(count)) {
				attrBldr.addPropertyValue("count", Integer.parseInt(count));
			}
			attributes.add(attrBldr.getBeanDefinition());
		}

		return attributes;
	}

	private void createOpenIDProvider() {
		Element openIDLoginElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.OPENID_LOGIN);
		BeanDefinitionBuilder openIDProviderBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(OPEN_ID_AUTHENTICATION_PROVIDER_CLASS);

		RootBeanDefinition uds = new RootBeanDefinition();
		uds.setFactoryBeanName(BeanIds.USER_DETAILS_SERVICE_FACTORY);
		uds.setFactoryMethodName("authenticationUserDetailsService");
		uds.getConstructorArgumentValues().addGenericArgumentValue(
				openIDLoginElt.getAttribute(ATT_USER_SERVICE_REF));

		openIDProviderBuilder.addPropertyValue("authenticationUserDetailsService", uds);

		BeanDefinition openIDProvider = openIDProviderBuilder.getBeanDefinition();
		openIDProviderRef = new RuntimeBeanReference(pc.getReaderContext()
				.registerWithGeneratedName(openIDProvider));
	}

	private void injectRememberMeServicesRef(RootBeanDefinition bean,
			String rememberMeServicesId) {
		if (rememberMeServicesId != null) {
			bean.getPropertyValues().addPropertyValue("rememberMeServices",
					new RuntimeBeanReference(rememberMeServicesId));
		}
	}

	void createBasicFilter(BeanReference authManager) {
		Element basicAuthElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.BASIC_AUTH);

		if (basicAuthElt == null && !autoConfig) {
			// No basic auth, do nothing
			return;
		}

		String realm = httpElt.getAttribute(ATT_REALM);
		if (!StringUtils.hasText(realm)) {
			realm = DEF_REALM;
		}

		BeanDefinitionBuilder filterBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(BasicAuthenticationFilter.class);

		String entryPointId;

		if (basicAuthElt != null) {
			if (StringUtils.hasText(basicAuthElt.getAttribute(ATT_ENTRY_POINT_REF))) {
				basicEntryPoint = new RuntimeBeanReference(
						basicAuthElt.getAttribute(ATT_ENTRY_POINT_REF));
			}

			injectAuthenticationDetailsSource(basicAuthElt, filterBuilder);

		}

		if (basicEntryPoint == null) {
			RootBeanDefinition entryPoint = new RootBeanDefinition(
					BasicAuthenticationEntryPoint.class);
			entryPoint.setSource(pc.extractSource(httpElt));
			entryPoint.getPropertyValues().addPropertyValue("realmName", realm);
			entryPointId = pc.getReaderContext().generateBeanName(entryPoint);
			pc.registerBeanComponent(new BeanComponentDefinition(entryPoint, entryPointId));
			basicEntryPoint = new RuntimeBeanReference(entryPointId);
		}

		filterBuilder.addConstructorArgValue(authManager);
		filterBuilder.addConstructorArgValue(basicEntryPoint);
		basicFilter = filterBuilder.getBeanDefinition();
	}

	void createBearerTokenAuthenticationFilter(BeanReference authManager) {
		Element resourceServerElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.OAUTH2_RESOURCE_SERVER);

		if (resourceServerElt == null) {
			// No resource server, do nothing
			return;
		}

		OAuth2ResourceServerBeanDefinitionParser resourceServerBuilder =
				new OAuth2ResourceServerBeanDefinitionParser(authManager, authenticationProviders,
						defaultEntryPointMappings, defaultDeniedHandlerMappings, csrfIgnoreRequestMatchers);
		bearerTokenAuthenticationFilter = resourceServerBuilder.parse(resourceServerElt, pc);
	}

	void createX509Filter(BeanReference authManager) {
		Element x509Elt = DomUtils.getChildElementByTagName(httpElt, Elements.X509);
		RootBeanDefinition filter = null;

		if (x509Elt != null) {
			BeanDefinitionBuilder filterBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(X509AuthenticationFilter.class);
			filterBuilder.getRawBeanDefinition().setSource(pc.extractSource(x509Elt));
			filterBuilder.addPropertyValue("authenticationManager", authManager);

			String regex = x509Elt.getAttribute("subject-principal-regex");

			if (StringUtils.hasText(regex)) {
				BeanDefinitionBuilder extractor = BeanDefinitionBuilder
						.rootBeanDefinition(SubjectDnX509PrincipalExtractor.class);
				extractor.addPropertyValue("subjectDnRegex", regex);

				filterBuilder.addPropertyValue("principalExtractor",
						extractor.getBeanDefinition());
			}

			injectAuthenticationDetailsSource(x509Elt, filterBuilder);

			filter = (RootBeanDefinition) filterBuilder.getBeanDefinition();
			createPrauthEntryPoint(x509Elt);

			createX509Provider();
		}

		x509Filter = filter;
	}

	private void injectAuthenticationDetailsSource(Element elt,
			BeanDefinitionBuilder filterBuilder) {
		String authDetailsSourceRef = elt
				.getAttribute(AuthenticationConfigBuilder.ATT_AUTH_DETAILS_SOURCE_REF);

		if (StringUtils.hasText(authDetailsSourceRef)) {
			filterBuilder.addPropertyReference("authenticationDetailsSource",
					authDetailsSourceRef);
		}
	}

	private void createX509Provider() {
		Element x509Elt = DomUtils.getChildElementByTagName(httpElt, Elements.X509);
		BeanDefinition provider = new RootBeanDefinition(
				PreAuthenticatedAuthenticationProvider.class);

		RootBeanDefinition uds = new RootBeanDefinition();
		uds.setFactoryBeanName(BeanIds.USER_DETAILS_SERVICE_FACTORY);
		uds.setFactoryMethodName("authenticationUserDetailsService");
		uds.getConstructorArgumentValues().addGenericArgumentValue(
				x509Elt.getAttribute(ATT_USER_SERVICE_REF));

		provider.getPropertyValues().addPropertyValue(
				"preAuthenticatedUserDetailsService", uds);

		x509ProviderRef = new RuntimeBeanReference(pc.getReaderContext()
				.registerWithGeneratedName(provider));
	}

	private void createPrauthEntryPoint(Element source) {
		if (preAuthEntryPoint == null) {
			preAuthEntryPoint = new RootBeanDefinition(Http403ForbiddenEntryPoint.class);
			preAuthEntryPoint.setSource(pc.extractSource(source));
		}
	}

	void createJeeFilter(BeanReference authManager) {
		final String ATT_MAPPABLE_ROLES = "mappable-roles";

		Element jeeElt = DomUtils.getChildElementByTagName(httpElt, Elements.JEE);
		RootBeanDefinition filter = null;

		if (jeeElt != null) {
			BeanDefinitionBuilder filterBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(J2eePreAuthenticatedProcessingFilter.class);
			filterBuilder.getRawBeanDefinition().setSource(pc.extractSource(jeeElt));
			filterBuilder.addPropertyValue("authenticationManager", authManager);

			BeanDefinitionBuilder adsBldr = BeanDefinitionBuilder
					.rootBeanDefinition(J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource.class);
			adsBldr.addPropertyValue("userRoles2GrantedAuthoritiesMapper",
					new RootBeanDefinition(
							SimpleAttributes2GrantedAuthoritiesMapper.class));

			String roles = jeeElt.getAttribute(ATT_MAPPABLE_ROLES);
			Assert.hasLength(roles, "roles is expected to have length");
			BeanDefinitionBuilder rolesBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(StringUtils.class);
			rolesBuilder.addConstructorArgValue(roles);
			rolesBuilder.setFactoryMethod("commaDelimitedListToSet");

			RootBeanDefinition mappableRolesRetriever = new RootBeanDefinition(
					SimpleMappableAttributesRetriever.class);
			mappableRolesRetriever.getPropertyValues().addPropertyValue(
					"mappableAttributes", rolesBuilder.getBeanDefinition());
			adsBldr.addPropertyValue("mappableRolesRetriever", mappableRolesRetriever);
			filterBuilder.addPropertyValue("authenticationDetailsSource",
					adsBldr.getBeanDefinition());

			filter = (RootBeanDefinition) filterBuilder.getBeanDefinition();

			createPrauthEntryPoint(jeeElt);
			createJeeProvider();
		}

		jeeFilter = filter;
	}

	private void createJeeProvider() {
		Element jeeElt = DomUtils.getChildElementByTagName(httpElt, Elements.JEE);
		BeanDefinition provider = new RootBeanDefinition(
				PreAuthenticatedAuthenticationProvider.class);

		RootBeanDefinition uds;
		if (StringUtils.hasText(jeeElt.getAttribute(ATT_USER_SERVICE_REF))) {
			uds = new RootBeanDefinition();
			uds.setFactoryBeanName(BeanIds.USER_DETAILS_SERVICE_FACTORY);
			uds.setFactoryMethodName("authenticationUserDetailsService");
			uds.getConstructorArgumentValues().addGenericArgumentValue(
					jeeElt.getAttribute(ATT_USER_SERVICE_REF));
		}
		else {
			uds = new RootBeanDefinition(
					PreAuthenticatedGrantedAuthoritiesUserDetailsService.class);
		}

		provider.getPropertyValues().addPropertyValue(
				"preAuthenticatedUserDetailsService", uds);

		jeeProviderRef = new RuntimeBeanReference(pc.getReaderContext()
				.registerWithGeneratedName(provider));
	}

	void createLoginPageFilterIfNeeded() {
		boolean needLoginPage = formFilterId != null || openIDFilterId != null || oauth2LoginFilterId != null;

		// If no login page has been defined, add in the default page generator.
		if (needLoginPage && formLoginPage == null && openIDLoginPage == null) {
			logger.info("No login page configured. The default internal one will be used. Use the '"
					+ FormLoginBeanDefinitionParser.ATT_LOGIN_PAGE
					+ "' attribute to set the URL of the login page.");
			BeanDefinitionBuilder loginPageFilter = BeanDefinitionBuilder
					.rootBeanDefinition(DefaultLoginPageGeneratingFilter.class);
			loginPageFilter.addPropertyValue("resolveHiddenInputs", new CsrfTokenHiddenInputFunction());

			BeanDefinitionBuilder logoutPageFilter = BeanDefinitionBuilder
					.rootBeanDefinition(DefaultLogoutPageGeneratingFilter.class);
			logoutPageFilter.addPropertyValue("resolveHiddenInputs", new CsrfTokenHiddenInputFunction());

			if (formFilterId != null) {
				loginPageFilter.addConstructorArgReference(formFilterId);
				loginPageFilter.addPropertyValue("authenticationUrl", loginProcessingUrl);
			}

			if (openIDFilterId != null) {
				loginPageFilter.addConstructorArgReference(openIDFilterId);
				loginPageFilter.addPropertyValue("openIDauthenticationUrl",
						openidLoginProcessingUrl);
			}

			if (oauth2LoginFilterId != null) {
				loginPageFilter.addConstructorArgReference(oauth2LoginFilterId);
				loginPageFilter.addPropertyValue("Oauth2LoginEnabled", true);
				loginPageFilter.addPropertyValue("Oauth2AuthenticationUrlToClientName", oauth2LoginLinks);
			}

			loginPageGenerationFilter = loginPageFilter.getBeanDefinition();
			this.logoutPageGenerationFilter = logoutPageFilter.getBeanDefinition();
		}
	}

	void createLogoutFilter() {
		Element logoutElt = DomUtils.getChildElementByTagName(httpElt, Elements.LOGOUT);
		if (logoutElt != null || autoConfig) {
			String formLoginPage = this.formLoginPage;
			if (formLoginPage == null) {
				formLoginPage = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;
			}
			LogoutBeanDefinitionParser logoutParser = new LogoutBeanDefinitionParser(
					formLoginPage, rememberMeServicesId, csrfLogoutHandler);
			logoutFilter = logoutParser.parse(logoutElt, pc);
			logoutHandlers = logoutParser.getLogoutHandlers();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	ManagedList getLogoutHandlers() {
		if (logoutHandlers == null && rememberMeProviderRef != null) {
			logoutHandlers = new ManagedList();
			if (csrfLogoutHandler != null) {
				logoutHandlers.add(csrfLogoutHandler);
			}
			logoutHandlers.add(new RuntimeBeanReference(rememberMeServicesId));
			logoutHandlers
					.add(new RootBeanDefinition(SecurityContextLogoutHandler.class));
		}

		return logoutHandlers;
	}

	BeanMetadataElement getEntryPointBean() {
		return mainEntryPoint;
	}

	BeanMetadataElement getAccessDeniedHandlerBean() {
		return accessDeniedHandler;
	}

	List<BeanDefinition> getCsrfIgnoreRequestMatchers() {
		return csrfIgnoreRequestMatchers;
	}

	void createAnonymousFilter() {
		Element anonymousElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.ANONYMOUS);

		if (anonymousElt != null && "false".equals(anonymousElt.getAttribute("enabled"))) {
			return;
		}

		String grantedAuthority = null;
		String username = null;
		String key = null;
		Object source = pc.extractSource(httpElt);

		if (anonymousElt != null) {
			grantedAuthority = anonymousElt.getAttribute("granted-authority");
			username = anonymousElt.getAttribute("username");
			key = anonymousElt.getAttribute(ATT_KEY);
			source = pc.extractSource(anonymousElt);
		}

		if (!StringUtils.hasText(grantedAuthority)) {
			grantedAuthority = "ROLE_ANONYMOUS";
		}

		if (!StringUtils.hasText(username)) {
			username = "anonymousUser";
		}

		if (!StringUtils.hasText(key)) {
			// Generate a random key for the Anonymous provider
			key = createKey();
		}

		anonymousFilter = new RootBeanDefinition(AnonymousAuthenticationFilter.class);
		anonymousFilter.getConstructorArgumentValues().addIndexedArgumentValue(0, key);
		anonymousFilter.getConstructorArgumentValues().addIndexedArgumentValue(1,
				username);
		anonymousFilter.getConstructorArgumentValues().addIndexedArgumentValue(2,
				AuthorityUtils.commaSeparatedStringToAuthorityList(grantedAuthority));
		anonymousFilter.setSource(source);

		RootBeanDefinition anonymousProviderBean = new RootBeanDefinition(
				AnonymousAuthenticationProvider.class);
		anonymousProviderBean.getConstructorArgumentValues().addIndexedArgumentValue(0,
				key);
		anonymousProviderBean.setSource(anonymousFilter.getSource());
		String id = pc.getReaderContext().generateBeanName(anonymousProviderBean);
		pc.registerBeanComponent(new BeanComponentDefinition(anonymousProviderBean, id));

		anonymousProviderRef = new RuntimeBeanReference(id);

	}

	private String createKey() {
		SecureRandom random = new SecureRandom();
		return Long.toString(random.nextLong());
	}

	void createExceptionTranslationFilter() {
		BeanDefinitionBuilder etfBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(ExceptionTranslationFilter.class);
		accessDeniedHandler = createAccessDeniedHandler(httpElt, pc);
		etfBuilder.addPropertyValue("accessDeniedHandler", accessDeniedHandler);
		assert requestCache != null;
		mainEntryPoint = selectEntryPoint();
		etfBuilder.addConstructorArgValue(mainEntryPoint);
		etfBuilder.addConstructorArgValue(requestCache);

		etf = etfBuilder.getBeanDefinition();
	}

	private BeanMetadataElement createAccessDeniedHandler(Element element,
			ParserContext pc) {
		Element accessDeniedElt = DomUtils.getChildElementByTagName(element,
				Elements.ACCESS_DENIED_HANDLER);
		BeanDefinitionBuilder accessDeniedHandler = BeanDefinitionBuilder
				.rootBeanDefinition(AccessDeniedHandlerImpl.class);

		if (accessDeniedElt != null) {
			String errorPage = accessDeniedElt.getAttribute("error-page");
			String ref = accessDeniedElt.getAttribute("ref");

			if (StringUtils.hasText(errorPage)) {
				if (StringUtils.hasText(ref)) {
					pc.getReaderContext()
							.error("The attribute "
									+ ATT_ACCESS_DENIED_ERROR_PAGE
									+ " cannot be used together with the 'ref' attribute within <"
									+ Elements.ACCESS_DENIED_HANDLER + ">",
									pc.extractSource(accessDeniedElt));

				}
				accessDeniedHandler.addPropertyValue("errorPage", errorPage);
				return accessDeniedHandler.getBeanDefinition();
			}
			else if (StringUtils.hasText(ref)) {
				return new RuntimeBeanReference(ref);
			}

		}

		if (this.defaultDeniedHandlerMappings.isEmpty()) {
			return accessDeniedHandler.getBeanDefinition();
		}
		if (this.defaultDeniedHandlerMappings.size() == 1) {
			return this.defaultDeniedHandlerMappings.values().iterator().next();
		}

		accessDeniedHandler = BeanDefinitionBuilder
				.rootBeanDefinition(RequestMatcherDelegatingAccessDeniedHandler.class);
		accessDeniedHandler.addConstructorArgValue(this.defaultDeniedHandlerMappings);
		accessDeniedHandler.addConstructorArgValue
				(BeanDefinitionBuilder.rootBeanDefinition(AccessDeniedHandlerImpl.class));

		return accessDeniedHandler.getBeanDefinition();
	}

	private BeanMetadataElement selectEntryPoint() {
		// We need to establish the main entry point.
		// First check if a custom entry point bean is set
		String customEntryPoint = httpElt.getAttribute(ATT_ENTRY_POINT_REF);

		if (StringUtils.hasText(customEntryPoint)) {
			return new RuntimeBeanReference(customEntryPoint);
		}

		if (!defaultEntryPointMappings.isEmpty()) {
			if (defaultEntryPointMappings.size() == 1) {
				return defaultEntryPointMappings.values().iterator().next();
			}
			BeanDefinitionBuilder delegatingEntryPoint = BeanDefinitionBuilder
					.rootBeanDefinition(DelegatingAuthenticationEntryPoint.class);
			delegatingEntryPoint.addConstructorArgValue(defaultEntryPointMappings);
			return delegatingEntryPoint.getBeanDefinition();
		}

		Element basicAuthElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.BASIC_AUTH);
		Element formLoginElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.FORM_LOGIN);
		Element openIDLoginElt = DomUtils.getChildElementByTagName(httpElt,
				Elements.OPENID_LOGIN);
		// Basic takes precedence if explicit element is used and no others are configured
		if (basicAuthElt != null && formLoginElt == null && openIDLoginElt == null && oauth2LoginEntryPoint == null) {
			return basicEntryPoint;
		}

		// If formLogin has been enabled either through an element or auto-config, then it
		// is used if no openID login page
		// has been set.

		if (formLoginPage != null && openIDLoginPage != null) {
			pc.getReaderContext().error(
					"Only one login-page can be defined, either for OpenID or form-login, "
							+ "but not both.", pc.extractSource(openIDLoginElt));
		}

		if (formFilterId != null && openIDLoginPage == null) {
			// gh-6802
			// If form login was enabled through element and Oauth2 login was enabled from element then use form login
			if (formLoginElt != null && oauth2LoginEntryPoint != null) {
				return formEntryPoint;
			}
			// If form login was enabled through auto-config, and Oauth2 login was not enabled then use form login
			if (oauth2LoginEntryPoint == null) {
				return formEntryPoint;
			}
		}

		// Otherwise use OpenID if enabled
		if (openIDFilterId != null) {
			return openIDEntryPoint;
		}

		// If X.509 or JEE have been enabled, use the preauth entry point.
		if (preAuthEntryPoint != null) {
			return preAuthEntryPoint;
		}

		// OAuth2 entry point will not be null if only 1 client registration
		if (oauth2LoginEntryPoint != null) {
			return oauth2LoginEntryPoint;
		}

		pc.getReaderContext()
				.error("No AuthenticationEntryPoint could be established. Please "
						+ "make sure you have a login mechanism configured through the namespace (such as form-login) or "
						+ "specify a custom AuthenticationEntryPoint with the '"
						+ ATT_ENTRY_POINT_REF + "' attribute ", pc.extractSource(httpElt));
		return null;
	}

	private void createUserDetailsServiceFactory() {
		if (pc.getRegistry().containsBeanDefinition(BeanIds.USER_DETAILS_SERVICE_FACTORY)) {
			// Multiple <http> case
			return;
		}
		RootBeanDefinition bean = new RootBeanDefinition(
				UserDetailsServiceFactoryBean.class);
		bean.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		pc.registerBeanComponent(new BeanComponentDefinition(bean,
				BeanIds.USER_DETAILS_SERVICE_FACTORY));
	}

	List<OrderDecorator> getFilters() {
		List<OrderDecorator> filters = new ArrayList<>();

		if (anonymousFilter != null) {
			filters.add(new OrderDecorator(anonymousFilter, ANONYMOUS_FILTER));
		}

		if (rememberMeFilter != null) {
			filters.add(new OrderDecorator(rememberMeFilter, REMEMBER_ME_FILTER));
		}

		if (logoutFilter != null) {
			filters.add(new OrderDecorator(logoutFilter, LOGOUT_FILTER));
		}

		if (x509Filter != null) {
			filters.add(new OrderDecorator(x509Filter, X509_FILTER));
		}

		if (jeeFilter != null) {
			filters.add(new OrderDecorator(jeeFilter, PRE_AUTH_FILTER));
		}

		if (formFilterId != null) {
			filters.add(new OrderDecorator(new RuntimeBeanReference(formFilterId),
					FORM_LOGIN_FILTER));
		}

		if (oauth2LoginFilterId != null) {
			filters.add(new OrderDecorator(new RuntimeBeanReference(oauth2LoginFilterId), OAUTH2_LOGIN_FILTER));
			filters.add(new OrderDecorator(oauth2AuthorizationRequestRedirectFilter, OAUTH2_AUTHORIZATION_REQUEST_FILTER));
		}

		if (openIDFilterId != null) {
			filters.add(new OrderDecorator(new RuntimeBeanReference(openIDFilterId),
					OPENID_FILTER));
		}

		if (loginPageGenerationFilter != null) {
			filters.add(new OrderDecorator(loginPageGenerationFilter, LOGIN_PAGE_FILTER));
			filters.add(new OrderDecorator(this.logoutPageGenerationFilter, LOGOUT_PAGE_FILTER));
		}

		if (basicFilter != null) {
			filters.add(new OrderDecorator(basicFilter, BASIC_AUTH_FILTER));
		}

		if (bearerTokenAuthenticationFilter != null) {
			filters.add(new OrderDecorator(bearerTokenAuthenticationFilter, BEARER_TOKEN_AUTH_FILTER));
		}

		if (authorizationCodeGrantFilter != null) {
			filters.add(new OrderDecorator(authorizationRequestRedirectFilter, OAUTH2_AUTHORIZATION_REQUEST_FILTER.getOrder() + 1));
			filters.add(new OrderDecorator(authorizationCodeGrantFilter, OAUTH2_AUTHORIZATION_CODE_GRANT_FILTER));
		}

		filters.add(new OrderDecorator(etf, EXCEPTION_TRANSLATION_FILTER));

		return filters;
	}

	List<BeanReference> getProviders() {
		List<BeanReference> providers = new ArrayList<>();

		if (anonymousProviderRef != null) {
			providers.add(anonymousProviderRef);
		}

		if (rememberMeProviderRef != null) {
			providers.add(rememberMeProviderRef);
		}

		if (openIDProviderRef != null) {
			providers.add(openIDProviderRef);
		}

		if (x509ProviderRef != null) {
			providers.add(x509ProviderRef);
		}

		if (jeeProviderRef != null) {
			providers.add(jeeProviderRef);
		}

		if (oauth2LoginAuthenticationProviderRef != null) {
			providers.add(oauth2LoginAuthenticationProviderRef);
		}

		if (oauth2LoginOidcAuthenticationProviderRef != null) {
			providers.add(oauth2LoginOidcAuthenticationProviderRef);
		}

		if (authorizationCodeAuthenticationProviderRef != null) {
			providers.add(authorizationCodeAuthenticationProviderRef);
		}

		providers.addAll(this.authenticationProviders);

		return providers;
	}

	private static class CsrfTokenHiddenInputFunction implements
		Function<HttpServletRequest, Map<String, String>> {

		@Override
		public Map<String, String> apply(HttpServletRequest request) {
			CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
			if (token == null) {
				return Collections.emptyMap();
			}
			return Collections.singletonMap(token.getParameterName(), token.getToken());
		}
	}
}
