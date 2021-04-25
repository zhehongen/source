/* Copyright 2009-2011 Vladimir Schäfer
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
package org.springframework.security.saml.metadata;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.util.SAMLUtil;
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
import java.io.PrintWriter;

/**过滤器期望在已配置的URL上进行调用，并向用户提供代表此应用程序部署的SAML2元数据。 如果应用程序配置为自动生成元数据，则生成将在首次调用此过滤器（向服务器发出的第一个请求）时发生。
 * The filter expects calls on configured URL and presents user with SAML2 metadata representing
 * this application deployment. In case the application is configured to automatically generate metadata,
 * the generation occurs upon first invocation of this filter (first request made to the server).
 *
 * @author Vladimir Schäfer
 */
public class MetadataDisplayFilter extends GenericFilterBean {//说明：就是下载元数据的过滤器

    /**
     * Class logger.
     */
    protected static final Logger log = LoggerFactory.getLogger(MetadataDisplayFilter.class);

    /**
     * Class storing all SAML metadata documents
     */
    protected MetadataManager manager;//说明：自动注入

    /**
     * Key manager for metadata signatures
     */
    protected KeyManager keyManager;//说明：自动注入

    /**基于URL的上下文提供者
     * Provider for context based on URL
     */
    protected SAMLContextProvider contextProvider;//说明：自动注入

    /**
     * Url this filter should get activated on.
     */
    protected String filterProcessesUrl = FILTER_URL;

    /**
     * Default name of path suffix which will invoke this filter.
     */
    public static final String FILTER_URL = "/saml/metadata";//可以理解，就是下载

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        FilterInvocation fi = new FilterInvocation(request, response, chain);

        if (!processFilter(fi.getRequest())) {
            chain.doFilter(request, response);
            return;
        }

        processMetadataDisplay(fi.getRequest(), fi.getResponse());

    }

    /**如果请求的URL包含FILTER_URL，则将使用该过滤器。
     * The filter will be used in case the URL of the request contains the FILTER_URL.
     *
     * @param request request used to determine whether to enable this filter
     * @return true if this filter should be used
     */
    protected boolean processFilter(HttpServletRequest request) {
        return SAMLUtil.processFilter(filterProcessesUrl, request);
    }

    /**过滤器尝试生成应用程序元数据（如果已配置），并且在对预期URL进行调用的情况下，将显示元数据值，并且不再调用其他过滤器。 否则，过滤器链调用将继续。
     * The filter attempts to generate application metadata (if configured so) and in case the call is made
     * to the expected URL the metadata value is displayed and no further filters are invoked. Otherwise
     * filter chain invocation continues.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException error
     * @throws java.io.IOException            io error
     */
    protected void processMetadataDisplay(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            SAMLMessageContext context = contextProvider.getLocalEntity(request, response);//说明：
            String entityId = context.getLocalEntityId();
            response.setContentType("application/samlmetadata+xml"); // SAML_Meta, 4.1.1 - line 1235
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment; filename=\"spring_saml_metadata.xml\"");
            displayMetadata(entityId, response.getWriter());//说明：response.getWriter()
        } catch (MetadataProviderException e) {
            throw new ServletException("Error initializing metadata", e);
        }
    }

    /**
     * Method writes metadata document into given writer object.
     *
     * @param spEntityName id of entity to display metadata for
     * @param writer       output for metadata
     * @throws ServletException error retrieving or writing the metadata
     */
    protected void displayMetadata(String spEntityName, PrintWriter writer) throws ServletException {
        try {
            EntityDescriptor descriptor = manager.getEntityDescriptor(spEntityName);//说明：
            if (descriptor == null) {
                throw new ServletException("Metadata entity with ID " + manager.getHostedSPName() + " wasn't found");
            } else {
                writer.print(getMetadataAsString(descriptor));
            }
        } catch (MarshallingException e) {
            log.error("Error marshalling entity descriptor", e);
            throw new ServletException(e);
        } catch (MetadataProviderException e) {
            log.error("Error retrieving metadata", e);
            throw new ServletException("Error retrieving metadata", e);
        }
    }

    protected String getMetadataAsString(EntityDescriptor descriptor) throws MarshallingException {
        return SAMLUtil.getMetadataAsString(manager, keyManager , descriptor, null);
    }

    @Autowired
    public void setManager(MetadataManager manager) {
        this.manager = manager;
    }

    @Autowired
    public void setContextProvider(SAMLContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Autowired
    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * @return filter URL
     */
    public String getFilterProcessesUrl() {
        return filterProcessesUrl;
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
        Assert.notNull(manager, "MetadataManager must be set");
        Assert.notNull(keyManager, "KeyManager must be set");
        Assert.notNull(contextProvider, "Context provider must be set");
    }

}
