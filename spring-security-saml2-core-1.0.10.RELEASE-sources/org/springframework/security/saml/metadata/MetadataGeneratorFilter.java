/* Copyright 2009 Vladimir Schäfer
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
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.SimpleURLCanonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**过滤器期望在已配置的URL上进行调用，并向用户提供代表此应用程序部署的SAML2元数据。 如果应用程序配置为自动生成元数据，则生成将在首次调用此过滤器（向服务器发出的第一个请求）时发生。
 * The filter expects calls on configured URL and presents user with SAML2 metadata representing
 * this application deployment. In case the application is configured to automatically generate metadata,
 * the generation occurs upon first invocation of this filter (first request made to the server).
 *
 * @author Vladimir Schäfer
 */
public class MetadataGeneratorFilter extends GenericFilterBean {//没看明白咋用的，就是数据初始化？

    /**
     * Class logger.
     */
    protected static final Logger log = LoggerFactory.getLogger(MetadataGeneratorFilter.class);

    /**
     * Class storing all SAML metadata documents存储所有SAML元数据文档。这是真的吗，为啥要他存储所有元数据文档
     */
    protected MetadataManager manager;// 自动注入

    /**
     * Class capable of generating new metadata.产生新的元数据
     */
    protected MetadataGenerator generator;

    /**
     * Metadata display filter.至于吗
     */
    protected MetadataDisplayFilter displayFilter;//几乎没用到。自动注入

    /**标志指示在使用生成的基本url的情况下（当MetadataGenerator中未提供值时），应对其进行规范化。 规范化包括方案和服务器名称的小写字母，并删除http方案的80个标准端口和https方案的443个标准端口。
     * Flag indicates that in case generated base url is used (when value is not provided in the MetadataGenerator)
     * it should be normalized. Normalization includes lower-casing of scheme and server name and removing standar
     * ports of 80 for http and 443 for https schemes.
     */
    protected boolean normalizeBaseUrl;

    /**
     * Default alias for generated entities.默认别名？奇葩，似乎没用到
     */
    private static final String DEFAULT_ALIAS = "defaultAlias";

    /**
     * Default constructor.
     *
     * @param generator generator
     */
    public MetadataGeneratorFilter(MetadataGenerator generator) {
        this.generator = generator;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        processMetadataInitialization((HttpServletRequest) request);
        chain.doFilter(request, response);
    }

    /**验证是否需要生成，以及是否需要生成元数据文档并将其存储在元数据管理器中。
     * Verifies whether generation is needed and if so the metadata document is created and stored in metadata
     * manager.
     *
     * @param request request
     * @throws javax.servlet.ServletException error
     */
    protected void processMetadataInitialization(HttpServletRequest request) throws ServletException {

        // In case the hosted SP metadata weren't initialized, let's do it now如果未初始化托管的SP元数据，请立即进行操作
        if (manager.getHostedSPName() == null) {//sp nameid?

            synchronized (MetadataManager.class) {

                if (manager.getHostedSPName() == null) {

                    try {

                        log.info("No default metadata configured, generating with default values, please pre-configure metadata for production use");

                        // Defaults
                        String alias = generator.getEntityAlias();//extendedMetadata.getAlias()
                        String baseURL = getDefaultBaseURL(request);

                        // Use default baseURL if not set如果未设置，则使用默认baseURL
                        if (generator.getEntityBaseURL() == null) {
                            log.warn("Generated default entity base URL {} based on values in the first server request. Please set property entityBaseURL on MetadataGenerator bean to fixate the value.", baseURL);
                            generator.setEntityBaseURL(baseURL);
                        } else {
                            baseURL = generator.getEntityBaseURL();//说明：肯定会设置
                        }

                        // Use default entityID if not set
                        if (generator.getEntityId() == null) {
                            generator.setEntityId(getDefaultEntityID(baseURL, alias));
                        }

                        EntityDescriptor descriptor = generator.generateMetadata();
                        ExtendedMetadata extendedMetadata = generator.generateExtendedMetadata();

                        log.info("Created default metadata for system with entityID: " + descriptor.getEntityID());
                        MetadataMemoryProvider memoryProvider = new MetadataMemoryProvider(descriptor);
                        memoryProvider.initialize();
                        MetadataProvider metadataProvider = new ExtendedMetadataDelegate(memoryProvider, extendedMetadata);

                        manager.addMetadataProvider(metadataProvider);//说明：加入MetadataManager
                        manager.setHostedSPName(descriptor.getEntityID());
                        manager.refreshMetadata();

                    } catch (MetadataProviderException e) {
                        log.error("Error generating system metadata", e);
                        throw new ServletException("Error generating system metadata", e);
                    }

                }

            }

        }

    }

    protected String getDefaultEntityID(String entityBaseUrl, String alias) {

        String displayFilterUrl = MetadataDisplayFilter.FILTER_URL;// /saml/metadata
        if (displayFilter != null) {
            displayFilterUrl = displayFilter.getFilterProcessesUrl();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(entityBaseUrl);
        sb.append(displayFilterUrl);

        if (StringUtils.hasLength(alias)) {
            sb.append("/alias/");
            sb.append(alias);
        }

        return sb.toString();

    }

    protected String getDefaultBaseURL(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
        sb.append(request.getContextPath());
        String url = sb.toString();
        if (isNormalizeBaseUrl()) {
            return SimpleURLCanonicalizer.canonicalize(url);
        } else {
            return url;
        }
    }

    @Autowired(required = false)
    public void setDisplayFilter(MetadataDisplayFilter displayFilter) {
        this.displayFilter = displayFilter;
    }

    @Autowired
    public void setManager(MetadataManager manager) {
        this.manager = manager;
    }

    public boolean isNormalizeBaseUrl() {
        return normalizeBaseUrl;
    }

    /**
     * When true flag indicates that in case generated base url is used (when value is not provided in the MetadataGenerator)
     * it should be normalized. Normalization includes lower-casing of scheme and server name and removing standar
     * ports of 80 for http and 443 for https schemes.
     *
     * @param normalizeBaseUrl flag
     */
    public void setNormalizeBaseUrl(boolean normalizeBaseUrl) {
        this.normalizeBaseUrl = normalizeBaseUrl;
    }

    /**
     * Verifies that required entities were autowired or set.
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        Assert.notNull(generator, "Metadata generator");
        Assert.notNull(manager, "MetadataManager must be set");
    }

}
