/* Copyright 2009-2011 Vladimir Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.saml;

import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.URLBuilder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**类从SP端初始化SAML WebSSO配置文件，IDP发现或ECP配置文件。 本地服务提供商和传入请求的配置确定将调用哪个配置文件。可以通过两种方式调用入口点。 任一用户访问配置为要求某种程度的身份验证的URL，并引发AuthenticationException，该异常将被处理并调用入口点。 另一种方法是通过访问/ saml / login URL直接调用入口点。
 * Class initializes SAML WebSSO Profile, IDP Discovery or ECP Profile from the SP side. Configuration
 * of the local service provider and incoming request determines which profile will get invoked.
 * <p>
 * There are two ways the entry point can get invoked. Either user accesses a URL configured to require
 * some degree of authentication and throws AuthenticationException which is handled and invokes the entry point.
 * The other way is direct invocation of the entry point by accessing the /saml/login URL.
 *
 * @author Vladimir Schaefer
 */
public class SAMLEntryPoint extends GenericFilterBean implements AuthenticationEntryPoint {
//说明：
    protected static final Logger log = LoggerFactory.getLogger(SAMLEntryPoint.class);

    protected WebSSOProfileOptions defaultOptions;//说明：设置的。可以影响到AuthnRequest。
    protected WebSSOProfile webSSOprofile;//说明：@Qualifier("webSSOprofile") 自动注入  void sendAuthenticationRequest(SAMLMessageContext context, WebSSOProfileOptions options)
    protected WebSSOProfile webSSOprofileECP;//说明：@Qualifier("ecpprofile")
    protected WebSSOProfile webSSOprofileHoK;//说明：@Qualifier("hokWebSSOProfile")
    protected MetadataManager metadata;//说明：@Autowired
    protected SAMLLogger samlLogger;//说明： @Autowired
    protected SAMLContextProvider contextProvider;//说明： @Autowired
    protected SAMLDiscovery samlDiscovery;//说明： @Autowired(required = false)

    /**
     * Url this filter should get activated on.
     */
    protected String filterProcessesUrl = FILTER_URL;

    /**
     * Default name of path suffix which will invoke this filter.
     */
    public static final String FILTER_URL = "/saml/login";

    /**
     * Name of parameter of HttpRequest telling entry point that the login should use specified idp.
     */
    public static final String IDP_PARAMETER = "idp";

    /**
     * Parameter is used to indicate response from IDP discovery service. When present IDP discovery is not invoked
     * again.
     */
    public static final String DISCOVERY_RESPONSE_PARAMETER = "disco";

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        FilterInvocation fi = new FilterInvocation(request, response, chain);

        if (!processFilter(fi.getRequest())) {
            chain.doFilter(request, response);
            return;
        }

