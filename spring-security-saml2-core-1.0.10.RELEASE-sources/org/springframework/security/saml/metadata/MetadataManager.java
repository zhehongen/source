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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.ChainingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataFilter;
import org.opensaml.saml2.metadata.provider.MetadataFilterChain;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ObservableMetadataProvider;
import org.opensaml.saml2.metadata.provider.SignatureValidationFilter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.x509.BasicPKIXValidationInformation;
import org.opensaml.xml.security.x509.BasicX509CredentialNameEvaluator;
import org.opensaml.xml.security.x509.CertPathPKIXValidationOptions;
import org.opensaml.xml.security.x509.PKIXValidationInformation;
import org.opensaml.xml.security.x509.PKIXValidationInformationResolver;
import org.opensaml.xml.security.x509.StaticPKIXValidationInformationResolver;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.trust.AllowAllSignatureTrustEngine;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.util.Assert;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**Class在基础链接MetadataProviders之上提供额外的服务。 Manager跟踪链接的元数据提供程序内部配置的所有可用身份和服务提供程序。 可以确定一个服务提供商为托管主机。
 * Class offers extra services on top of the underlying chaining MetadataProviders. Manager keeps track of all available
 * identity and service providers configured inside the chained metadata providers. Exactly one service provider can
 * be determined as hosted.
 * <p>在内部ReentrantReadWriteLock中使用同步该类。
 * The class is synchronized using in internal ReentrantReadWriteLock.
 * <p>所有元数据提供程序都分为两类-可用的提供程序-包含用户已注册的所有提供程序，以及活动的提供程序-所有通过验证的提供程序。 每次刷新期间都会更新活动提供程序的列表。
 * All metadata providers are kept in two groups - available providers - which contain all the ones users have registered,
 * and active providers - all those which passed validation. List of active providers is updated during each refresh.
 *
 * @author Vladimir Schaefer
 */
public class MetadataManager extends ChainingMetadataProvider implements ExtendedMetadataProvider, InitializingBean, DisposableBean {
//说明：没有继承AbstractReloadingMetadataProvider类。但是他的定时器和AbstractReloadingMetadataProvider的定时器有什么区别和联系？
    // Class logger    AbstractReloadingMetadataProvider定时器发现元数据有改变通知MetadataManager的refreshRequired=ture。MetadataManager的定时器定时扫描发现如果需要更新就更新
    protected final Logger log = LoggerFactory.getLogger(MetadataManager.class);

    // Lock for the instance
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();//多实例咋办？

    // Lock for the refresh mechanism
    private final ReentrantReadWriteLock refreshLock = new ReentrantReadWriteLock();

    private String hostedSPName;//吊毛，啥用 就他奶的sp的entityId

    private String defaultIDP;
//说明：我要避免使用这东西。因为MetadataManager是单例的。  this.defaultExtendedMetadata = new ExtendedMetadata();永远不会为空。
    private ExtendedMetadata defaultExtendedMetadata;//为啥又弄个默认扩展元数据？实际就是公共扩展元数据配置

    // Timer used to refresh the metadata upon changes
    private Timer timer;//说明：思路很清晰，每十秒检查一下

    // Internal of metadata refresh checking in ms
    private long refreshCheckInterval = 10000l;

    // Flag indicating whether metadata needs to be reloaded
    private boolean refreshRequired = true;

    // Storage for cryptographic data used to verify metadata signatures存储用于验证元数据签名的密码数据
    protected KeyManager keyManager;

    // All providers which were added, not all may be active已添加的所有提供程序，可能不是全部都处于活动状态。什么叫活动状态？
    private List<ExtendedMetadataDelegate> availableProviders;//说明：限定只能是ExtendedMetadataDelegate

    /**吊毛
     * Set of IDP names available in the system.
     */
    private Set<String> idpName;

    /**系统中可用的一组SP名称。难道是为了支持多租户？
     * Set of SP names available in the system.
     */
    private Set<String> spName;

    /**什么玩意？sp的别名集？
     * All valid aliases.
     */
    private Set<String> aliasSet;//说明：配置的本地元数据别名集合

    /**创建新的元数据管理器，自动注册自己以接收来自元数据更改的通知，并在更改时调用reload。 还注册计时器以验证是否需要在指定的时间间隔内重新加载元数据。构造之后必须调用afterPropertiesSet方法。
     * Creates new metadata manager, automatically registers itself for notifications from metadata changes and calls
     * reload upon a change. Also registers timer which verifies whether metadata needs to be reloaded in a specified
     * time interval.
     * <p>
     * It is mandatory that method afterPropertiesSet is called after the construction.
     *
     * @param providers providers to include, mustn't be null or empty
     * @throws MetadataProviderException error during initialization
     */
    public MetadataManager(List<MetadataProvider> providers) throws MetadataProviderException {

        super();

        this.idpName = new HashSet<String>();
        this.spName = new HashSet<String>();
        this.defaultExtendedMetadata = new ExtendedMetadata();//说明：不能理解就仅仅实例化一个ExtendedMetadata就行？那不全是默认值？
        availableProviders = new LinkedList<ExtendedMetadataDelegate>();

        setProviders(providers);
        getObservers().add(new MetadataProviderObserver());//说明：增加一个观察者

    }

    /**在提供程序构造之后必须调用方法。 它创建刷新计时器并第一次刷新元数据。
     * Method must be called after provider construction. It creates the refresh timer and refreshes the metadata for
     * the first time.
     *
     * @throws MetadataProviderException error
     */
    public final void afterPropertiesSet() throws MetadataProviderException {

        Assert.notNull(keyManager, "KeyManager must be set");

        // Create timer if needed
        if (refreshCheckInterval > 0) {//看过了
            log.debug("Creating metadata reload timer with interval {}", refreshCheckInterval);
            this.timer = new Timer("Metadata-reload", true);
            this.timer.schedule(new RefreshTask(), refreshCheckInterval, refreshCheckInterval);
        } else {
            log.debug("Metadata reload timer is not created, refreshCheckInternal is {}", refreshCheckInterval);
        }

        refreshMetadata();

    }

    /**
     * Stops and removes the timer in case it was started. Cleans all metadata objects.
     */
    public void destroy() {

        try {

            refreshLock.writeLock().lock();
            lock.writeLock().lock();

            for (MetadataProvider provider : getProviders()) {
                if (provider instanceof ExtendedMetadataDelegate) {
                    ((ExtendedMetadataDelegate) provider).destroy();
                }
            }

            super.destroy();

            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }

            // Workaround for Tomcat detection of terminated threads,Tomcat检测终止线程的解决方法
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }

            setRefreshRequired(false);

        } finally {

            lock.writeLock().unlock();
            refreshLock.writeLock().unlock();

        }

    }