        commence(fi.getRequest(), fi.getResponse(), null);

    }

    /**如果请求的URL包含DEFAULT_FILTER_URL，则将使用该过滤器。
     * The filter will be used in case the URL of the request contains the DEFAULT_FILTER_URL.
     *
     * @param request request used to determine whether to enable this filter用于确定是否启用此过滤器的请求
     * @return true if this filter should be used
     */
    protected boolean processFilter(HttpServletRequest request) {
        return SAMLUtil.processFilter(filterProcessesUrl, request);
    }

    /**方法启动一个过程，该过程用于最终使用WebSSO配置文件对用户进行身份验证。该机制的首要任务是确定要使用的IDP。可用选项包括：让用户代理为我们确定IDP（ECP配置文件），使用IDP发现确定IDP（或在请求中接受预定义IDP）或使用默认IDP。以下逻辑用于确定我们的情况：如果在contextProvider中未确定IDP且启用了发现并且当前请求尚未包含IDP信息，则将初始化IDP发现。如果请求根据SAML规范支持增强的客户端或代理，并且支持ECP，则可以使用ECP初始化身份验证。如果IDP可用，则WebSSO或HoKWebSSO初始化，否则我们将在SSO初始化期间失败。默认情况下，contextProvider通过参数“ idp”确定要使用的IDP。如果缺少参数，则使用defaultIDP代替。类可以自定义WebSSO初始化行为。
     * Method starts a process used to ultimately authenticate user using WebSSO Profile. First task of the mechanism
     * is to determine which IDP to use. Available options are: let the user agent determine IDP for us (ECP profile), use IDP discovery
     * to determine IDP (or accept a predefined IDP in request), or use the default IDP. The following logic is used to determine our case:
     * <br>
     * <ul>
     * <li>In case IDP wasn't determined in contextProvider and discovery is enabled and the current request doesn't already contain IDP information then IDP Discovery is initialized</li>
     * <li>In case request supports Enhanced Client or Proxy as per SAML specification and ECP is supported authentication is initialized using ECP.</li>
     * <li>In case IDP is available WebSSO or HoKWebSSO is initialized otherwise we fail during SSO initialization.</li>
     * </ul>
     * <p>
     * By default contextProvider determines IDP to use by parameter "idp". In case parameter is missing the defaultIDP is used instead.
     * <p>
     * Subclasses can customize the WebSSO initialization behavior.
     *
     * @param request  request
     * @param response response
     * @param e        exception causing this entry point to be invoked or null when EntryPoint is invoked directly
     * @throws IOException      error sending response
     * @throws ServletException error initializing SAML protocol
     */
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        //说明：该机制的首要任务是确定要使用的IDP
        try {

            SAMLMessageContext context = contextProvider.getLocalAndPeerEntity(request, response);

            if (isECP(context)) {
                initializeECP(context, e);
            } else if (isDiscovery(context)) {
                initializeDiscovery(context);
            } else {
                initializeSSO(context, e);
            }

        } catch (SAMLException e1) {
            log.debug("Error initializing entry point", e1);
            throw new ServletException(e1);
        } catch (MetadataProviderException e1) {
            log.debug("Error initializing entry point", e1);
            throw new ServletException(e1);
        } catch (MessageEncodingException e1) {
            log.debug("Error initializing entry point", e1);
            throw new ServletException(e1);
        }

    }

    /**
     * Initializes ECP profile.
     * <p>
     * Subclasses can alter the initialization behaviour.
     *
     * @param context saml context, also containing wrapped request and response objects
     * @param e       exception causing the entry point to be invoked (if any)
     * @throws MetadataProviderException in case metadata can't be queried
     * @throws SAMLException             in case message sending fails
     * @throws MessageEncodingException  in case SAML message encoding fails
     */
    protected void initializeECP(SAMLMessageContext context, AuthenticationException e) throws MetadataProviderException, SAMLException, MessageEncodingException {
//看过了
        WebSSOProfileOptions options = getProfileOptions(context, e);

        log.debug("Processing SSO using ECP profile");
        webSSOprofileECP.sendAuthenticationRequest(context, options);
        samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);

    }

    /**WebSSO配置文件或WebSSO密钥持有者配置文件。根据服务提供商的设置进行选择。如果启用了增强型客户端/代理，并且请求声明支持此配置文件，则使用该配置文件。否则，将验证为WebSSOProfileOptions中的assertionConsumerIndex指定的绑定和配置文件是什么。如果是HoK，则使用WebSSO密钥持有者配置文件，否则使用普通的WebSSO。
     * WebSSO profile or WebSSO Holder-of-Key profile. Selection is made based on the settings of the Service Provider.
     * In case Enhanced Client/Proxy is enabled and the request claims to support this profile it is used. Otherwise it is verified what is the binding
     * and profile specified for the assertionConsumerIndex in the WebSSOProfileOptions. In case it is HoK the WebSSO Holder-of-Key profile is used,
     * otherwise the ordinary WebSSO.
     * <p>
     * Subclasses can alter the initialization behaviour.
     *
     * @param context saml context, also containing wrapped request and response objects
     * @param e       exception causing the entry point to be invoked (if any)
     * @throws MetadataProviderException in case metadata can't be queried
     * @throws SAMLException             in case message sending fails
     * @throws MessageEncodingException  in case SAML message encoding fails
     */
    protected void initializeSSO(SAMLMessageContext context, AuthenticationException e) throws MetadataProviderException, SAMLException, MessageEncodingException {

        // Generate options for the current SSO request
        WebSSOProfileOptions options = getProfileOptions(context, e);

        // Determine the assertionConsumerService to be used
        AssertionConsumerService consumerService = SAMLUtil.getConsumerService((SPSSODescriptor) context.getLocalEntityRoleMetadata(), options.getAssertionConsumerIndex());

        // HoK WebSSO ---------"urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser"
        if (SAMLConstants.SAML2_HOK_WEBSSO_PROFILE_URI.equals(consumerService.getBinding())) {
            if (webSSOprofileHoK == null) {
                log.warn("WebSSO HoK profile was specified to be used, but profile is not configured in the EntryPoint, HoK will be skipped");
            } else {
                log.debug("Processing SSO using WebSSO HolderOfKey profile");
                webSSOprofileHoK.sendAuthenticationRequest(context, options);
                samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);
                return;
            }
        }

        // Ordinary WebSSO
        log.debug("Processing SSO using WebSSO profile");
        webSSOprofile.sendAuthenticationRequest(context, options);
        samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);

    }

    /**方法初始化https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-idp-discovery.pdf中定义的IDP发现配置文件假定本地服务提供商的元数据包含发现返回 地址。
     * Method initializes IDP Discovery Profile as defined in https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-idp-discovery.pdf
     * It is presumed that metadata of the local Service Provider contains discovery return address.
     *
     * @param context saml context also containing request and response objects
     * @throws ServletException          error
     * @throws IOException               io error
     * @throws MetadataProviderException in case metadata of the local entity can't be populated
     */
    protected void initializeDiscovery(SAMLMessageContext context) throws ServletException, IOException, MetadataProviderException {

        String discoveryURL = context.getLocalExtendedMetadata().getIdpDiscoveryURL();

        if (discoveryURL != null) {

            URLBuilder urlBuilder = new URLBuilder(discoveryURL);
            List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();//"entityID"
            queryParams.add(new Pair<String, String>(SAMLDiscovery.ENTITY_ID_PARAM, context.getLocalEntityId()));
            queryParams.add(new Pair<String, String>(SAMLDiscovery.RETURN_ID_PARAM, IDP_PARAMETER));//"returnIDParam=idp"
            discoveryURL = urlBuilder.buildURL();

            log.debug("Using discovery URL from extended metadata");

        } else {

            String discoveryUrl = SAMLDiscovery.FILTER_URL;//说明："/saml/discovery"
            if (samlDiscovery != null) {
                discoveryUrl = samlDiscovery.getFilterProcessesUrl();
            }

            String contextPath = (String) context.getInboundMessageTransport().getAttribute(SAMLConstants.LOCAL_CONTEXT_PATH);//"localContextPath"
            discoveryURL = contextPath + discoveryUrl + "?" + SAMLDiscovery.RETURN_ID_PARAM + "=" + IDP_PARAMETER + "&" + SAMLDiscovery.ENTITY_ID_PARAM + "=" + context.getLocalEntityId();

            log.debug("Using local discovery URL");

        }

        log.debug("Redirecting to discovery URL {}", discoveryURL);
        HTTPOutTransport response = (HTTPOutTransport) context.getOutboundMessageTransport();
        response.sendRedirect(discoveryURL);

    }

    /**方法应该填充用于构造SAML消息的首选项。 可以重写方法以提供适合给定应用程序的逻辑。 如果设置了defaultOptions对象，它将用作构造的基础，并且将更新请求的特定值（idp字段）。
     * Method is supposed to populate preferences used to construct the SAML message. Method can be overridden to provide
     * logic appropriate for given application. In case defaultOptions object was set it will be used as basis for construction
     * and request specific values will be update (idp field).
     *
     * @param context   containing local entity
     * @param exception exception causing invocation of this entry point (can be null)
     * @return populated webSSOprofile
     * @throws MetadataProviderException in case metadata loading fails
     */
    protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) throws MetadataProviderException {
        //说明：入参倒是有可能使用
        WebSSOProfileOptions ssoProfileOptions;
        if (defaultOptions != null) {
            ssoProfileOptions = defaultOptions.clone();
        } else {
            ssoProfileOptions = new WebSSOProfileOptions();
        }

        return ssoProfileOptions;

    }

    /**
     * Sets object which determines default values to be used as basis for construction during getProfileOptions call.
     *
     * @param defaultOptions default object to use for options construction
     */
    public void setDefaultProfileOptions(WebSSOProfileOptions defaultOptions) {//看过了
        if (defaultOptions != null) {
            this.defaultOptions = defaultOptions.clone();
        } else {
            this.defaultOptions = null;
        }
    }

    /**确定是否应初始化IDP发现。 默认情况下，上下文中不得存在用户选择的IDP，必须启用IDP发现，并且该请求不得为IDP发现的响应，以使该方法返回true。
     * Determines whether IDP Discovery should be initialized. By default no user-selected IDP must be present in the context,
     * IDP Discovery must be enabled and the request mustn't be a response from IDP Discovery in order for the method
     * to return true.
     *
     * @param context context
     * @return true if IDP Discovery should get initialized
     */
    protected boolean isDiscovery(SAMLMessageContext context) {
        return !context.isPeerUserSelected() && context.getLocalExtendedMetadata().isIdpDiscoveryEnabled() && !isDiscoResponse(context);
    }//说明：idp=****就代表是用户选择的idp。不是用户选择的才走idp发现。所以增加参数及可禁用发现功能。很容易实现

    /**确定是否应初始化ECP配置文件。默认情况下，当请求声明对ECP的支持并且当前服务提供者允许ECP时，使用ECP。如果启用了ECP，但未设置WebsProfileECP，则会记录警告，并且不使用ECP。
     * Determines whether ECP profile should get initialized. By default ECP is used when request declares supports for ECP
     * and ECP is allowed for the current service provider. In case ECP is enabled but webSSOprofileECP wasn't set a warning
     * is logged and ECP is not used.
     *
     * @param context context
     * @return true if ECP profile should get initialized
     */
    protected boolean isECP(SAMLMessageContext context) {//说明：使HttpServletRequest适应HTTPInTransport。
        HttpServletRequest request = ((HttpServletRequestAdapter) context.getInboundMessageTransport()).getWrappedRequest();
        boolean ecp = context.getLocalExtendedMetadata().isEcpEnabled() && SAMLUtil.isECPRequest(request);//说明：主要根据请求头来判断
        if (ecp) {
            if (webSSOprofileECP == null) {
                log.warn("ECP profile was specified to be used, but profile is not configured in the EntryPoint, ECP will be skipped");
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * True value indicates that request is a response from the discovery profile. We use the value to
     * prevent repeated invocation of the discovery service upon failure.
     *
     * @param context context with request and response included
     * @return true if this HttpRequest is a response from IDP discovery profile.
     */
    private boolean isDiscoResponse(SAMLMessageContext context) {
        HTTPInTransport request = (HTTPInTransport) context.getInboundMessageTransport();
        String disco = request.getParameterValue(DISCOVERY_RESPONSE_PARAMETER);//说明："disco=true"
        return (disco != null && disco.toLowerCase().trim().equals("true"));
    }

    /**
     * Profile for consumption of processed messages, cannot be null, must be set.
     *
     * @param webSSOprofile profile
     */
    @Autowired
    @Qualifier("webSSOprofile")
    public void setWebSSOprofile(WebSSOProfile webSSOprofile) {
        Assert.notNull(webSSOprofile, "WebSSOPRofile can't be null");
        this.webSSOprofile = webSSOprofile;
    }

    @Autowired(required = false)
    @Qualifier("ecpprofile")
    public void setWebSSOprofileECP(WebSSOProfile webSSOprofileECP) {
        this.webSSOprofileECP = webSSOprofileECP;
    }

    @Autowired(required = false)
    @Qualifier("hokWebSSOProfile")
    public void setWebSSOprofileHoK(WebSSOProfile webSSOprofileHoK) {
        this.webSSOprofileHoK = webSSOprofileHoK;
    }

    /**
     * Logger for SAML events, cannot be null, must be set.
     *
     * @param samlLogger logger
     */
    @Autowired
    public void setSamlLogger(SAMLLogger samlLogger) {
        Assert.notNull(samlLogger, "SAML Logger can't be null");
        this.samlLogger = samlLogger;
    }

    /**
     * Dependency for loading of discovery URL
     * @param samlDiscovery saml discovery endpoint
     */
    @Autowired(required = false)
    public void setSamlDiscovery(SAMLDiscovery samlDiscovery) {
        this.samlDiscovery = samlDiscovery;
    }

    /**
     * Sets entity responsible for populating local entity context data.
     *
     * @param contextProvider provider implementation
     */
    @Autowired
    public void setContextProvider(SAMLContextProvider contextProvider) {
        Assert.notNull(contextProvider, "Context provider can't be null");
        this.contextProvider = contextProvider;
    }

    /**
     * Metadata manager, cannot be null, must be set.
     *
     * @param metadata manager
     */
    @Autowired
    public void setMetadata(MetadataManager metadata) {
        Assert.notNull(metadata, "MetadataManager can't be null");
        this.metadata = metadata;
    }

    /**
     * @return filter URL
     */
    public String getFilterProcessesUrl() {
        return filterProcessesUrl;// /saml/login
    }

    /**
     * Custom filter URL which overrides the default. Filter url determines URL where filter starts processing.
     *
     * @param filterProcessesUrl filter URL
     */
    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }

    /**
     * Verifies that required entities were autowired or set.
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        Assert.notNull(webSSOprofile, "WebSSO profile must be set");
        Assert.notNull(metadata, "Metadata must be set");
        Assert.notNull(samlLogger, "Logger must be set");
        Assert.notNull(contextProvider, "Context provider must be set");
    }

}