//说明：沃日你妹啊，这孙子有可能可以不断更新这个列表啊。在程序启动的时候，通过数据库获取到所有的信息，构建provider列表
    @Override
    public void setProviders(List<MetadataProvider> newProviders) throws MetadataProviderException {

        try {

            lock.writeLock().lock();

            availableProviders.clear();
            if (newProviders != null) {
                for (MetadataProvider provider : newProviders) {
                    addMetadataProvider(provider);
                }
            }

        } finally {

            lock.writeLock().unlock();

        }

        setRefreshRequired(true);

    }

    /**可以重复调用Method以浏览所有已配置的提供程序并加载它们支持的SP和IDP名称。 初始化期间失败的提供程序将被忽略以进行此刷新。
     * Method can be repeatedly called to browse all configured providers and load SP and IDP names which
     * are supported by them. Providers which fail during initialization are ignored for this refresh.
     */
    public void refreshMetadata() {
//说明：只能全量刷新
        log.debug("Reloading metadata");

        try {

            // Let's load new metadata lists
            lock.writeLock().lock();

            // Remove existing providers, they'll get repopulated删除现有的提供商，他们将被重新填充
            super.setProviders(Collections.<MetadataProvider>emptyList());

            // Reinitialize the sets
            idpName = new HashSet<String>();
            spName = new HashSet<String>();
            aliasSet = new HashSet<String>();

            for (ExtendedMetadataDelegate provider : availableProviders) {

                try {

                    log.debug("Refreshing metadata provider {}", provider);
                    initializeProviderFilters(provider);//在provider原有过滤器的基础上增加一个签名验证过滤器
                    initializeProvider(provider);//说明：调用的还是最具体的provider的初始化方法。初始化完成之后能 XMLObject getMetadata()
                    initializeProviderData(provider);////说明：将解析出来的entityId分别存入idpName或spName集合。将将从extendmetadata从解析出来的所有alias全部存入aliasSet

                    // Make provider available for queries
                    super.addMetadataProvider(provider);
                    log.debug("Metadata provider was initialized {}", provider);

                } catch (MetadataProviderException e) {

                    log.error("Initialization of metadata provider {} failed, provider will be ignored", provider, e);

                }

            }

            log.debug("Reloading metadata was finished");

        } catch (MetadataProviderException e) {

            throw new RuntimeException("Error clearing existing providers");

        } finally {

            lock.writeLock().unlock();

        }

    }

    /**
     * Determines whether metadata requires refresh and if so clears the flag.
     *
     * @return true in case refresh should be performed
     */
    private boolean isRefreshNowAndClear() {

        try {
            //防止任何人在重新加载过程中更改刷新状态，以避免丢失调用
            // Prevent anyone from changing the refresh status during reload to avoid missed calls
            refreshLock.writeLock().lock();

            // Make sure refresh is really necessary
            if (!isRefreshRequired()) {
                log.debug("Refresh is not required, isRefreshRequired flag isn't set");
                return false;
            }

            // Clear the refresh flag
            setRefreshRequired(false);

        } finally {

            refreshLock.writeLock().unlock();

        }

        return true;

    }

    /**将新的元数据提供程序添加到托管列表。 首先，仅注册提供者，并且将在下一轮元数据刷新或调用refreshMetadata时对提供者进行验证。除非提供者已经扩展了ExtendedMetadataDelegate类，否则它将作为添加的一部分自动包装在其中。
     * Adds a new metadata provider to the managed list. At first provider is only registered and will be validated
     * upon next round of metadata refreshing or call to refreshMetadata.
     * <p>
     * Unless provider already extends class ExtendedMetadataDelegate it will be automatically wrapped in it as part of the
     * addition.
     *
     * @param newProvider provider
     * @throws MetadataProviderException in case provider can't be added
     */
    @Override
    public void addMetadataProvider(MetadataProvider newProvider) throws MetadataProviderException {
//说明：从数据库热加载吧。或者前期provider比较少，每次都全量扫描也行
        if (newProvider == null) {
            throw new IllegalArgumentException("Null provider can't be added");
        }
//说明：添加也仅仅是添加到可用列表，想变成激活状态，还得等到下一次刷新的时候验证通过才能用。真正的激活provider列表还是放在父类ChainingMetadataProvider中
        try {

            lock.writeLock().lock();//说明：写锁

            ExtendedMetadataDelegate wrappedProvider = getWrappedProvider(newProvider);
            availableProviders.add(wrappedProvider);

        } finally {
            lock.writeLock().unlock();
        }

        setRefreshRequired(true);//是吗

    }

    /**从可用性列表中删除现有的元数据提供程序。 下次管理员刷新时，提供程序将被完全删除。
     * Removes existing metadata provider from the availability list. Provider will be completely removed
     * during next manager refresh.
     *
     * @param provider provider to remove
     */
    @Override
    public void removeMetadataProvider(MetadataProvider provider) {//看过了

        if (provider == null) {
            throw new IllegalArgumentException("Null provider can't be removed");
        }

        try {

            lock.writeLock().lock();

            ExtendedMetadataDelegate wrappedProvider = getWrappedProvider(provider);
            availableProviders.remove(wrappedProvider);

        } finally {
            lock.writeLock().unlock();
        }

        setRefreshRequired(true);

    }

    /**方法提供了活动的提供程序的列表-有效的提供程序列表，可以查询这些提供程序以获取元数据。 返回值是一个副本。
     * Method provides list of active providers - those which are valid and can be queried for metadata. Returned
     * value is a copy.
     *
     * @return active providers
     */
    public List<MetadataProvider> getProviders() {

        try {
            lock.readLock().lock();
            return new ArrayList<MetadataProvider>(super.getProviders());//说明：没看出来是副本
        } finally {
            lock.readLock().unlock();
        }

    }

    /**方法提供所有可用提供者的列表。 如果验证失败，则可能无法使用所有这些提供程序。 返回值是数据的副本。
     * Method provides list of all available providers. Not all of these providers may be used in case their validation failed.
     * Returned value is a copy of the data.
     *
     * @return all available providers
     */
    public List<ExtendedMetadataDelegate> getAvailableProviders() {

        try {
            lock.readLock().lock();
            return new ArrayList<ExtendedMetadataDelegate>(availableProviders);
        } finally {
            lock.readLock().unlock();
        }

    }

    private ExtendedMetadataDelegate getWrappedProvider(MetadataProvider provider) {//看过了
        if (!(provider instanceof ExtendedMetadataDelegate)) {
            log.debug("Wrapping metadata provider {} with extendedMetadataDelegate", provider);
            return new ExtendedMetadataDelegate(provider);//说明：这样构造出来的ExtendedMetadataDelegate的ExtendedMetadata是直接new出来的
        } else {
            return (ExtendedMetadataDelegate) provider;
        }
    }

    /**期望使用方法来确保提供程序已正确初始化。 同样，所有加载的过滤器都应应用。
     * Method is expected to make sure that the provider is properly initialized. Also all loaded filters should get
     * applied.
     *
     * @param provider provider to initialize
     * @throws MetadataProviderException error
     */
    protected void initializeProvider(ExtendedMetadataDelegate provider) throws MetadataProviderException {

        // Initialize provider and perform signature verification
        log.debug("Initializing extendedMetadataDelegate {}", provider);
        provider.initialize();//看过了

    }

    /**方法填充IDP和SP名称的本地存储，并验证可能出现的任何名称冲突。
     * Method populates local storage of IDP and SP names and verifies any name conflicts which might arise.
     *
     * @param provider provider to initialize
     * @throws MetadataProviderException error
     */
    protected void initializeProviderData(ExtendedMetadataDelegate provider) throws MetadataProviderException {
//说明：将解析出来的entityId分别存入idpName或spName集合。将将从extendmetadata从解析出来的所有alias全部存入aliasSet
        log.debug("Initializing provider data {}", provider);

        List<String> stringSet = parseProvider(provider);//说明：entityid列表，XMLObject object = provider.getMetadata();//说明：获取到元数据

        for (String key : stringSet) {
            //看过了
            RoleDescriptor idpRoleDescriptor = provider.getRole(key, IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS);

            if (idpRoleDescriptor != null) {
                if (idpName.contains(key)) {//说明：entityid冲突校验
                    log.warn("Provider {} contains entity {} with IDP which was already contained in another metadata provider and will be ignored", provider, key);
                } else {
                    idpName.add(key);
                }
            }

            RoleDescriptor spRoleDescriptor = provider.getRole(key, SPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS);
            if (spRoleDescriptor != null) {
                if (spName.contains(key)) {
                    log.warn("Provider {} contains entity {} which was already included in another metadata provider and will be ignored", provider, key);
                } else {
                    spName.add(key);
                }
            }
//说明： if (provider instanceof ExtendedMetadataProvider) {//真奇葩，即使他又是她
            // Verify extended metadata
            ExtendedMetadata extendedMetadata = getExtendedMetadata(key, provider);

            if (extendedMetadata != null) {

                if (extendedMetadata.isLocal()) {

                    // Parse alias
                    String alias = extendedMetadata.getAlias();
                    if (alias != null) {

                        // Verify alias is valid
                        SAMLUtil.verifyAlias(alias, key);//说明：别名是非空字符串，仅包含ASCII字符

                        // Verify alias is unique
                        if (aliasSet.contains(alias)) {
                        //说明：别名必须唯一
                            log.warn("Provider {} contains alias {} which is not unique and will be ignored", provider, alias);

                        } else {

                            aliasSet.add(alias);
                            log.debug("Local entity {} available under alias {}", key, alias);

                        }

                    } else {

                        log.debug("Local entity {} doesn't have an alias", key);

                    }

                    // Set default local SP
                    if (spRoleDescriptor != null && getHostedSPName() == null) {//getHostedSPName() == null
                        setHostedSPName(key);//说明：在这里进行了初始化操作。
                    }

                } else {

                    log.debug("Remote entity {} available", key);

                }

            } else {

                log.debug("No extended metadata available for entity {}", key);

            }

        }

    }

    /**在每次尝试初始化提供程序数据时都会自动调用方法。 它期望加载元数据验证所需的所有过滤器。 还必须确保在调用此方法后可以使用元数据提供程序。每个提供程序必须扩展AbstractMetadataProvider或具有ExtendedMetadataDelegate类型。 默认情况下，SignatureValidationFilter与任何现有过滤器一起添加。
     * Method is automatically called during each attempt to initialize the provider data. It expects to load
     * all filters required for metadata verification. It must also be ensured that metadata provider is ready to be used
     * after call to this method.
     * <p>
     * Each provider must extend AbstractMetadataProvider or be of ExtendedMetadataDelegate type.
     * <p>
     * By default a SignatureValidationFilter is added together with any existing filters.
     *
     * @param provider provider to check
     * @throws MetadataProviderException in case initialization fails
     */
    protected void initializeProviderFilters(ExtendedMetadataDelegate provider) throws MetadataProviderException {//说明：初始化过滤器列表
//说明：在provider原有过滤器的基础上增加一个签名验证过滤器
        if (provider.isTrustFiltersInitialized()) {

            log.debug("Metadata provider was already initialized, signature filter initialization will be skipped");

        } else {

            boolean requireSignature = provider.isMetadataRequireSignature();
            SignatureTrustEngine trustEngine = getTrustEngine(provider);//看过了
            SignatureValidationFilter filter = new SignatureValidationFilter(trustEngine);
            filter.setRequireSignature(requireSignature);

            log.debug("Created new trust manager for metadata provider {}", provider);

            MetadataFilter currentFilter = provider.getMetadataFilter();
            if (currentFilter != null) {
                log.debug("Adding signature filter before existing filters");
                MetadataFilterChain chain = new MetadataFilterChain();//看过了
                chain.setFilters(Arrays.asList(filter, currentFilter));
                provider.setMetadataFilter(chain);
            } else {
                log.debug("Adding signature filter");
                provider.setMetadataFilter(filter);//说明：只有多个过滤器的时候才用chain封装？
            }

            provider.setTrustFiltersInitialized(true);

        }

    }

    /**预期方法会创建一个信任引擎，用于验证来自此提供程序的签名。
     * Method is expected to create a trust engine used to verify signatures from this provider.
     *
     * @param provider provider to create engine for
     * @return trust engine or null to skip trust verification
     */
    protected SignatureTrustEngine getTrustEngine(MetadataProvider provider) {//说明：就是创建一个信任引擎，用来验证签名

        Set<String> trustedKeys = null;
        boolean verifyTrust = true;
        boolean forceRevocationCheck = false;

        if (provider instanceof ExtendedMetadataDelegate) {
            ExtendedMetadataDelegate metadata = (ExtendedMetadataDelegate) provider;
            trustedKeys = metadata.getMetadataTrustedKeys();//说明：没设置，但是后续应该会用到。每个ipd只信任自己的公钥
            verifyTrust = metadata.isMetadataTrustCheck();//说明：一般会设置，为true
            forceRevocationCheck = metadata.isForceMetadataRevocationCheck();//说明：一般不设置，默认为false
        }

        if (verifyTrust) {

            log.debug("Setting trust verification for metadata provider {}", provider);

            CertPathPKIXValidationOptions pkixOptions = new CertPathPKIXValidationOptions();

            if (forceRevocationCheck) {
                log.debug("Revocation checking forced to true");
                pkixOptions.setForceRevocationEnabled(true);
            } else {
                log.debug("Revocation checking not forced");
                pkixOptions.setForceRevocationEnabled(false);
            }

            return new PKIXSignatureTrustEngine(
                    getPKIXResolver(provider, trustedKeys, null),
                    Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver(),
                    new org.springframework.security.saml.trust.CertPathPKIXTrustEvaluator(pkixOptions),
                    new BasicX509CredentialNameEvaluator());

        } else {

            log.debug("Trust verification skipped for metadata provider {}", provider);
            return new AllowAllSignatureTrustEngine(Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());

        }

    }

    /**期望该方法构造（具有（可用于给定提供者的）所有受信任数据的）信息解析器。好复杂
     * Method is expected to construct information resolver with all trusted data available for the given provider.
     *
     * @param provider     provider
     * @param trustedKeys  trusted keys for the providers
     * @param trustedNames trusted names for the providers (always null)
     * @return information resolver
     */
    protected PKIXValidationInformationResolver getPKIXResolver(MetadataProvider provider, Set<String> trustedKeys, Set<String> trustedNames) {
//说明：还算好理解吧。根据trustKey获取到所有x509证书。然后构建一个resolver
        // Use all available keys
        if (trustedKeys == null) {
            trustedKeys = keyManager.getAvailableCredentials();
        }

        // Resolve allowed certificates to build the anchors解析允许的证书以构建锚
        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (String key : trustedKeys) {
            log.debug("Adding PKIX trust anchor {} for metadata verification of provider {}", key, provider);
            X509Certificate certificate = keyManager.getCertificate(key);
            if (certificate != null) {
                certificates.add(certificate);
            } else {
                log.warn("Cannot construct PKIX trust anchor for key with alias {} for provider {}, key isn't included in the keystore", key, provider);
            }
        }

        List<PKIXValidationInformation> info = new LinkedList<PKIXValidationInformation>();
        info.add(new BasicPKIXValidationInformation(certificates, null, 4));
        return new StaticPKIXValidationInformationResolver(info, trustedNames) {
            @Override
            public Set<String> resolveTrustedNames(CriteriaSet criteriaSet)//说明：为了处理isEmpty这种情况
                throws SecurityException, UnsupportedOperationException {
                Set<String> names = super.resolveTrustedNames(criteriaSet);
                //previous implementation returned true
                //if trustedNames was empty(), not just null
                //https://git.shibboleth.net/view/?p=java-xmltooling.git;a=commitdiff;h=c3c19e4857b815c7c05fa3b675f9cd1adde43429#patch2
                if (names.isEmpty()) {
                    return null;
                } else {
                    return names;
                }
            }
        };

    }

    /**解析提供者，并返回提供者内部包含的一组entityID。
     * Parses the provider and returns set of entityIDs contained inside the provider.
     *
     * @param provider provider to parse
     * @return set of entityIDs available in the provider
     * @throws MetadataProviderException error
     */
    protected List<String> parseProvider(MetadataProvider provider) throws MetadataProviderException {

        List<String> result = new LinkedList<String>();

        XMLObject object = provider.getMetadata();//说明：获取到元数据
        if (object instanceof EntityDescriptor) {
            addDescriptor(result, (EntityDescriptor) object);//说明：奇葩
        } else if (object instanceof EntitiesDescriptor) {
            addDescriptors(result, (EntitiesDescriptor) object);
        }

        return result;

    }

    /**递归解析描述符对象。 同时支持嵌套的entityDescriptor元素和叶entityDescriptor。 找到的所有描述符的EntityID将添加到结果集中。 使用给定的策略和信任引擎来验证所有找到的实体上的签名
     * Recursively parses descriptors object. Supports both nested entitiesDescriptor
     * elements and leaf entityDescriptors. EntityID of all found descriptors are added
     * to the result set. Signatures on all found entities are verified using the given policy
     * and trust engine.
     *
     * @param result      result set of parsed entity IDs
     * @param descriptors descriptors to parse
     * @throws MetadataProviderException in case signature validation fails
     */
    private void addDescriptors(List<String> result, EntitiesDescriptor descriptors) throws MetadataProviderException {
//说明：很容易理解
        log.debug("Found metadata EntitiesDescriptor with ID {}", descriptors.getID());

        if (descriptors.getEntitiesDescriptors() != null) {
            for (EntitiesDescriptor descriptor : descriptors.getEntitiesDescriptors()) {
                addDescriptors(result, descriptor);
            }
        }
        if (descriptors.getEntityDescriptors() != null) {
            for (EntityDescriptor descriptor : descriptors.getEntityDescriptors()) {
                addDescriptor(result, descriptor);
            }
        }

    }

    /**解析描述符中的entityID，并将其添加到结果集中。 使用给定的策略和信任引擎来验证所有找到的实体上的签名。
     * Parses entityID from the descriptor and adds it to the result set.  Signatures on all found entities
     * are verified using the given policy and trust engine.
     *
     * @param result     result set
     * @param descriptor descriptor to parse
     * @throws MetadataProviderException in case signature validation fails
     */
    private void addDescriptor(List<String> result, EntityDescriptor descriptor) throws MetadataProviderException {
//说明：简单
        String entityID = descriptor.getEntityID();
        log.debug("Found metadata EntityDescriptor with ID {}", entityID);
        result.add(entityID);

    }

    /**
     * Returns set of names of all IDPs available in the metadata
     *
     * @return set of entityID names
     */
    public Set<String> getIDPEntityNames() {
        try {//说明：这个集合永远不会被修改，所以我们不需要在这里克隆，只需要确保我们得到了正确的实例。
            lock.readLock().lock();
            // The set is never modified so we don't need to clone here, only make sure we get the right instance.
            return Collections.unmodifiableSet(idpName);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns set of names of all SPs entity names
     *
     * @return set of SP entity names available in the metadata
     */
    public Set<String> getSPEntityNames() {
        try {
            lock.readLock().lock();
            // The set is never modified so we don't need to clone here, only make sure we get the right instance.
            return Collections.unmodifiableSet(spName);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param idpID name of IDP to check
     * @return true if IDP entity ID is in the circle of trust with our entity
     */
    public boolean isIDPValid(String idpID) {
        try {
            lock.readLock().lock();
            return idpName.contains(idpID);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**要检查的SP的实体ID
     * @param spID entity ID of SP to check如果给定的SP实体ID在信任圈内有效，则为true
     * @return true if given SP entity ID is valid in circle of trust
     */
    public boolean isSPValid(String spID) {
        try {
            lock.readLock().lock();
            return spName.contains(spID);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**该方法返回运行此应用程序的SP的名称。 该名称可以通过调用元数据过滤器从spring上下文自动设置。
     * The method returns name of SP running this application. This name is either set from spring
     * context of automatically by invoking of the metadata filter.
     *什么名称？托管的？服务提供商名称，没听说过。下面说了nameid
     * @return name of hosted SP metadata which can be returned by call to getEntityDescriptor.可以通过调用getEntityDescriptor返回的托管SP元数据的名称。
     */
    public String getHostedSPName() {
        return hostedSPName;//说明：不太明白，sp entityid
    }

    /**设置此计算机上托管的SP的nameID。 这可以从springContext调用，也可以在元数据生成过滤器调用期间自动调用。
     * Sets nameID of SP hosted on this machine. This can either be called from springContext or
     * automatically during invocation of metadata generation filter.
     *描述此计算机上托管的SP的元数据名称
     * @param hostedSPName name of metadata describing SP hosted on this machine
     */
    public void setHostedSPName(String hostedSPName) {
        this.hostedSPName = hostedSPName;
    }

    /**吊毛
     * Returns entity ID of the IDP to be used by default. In case the defaultIDP property has been set
     * it is returned. Otherwise first available IDP in IDP list is used.
     *
     * @return entity ID of IDP to use
     * @throws MetadataProviderException in case IDP can't be determined
     */
    public String getDefaultIDP() throws MetadataProviderException {
//说明：这个方法可能需要自定义
        try {

            lock.readLock().lock();

            if (defaultIDP != null) {//说明：肯定是空的
                return defaultIDP;//说明：我用不了默认idp。因为此类是单例的。只有一个默认idp。所以此方法我应该用不到
            } else {
                Iterator<String> iterator = getIDPEntityNames().iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    throw new MetadataProviderException("No IDP was configured, please update included metadata with at least one IDP");
                }
            }

        } finally {

            lock.readLock().unlock();

        }

    }

    /**
     * Sets name of IDP to be used as default.
     *
     * @param defaultIDP IDP to set as default
     */
    public void setDefaultIDP(String defaultIDP) {
        this.defaultIDP = defaultIDP;
    }

    /**尝试通过依次尝试一个提供程序来定位ExtendedMetadata。 仅考虑实现ExtendedMetadataProvider的提供程序。如果所有提供程序都不能提供扩展版本，则使用默认版本。始终返回内部表示的副本，修改返回的对象将不会反映在后续调用中。
     * Tries to locate ExtendedMetadata by trying one provider after another. Only providers implementing
     * ExtendedMetadataProvider are considered.
     * <p>
     * In case none of the providers can supply the extended version, the default is used.
     * <p>
     * A copy of the internal representation is always returned, modifying the returned object will not be reflected
     * in the subsequent calls.
     *
     * @param entityID entity ID to load extended metadata for
     * @return extended metadata or defaults
     * @throws MetadataProviderException never thrown
     */
    public ExtendedMetadata getExtendedMetadata(String entityID) throws MetadataProviderException {

        try {
        //说明：本质就是找到第一个ExtendedMetadataDelegate。然后拿出来他配置的ExtendedMetadata。这他奶的悲催了
            lock.readLock().lock();
//说明：这方法需要改写。确实需要改写
            for (MetadataProvider provider : getProviders()) {
                ExtendedMetadata extendedMetadata = getExtendedMetadata(entityID, provider);
                if (extendedMetadata != null) {
                    return extendedMetadata;
                }
            }

            return getDefaultExtendedMetadata().clone();

        } finally {

            lock.readLock().unlock();

        }

    }

    private ExtendedMetadata getExtendedMetadata(String entityID, MetadataProvider provider) throws MetadataProviderException {
        if (provider instanceof ExtendedMetadataProvider) {//真奇葩，即使他又是她
            ExtendedMetadataProvider extendedProvider = (ExtendedMetadataProvider) provider;
            ExtendedMetadata extendedMetadata = extendedProvider.getExtendedMetadata(entityID);//说明：普通的get方法
            if (extendedMetadata != null) {
                return extendedMetadata.clone();
            }
        }
        return null;
    }

    /**找到其entityId SHA-1哈希等于参数中的实体描述符的实体描述符。
     * Locates entity descriptor whose entityId SHA-1 hash equals the one in the parameter.
     *
     * @param hash hash of the entity descriptor
     * @return found descriptor or null
     * @throws MetadataProviderException in case metadata required for processing can't be loaded
     */
    public EntityDescriptor getEntityDescriptor(byte[] hash) throws MetadataProviderException {
//说明：entityid必须全局唯一
        try {

            lock.readLock().lock();

            for (String idp : idpName) {
                if (SAMLUtil.compare(hash, idp)) {
                    return getEntityDescriptor(idp);
                }
            }

            for (String sp : spName) {
                if (SAMLUtil.compare(hash, sp)) {
                    return getEntityDescriptor(sp);
                }
            }

            return null;

        } finally {

            lock.readLock().unlock();

        }

    }

    /**尝试加载具有给定别名的实体的entityId。 如果在系统中配置了两个具有相同别名的实体，则失败。
     * Tries to load entityId for entity with the given alias. Fails in case two entities with the same alias
     * are configured in the system.
     *
     * @param entityAlias alias to locate id for
     * @return entity id for the given alias or null if none exists
     * @throws MetadataProviderException in case two entity have the same non-null alias
     */
    public String getEntityIdForAlias(String entityAlias) throws MetadataProviderException {
//说明：遍历所有ipd和sp的entityid列表。找到他的扩展元数据，然后和entityAlias比较，如果相等则返回
        try {

            lock.readLock().lock();

            if (entityAlias == null) {
                return null;
            }

            String entityId = null;

            for (String idp : idpName) {
                ExtendedMetadata extendedMetadata = getExtendedMetadata(idp);//说明：取出来难道不是同一个ExtendedMetadata吗？
                if (extendedMetadata.isLocal() && entityAlias.equals(extendedMetadata.getAlias())) {
                    if (entityId != null && !entityId.equals(idp)) {//两个idp有相同的alias,就会进入此
                        throw new MetadataProviderException("Alias " + entityAlias + " is used both for entity " + entityId + " and " + idp);
                    } else {
                        entityId = idp;
                    }
                }
            }

            for (String sp : spName) {
                ExtendedMetadata extendedMetadata = getExtendedMetadata(sp);
                if (extendedMetadata.isLocal() && entityAlias.equals(extendedMetadata.getAlias())) {
                    if (entityId != null && !entityId.equals(sp)) {
                        throw new MetadataProviderException("Alias " + entityAlias + " is used both for entity " + entityId + " and " + sp);
                    } else {
                        entityId = sp;
                    }
                }
            }

            return entityId;

        } finally {

            lock.readLock().unlock();

        }

    }

    /**
     * @return default extended metadata to be used in case no entity specific version exists, never null
     */
    public ExtendedMetadata getDefaultExtendedMetadata() {
        try {
            lock.readLock().lock();
            return defaultExtendedMetadata;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**吊毛
     * Sets default extended metadata to be used in case no version specific is available.
     *
     * @param defaultExtendedMetadata metadata, RuntimeException when null
     */
    public void setDefaultExtendedMetadata(ExtendedMetadata defaultExtendedMetadata) {
        Assert.notNull(defaultExtendedMetadata, "ExtendedMetadata parameter mustn't be null");
        lock.writeLock().lock();//说明：没用到
        this.defaultExtendedMetadata = defaultExtendedMetadata;
        lock.writeLock().unlock();
    }

    /**
     * Flag indicating whether configuration of the metadata should be reloaded.
     *
     * @return true if reload is required
     */
    public boolean isRefreshRequired() {
        try {
            refreshLock.readLock().lock();
            return refreshRequired;
        } finally {
            refreshLock.readLock().unlock();
        }
    }

    /**指示应在提供程序配置已更改时重新加载元数据。 使用单独的锁定机制允许设置元数据刷新标志，而不会中断现有的读取器。
     * Indicates that the metadata should be reloaded as the provider configuration has changed.
     * Uses a separate locking mechanism to allow setting metadata refresh flag without interrupting existing readers.
     *
     * @param refreshRequired true if refresh is required
     */
    public void setRefreshRequired(boolean refreshRequired) {
        try {
            refreshLock.writeLock().lock();
            this.refreshRequired = refreshRequired;
        } finally {
            refreshLock.writeLock().unlock();
        }
    }


    /**用于重新验证元数据及其重新加载的时间间隔（以毫秒为单位）。 触发后，将要求每个提供程序返回其元数据，这可能会触发其重新加载。 如果重新加载元数据，则会通知管理器，并通过调用refreshMetadata自动刷新所有内部数据。如果该值小于零，则不会创建计时器。 默认值为10000l。该值只能在调用afterBeanPropertiesSet之前进行修改，此后将不应用更改。
     * Interval in milliseconds used for re-verification of metadata and their reload. Upon trigger each provider
     * is asked to return it's metadata, which might trigger their reloading. In case metadata is reloaded the manager
     * is notified and automatically refreshes all internal data by calling refreshMetadata.
     * <p>
     * In case the value is smaller than zero the timer is not created. The default value is 10000l.
     * <p>
     * The value can only be modified before the call to the afterBeanPropertiesSet, the changes are not applied after that.
     *
     * @param refreshCheckInterval internal, timer not created if &lt;= 2000
     */
    public void setRefreshCheckInterval(long refreshCheckInterval) {
        this.refreshCheckInterval = refreshCheckInterval;
    }

    /**
     * Task used to refresh the metadata when required.
     */
    private class RefreshTask extends TimerTask {

        @Override
        public void run() {

            try {

                log.trace("Executing metadata refresh task");
                //说明：调用getMetadata会在需要时执行刷新，潜在的昂贵操作，但是其他线程仍可以加载现有的缓存数据
                // Invoking getMetadata performs a refresh in case it's needed
                // Potentially expensive operation, but other threads can still load existing cached data
                for (MetadataProvider provider : getProviders()) {//说明：不懂有何意义
                    provider.getMetadata();//说明：这是什么操作?
                }

                // Refresh the metadataManager if needed
                if (isRefreshRequired()) {
                    if (isRefreshNowAndClear()) {
                        refreshMetadata();
                    }
                }

            } catch (Throwable e) {
                log.warn("Metadata refreshing has failed", e);
            }

        }

    }

    /**观察者，它根据来自任何提供者的任何通知清除缓存。
     * Observer which clears the cache upon any notification from any provider.
     */
    private class MetadataProviderObserver implements ObservableMetadataProvider.Observer {

        /**
         * {@inheritDoc}
         */
        public void onEvent(MetadataProvider provider) {
            setRefreshRequired(true);//说明：设置标志位需要刷新了
        }

    }

    @Autowired
    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Autowired(required = false)
    public void setTLSConfigurer(TLSProtocolConfigurer configurer) {
        // Only explicit dependency仅显式依赖
    }

}
