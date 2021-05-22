/*
 * Copyright 2011 Vladimir Schaefer
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
package org.springframework.security.saml.context;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.ws.security.ServletRequestX509CredentialAdapter;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.encryption.ChainingEncryptedKeyResolver;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.trust.ExplicitX509CertificateTrustEngine;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.*;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.storage.HttpSessionStorageFactory;
import org.springframework.security.saml.storage.SAMLMessageStorageFactory;
import org.springframework.security.saml.trust.CertPathPKIXTrustEvaluator;
import org.springframework.security.saml.trust.PKIXInformationResolver;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.util.Assert;

import javax.net.ssl.HostnameVerifier;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**类负责解析HttpRequest / Response并确定哪个本地实体（IDP / SP）负责其处理。
 * Class is responsible for parsing HttpRequest/Response and determining which local entity (IDP/SP) is responsible
 * for its handling.
 *
 * @author Vladimir Schaefer
 */
public class SAMLContextProviderImpl implements SAMLContextProvider, InitializingBean {
//说明：Selection of the correct Service Provider instance based on URL is performed inside SAMLContextProviderImpl class.
    protected static final Logger log = LoggerFactory.getLogger(SAMLContextProviderImpl.class);

    // Way to obtain encrypted key info from XML Encryption从XML加密获取加密密钥信息的方法
    private static ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver();

    static {
        encryptedKeyResolver.getResolverChain().add(new InlineEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new EncryptedElementTypeEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new SimpleRetrievalMethodEncryptedKeyResolver());
    }

    protected KeyManager keyManager;
    protected MetadataManager metadata;
    protected MetadataCredentialResolver metadataResolver;//解析凭据？
    protected PKIXValidationInformationResolver pkixResolver;
    protected PKIXTrustEvaluator pkixTrustEvaluator;//使用PKIX验证规则针对一组受信任的PKIXValidationInformation评估X509Credential
    protected SAMLMessageStorageFactory storageFactory = new HttpSessionStorageFactory();

    /**
     * Creates a SAMLContext with local entity values filled. Also request and response must be stored in the context
     * as message transports.
     *
     * @param request  request
     * @param response response
     * @return context
     * @throws MetadataProviderException in case of metadata problems
     */
    public SAMLMessageContext getLocalEntity(HttpServletRequest request, HttpServletResponse response) throws MetadataProviderException {

        SAMLMessageContext context = new SAMLMessageContext();
        populateGenericContext(request, response, context);//说明：填充 元数据管理器，请求，响应，消息存储方式
        populateLocalEntityId(context, request.getRequestURI());//说明：填充LocalEntityId，LocalEntityRole
        populateLocalContext(context);//看过了
        return context;

    }

    /**创建一个具有本地实体和对等值填充的SAMLContext。 同样，请求和响应也必须作为消息传输存储在上下文中。 当可以从请求中确定本地实体和对等实体时，应使用该属性。
     * Creates a SAMLContext with local entity and peer values filled. Also request and response must be stored in the context
     * as message transports. Should be used when both local entity and peer entity can be determined from the request.
     *
     * @param request  request
     * @param response response
     * @return context
     * @throws MetadataProviderException in case of metadata problems
     */
    public SAMLMessageContext getLocalAndPeerEntity(HttpServletRequest request, HttpServletResponse response) throws MetadataProviderException {

        SAMLMessageContext context = new SAMLMessageContext();
        populateGenericContext(request, response, context);//说明：填充 元数据管理器，请求，响应，消息存储方式
        populateLocalEntityId(context, request.getRequestURI());//说明：填充LocalEntityId，LocalEntityRole
        populateLocalContext(context);
        populatePeerEntityId(context);//说明：填充PeerEntityId，PeerEntityRole
        populatePeerContext(context);
        return context;

    }

    /**首先尝试从请求属性中找到预配置的IDP。 如果未找到，则从请求中加载IDP_PARAMETER；如果不为null，则在我们的信任圈中验证具有该值的IDP是否为有效的IDP。 IDP无效时处理失败。 在上下文中将IDP设置为PeerEntityId。如果request参数为null，则返回默认IDP。
     * First tries to find pre-configured IDP from the request attribute. If not found
     * loads the IDP_PARAMETER from the request and if it is not null verifies whether IDP with this value is valid
     * IDP in our circle of trust. Processing fails when IDP is not valid. IDP is set as PeerEntityId in the context.
     * <p>
     * If request parameter is null the default IDP is returned.
     *
     * @param context context to populate ID for
     * @throws MetadataProviderException in case provided IDP value is invalid
     */
    protected void populatePeerEntityId(SAMLMessageContext context) throws MetadataProviderException {

        HTTPInTransport inTransport = (HTTPInTransport) context.getInboundMessageTransport();
        String entityId;
                                                                                        //说明："peerEntityId"
        entityId = (String) inTransport.getAttribute(org.springframework.security.saml.SAMLConstants.PEER_ENTITY_ID);
        if (entityId != null) { // Pre-configured entity Id
            log.debug("Using protocol specified IDP {}", entityId);
        } else {
            entityId = inTransport.getParameterValue(SAMLEntryPoint.IDP_PARAMETER);//说明：参数："idp"
            if (entityId != null) { // IDP from request
                log.debug("Using user specified IDP {} from request", entityId);
                context.setPeerUserSelected(true);
            } else { // Default IDP
                entityId = metadata.getDefaultIDP();//看过了
                log.debug("No IDP specified, using default {}", entityId);
                context.setPeerUserSelected(false);
            }
        }
//说明：填充PeerEntityId，PeerEntityRole
        context.setPeerEntityId(entityId);
        context.setPeerEntityRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

    }

    /**根据先前加载的peerEntityId填充有关对等方的其他信息。
     * Populates additional information about the peer based on the previously loaded peerEntityId.
     *
     * @param samlContext to populate
     * @throws MetadataProviderException in case metadata problem is encountered
     */
    protected void populatePeerContext(SAMLMessageContext samlContext) throws MetadataProviderException {
//看过了
        String peerEntityId = samlContext.getPeerEntityId();
        QName peerEntityRole = samlContext.getPeerEntityRole();

        if (peerEntityId == null) {
            throw new MetadataProviderException("Peer entity ID wasn't specified, but is requested");
        }

        EntityDescriptor entityDescriptor = metadata.getEntityDescriptor(peerEntityId);
        RoleDescriptor roleDescriptor = metadata.getRole(peerEntityId, peerEntityRole, SAMLConstants.SAML20P_NS);
        ExtendedMetadata extendedMetadata = metadata.getExtendedMetadata(peerEntityId);

        if (entityDescriptor == null || roleDescriptor == null) {
            throw new MetadataProviderException("Metadata for entity " + peerEntityId + " and role " + peerEntityRole + " wasn't found");
        }

        samlContext.setPeerEntityMetadata(entityDescriptor);
        samlContext.setPeerEntityRoleMetadata(roleDescriptor);
        samlContext.setPeerExtendedMetadata(extendedMetadata);


    }
//说明：填充 元数据管理器，请求，响应，消息存储方式
    protected void populateGenericContext(HttpServletRequest request, HttpServletResponse response, SAMLMessageContext context) throws MetadataProviderException {

        HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(request);
        HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, request.isSecure());

        // Store attribute which cannot be located from InTransport directly 存储属性
        request.setAttribute(org.springframework.security.saml.SAMLConstants.LOCAL_CONTEXT_PATH, request.getContextPath());
                                                                            //说明："localContextPath"
        context.setMetadataProvider(metadata);
        context.setInboundMessageTransport(inTransport);
        context.setOutboundMessageTransport(outTransport);

        context.setMessageStorage(storageFactory.getMessageStorage(request));//存入httpsession

    }

    protected void populateLocalContext(SAMLMessageContext context) throws MetadataProviderException {

        populateLocalEntity(context);//说明：填充LocalEntityMetadata，LocalEntityRoleMetadata，LocalExtendedMetadata，LocalSigningCredential
        populateDecrypter(context);//看过了
        populateSSLCredential(context);//本地ssl私钥？
        populatePeerSSLCredential(context);//看不懂啊
        populateTrustEngine(context);//pkix or metaiop 签名用
        populateSSLTrustEngine(context); //pkix or metaiop ssl用
        populateSSLHostnameVerifier(context);//default

    }

    /**方法尝试从请求路径加载localEntityAlias和localEntityRole。 路径应采用以下格式：https(s)://server:port/application/saml/filterName/alias/aliasName/idp|sp?query。 如果路径中缺少别名，则使用默认值。 否则，将localEntityId和sp或idp localEntityRole输入到上下文中。如果未找到别名实体ID，则会引发异常。
     * Method tries to load localEntityAlias and localEntityRole from the request path. Path is supposed to be in format:
     * https(s)://server:port/application/saml/filterName/alias/aliasName/idp|sp?query. In case alias is missing from
     * the path defaults are used. Otherwise localEntityId and sp or idp localEntityRole is entered into the context.
     * <p>
     * In case alias entity id isn't found an exception is raised.
     *
     * @param context     context to populate fields localEntityId and localEntityRole for用于填充字段localEntityId和localEntityRole的上下文
     * @param requestURI context path to parse entityId and entityRole from从中解析entityId和entityRole的上下文路径
     * @throws MetadataProviderException in case entityId can't be populated
     */
    protected void populateLocalEntityId(SAMLMessageContext context, String requestURI) throws MetadataProviderException {
//说明：填充LocalEntityId，LocalEntityRole
        String entityId;
        HTTPInTransport inTransport = (HTTPInTransport) context.getInboundMessageTransport();
        //说明：先从attribute里面拿？
        // Pre-configured entity Id                                                                 "localEntityId"
        entityId = (String) inTransport.getAttribute(org.springframework.security.saml.SAMLConstants.LOCAL_ENTITY_ID);
        if (entityId != null) {
            log.debug("Using protocol specified SP {}", entityId);
            context.setLocalEntityId(entityId);
            context.setLocalEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
            return;
        }

        if (requestURI == null) {
            requestURI = "";
        }

        int filterIndex = requestURI.indexOf("/alias/");
        if (filterIndex != -1) { // EntityId from URL alias

            String localAlias = requestURI.substring(filterIndex + 7);
            QName localEntityRole;
            //说明：/alias/test/idp
            int entityTypePosition = localAlias.lastIndexOf('/');
            if (entityTypePosition != -1) {
                String entityRole = localAlias.substring(entityTypePosition + 1);
                if ("idp".equalsIgnoreCase(entityRole)) {
                    localEntityRole = IDPSSODescriptor.DEFAULT_ELEMENT_NAME;
                } else {
                    localEntityRole = SPSSODescriptor.DEFAULT_ELEMENT_NAME;
                }
                localAlias = localAlias.substring(0, entityTypePosition);
            } else {
                localEntityRole = SPSSODescriptor.DEFAULT_ELEMENT_NAME;
            }


            // Populate entityId
            entityId = metadata.getEntityIdForAlias(localAlias);//一般般

            if (entityId == null) {
                throw new MetadataProviderException("No local entity found for alias " + localAlias + ", verify your configuration.");
            } else {
                log.debug("Using SP {} specified in request with alias {}", entityId, localAlias);
            }

            context.setLocalEntityId(entityId);
            context.setLocalEntityRole(localEntityRole);

        } else { // Defaults

            context.setLocalEntityId(getDefaultLocalEntityId(context, requestURI));
            context.setLocalEntityRole(getDefaultLocalEntityRole(context, requestURI));

        }

    }

    /**如果路径中缺少别名，则返回要为上下文填充的localEntityId
     * Returns localEntityId to be populated for the context in case alias is missing from the path
     * @param context context to retrieve localEntityId for
     * @param requestURI context path to parse entityId from
     * @return localEntityId
     * @throws MetadataProviderException in case entityId can't be retrieved
     */
    protected String getDefaultLocalEntityId(SAMLMessageContext context, String requestURI) throws MetadataProviderException {
        return metadata.getHostedSPName();
    }

    /**如果路径中缺少别名，则返回要为上下文填充的localEntityRole
     * Returns localEntityRole to be populated for the context in case alias is missing from the path
     * @param context context to retrieve localEntityRole for
     * @param requestURI context path to parse entityRole from
     * @return localEntityRole
     * @throws MetadataProviderException in case entityRole can't be retrieved
     */
    protected QName getDefaultLocalEntityRole(SAMLMessageContext context, String requestURI) throws MetadataProviderException {
        return SPSSODescriptor.DEFAULT_ELEMENT_NAME;
    }

    /**方法填充字段localEntityId，localEntityRole，localEntityMetadata，localEntityRoleMetadata和peerEntityRole。 如果设置了字段localAlias，localEntityId，localEntiyRole或peerEntityRole，则使用默认SP和IDP作为对等项的默认值。
     * Method populates fields localEntityId, localEntityRole, localEntityMetadata, localEntityRoleMetadata and peerEntityRole.
     * In case fields localAlias, localEntityId, localEntiyRole or peerEntityRole are set they are used, defaults of default SP and IDP as a peer
     * are used instead.
     *
     * @param samlContext context to populate
     * @throws org.opensaml.saml2.metadata.provider.MetadataProviderException
     *          in case metadata do not contain expected entities or localAlias is specified but not found
     */
    protected void populateLocalEntity(SAMLMessageContext samlContext) throws MetadataProviderException {

        String localEntityId = samlContext.getLocalEntityId();
        QName localEntityRole = samlContext.getLocalEntityRole();

        if (localEntityId == null) {
            throw new MetadataProviderException("No hosted service provider is configured and no alias was selected");
        }
//说明：RoleDescriptor是EntityDescriptor的子类？是的。通过localEntityRole和SAMLConstants.SAML20P_NS过滤出第一个
        EntityDescriptor entityDescriptor = metadata.getEntityDescriptor(localEntityId);
        RoleDescriptor roleDescriptor = metadata.getRole(localEntityId, localEntityRole, SAMLConstants.SAML20P_NS);//
        ExtendedMetadata extendedMetadata = metadata.getExtendedMetadata(localEntityId);

        if (entityDescriptor == null || roleDescriptor == null) {
            throw new MetadataProviderException("Metadata for entity " + localEntityId + " and role " + localEntityRole + " wasn't found");
        }
//说明：填充LocalEntityMetadata，LocalEntityRoleMetadata，LocalExtendedMetadata，LocalSigningCredential
        samlContext.setLocalEntityMetadata(entityDescriptor);
        samlContext.setLocalEntityRoleMetadata(roleDescriptor);
        samlContext.setLocalExtendedMetadata(extendedMetadata);

        if (extendedMetadata.getSigningKey() != null) {
            samlContext.setLocalSigningCredential(keyManager.getCredential(extendedMetadata.getSigningKey()));
        } else {
            samlContext.setLocalSigningCredential(keyManager.getDefaultCredential());//看过了
        }

    }

    /**填充用于根据对等服务器对本机进行身份验证的X509凭据。 使用具有在TlsKey下的扩展元数据中指定的别名的密钥，如果未设置，则使用默认凭据。
     * Populates X509 Credential used to authenticate this machine against peer servers. Uses key with alias specified
     * in extended metadata under TlsKey, when not set uses the default credential.
     *
     * @param samlContext context to populate
     */
    protected void populateSSLCredential(SAMLMessageContext samlContext) {

        X509Credential tlsCredential;
        if (samlContext.getLocalExtendedMetadata().getTlsKey() != null) {
            tlsCredential = (X509Credential) keyManager.getCredential(samlContext.getLocalExtendedMetadata().getTlsKey());
        } else {
            tlsCredential = null;
        }

        samlContext.setLocalSSLCredential(tlsCredential);

    }

    /**使用在上下文提供程序中配置的值填充主机名验证程序。
     * Populates hostname verifier using value configured in the context provider..
     *
     * @param samlContext context to populate
     */
    protected void populateSSLHostnameVerifier(SAMLMessageContext samlContext) {

        HostnameVerifier hostnameVerifier = SAMLUtil.getHostnameVerifier(samlContext.getLocalExtendedMetadata().getSslHostnameVerification());
        samlContext.setGetLocalSSLHostnameVerifier(hostnameVerifier);

    }

    /**尝试使用属性“ javax.servlet.request.X509Certificate”从入站消息传输中加载对等SSL证书。 如果找到，则在上下文中设置peerSSLCredential。
     * Tries to load peer SSL certificate from the inbound message transport using attribute
     * "javax.servlet.request.X509Certificate". If found sets peerSSLCredential in the context.
     *
     * @param samlContext context to populate
     */
    protected void populatePeerSSLCredential(SAMLMessageContext samlContext) {

        X509Certificate[] chain = (X509Certificate[]) samlContext.getInboundMessageTransport().getAttribute(ServletRequestX509CredentialAdapter.X509_CERT_REQUEST_ATTRIBUTE);

        if (chain != null && chain.length > 0) {

            log.debug("Found certificate chain from request {}", chain[0]);
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(chain[0]);
            credential.setEntityCertificateChain(Arrays.asList(chain));
            samlContext.setPeerSSLCredential(credential);

        }

    }

    /**当扩展元数据中未指定加密凭据时，基于扩展元数据中的设置或使用默认凭据来填充解密器。
     * Populates a decrypter based on settings in the extended metadata or using a default credential when no
     * encryption credential is specified in the extended metadata.
     *
     * @param samlContext context to populate decryptor for.
     */
    protected void populateDecrypter(SAMLMessageContext samlContext) {

        // Locate encryption key for this entity
        Credential encryptionCredential;
        if (samlContext.getLocalExtendedMetadata().getEncryptionKey() != null) {
            encryptionCredential = keyManager.getCredential(samlContext.getLocalExtendedMetadata().getEncryptionKey());
        } else {
            encryptionCredential = keyManager.getDefaultCredential();
        }
        //用于解密加密的XML部件的实体，使用encryptionKeyResolver从加密的XML中提取EncryptedKey，并尝试使用解析器提供的私钥对其进行解密。
        // Entity used for decrypting of encrypted XML parts
        // Extracts EncryptedKey from the encrypted XML using the encryptedKeyResolver and attempts to decrypt it
        // using private keys supplied by the resolver.
        KeyInfoCredentialResolver resolver = new StaticKeyInfoCredentialResolver(encryptionCredential);

        Decrypter decrypter = new Decrypter(null, resolver, encryptedKeyResolver);
        decrypter.setRootInNewDocument(true);

        samlContext.setLocalDecrypter(decrypter);

    }

    /**基于扩展元数据中的设置，可以使用扩展元数据中指定的可信密钥作为锚来创建PKIX信任引擎，或者（默认情况下）使用来自元数据或来自ExtendedMetadata中覆盖的值的数据来创建显式信任引擎。
     * Based on the settings in the extended metadata either creates a PKIX trust engine with trusted keys specified
     * in the extended metadata as anchors or (by default) an explicit trust engine using data from the metadata or
     * from the values overridden in the ExtendedMetadata.
     *
     * @param samlContext context to populate
     */
    protected void populateTrustEngine(SAMLMessageContext samlContext) {
        SignatureTrustEngine engine;
        if ("pkix".equalsIgnoreCase(samlContext.getLocalExtendedMetadata().getSecurityProfile())) {
            engine = new PKIXSignatureTrustEngine(pkixResolver, Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver(), pkixTrustEvaluator, new BasicX509CredentialNameEvaluator());
        } else {
            engine = new ExplicitKeySignatureTrustEngine(metadataResolver, Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        }
        samlContext.setLocalTrustEngine(engine);
    }

    /**基于扩展元数据中的设置，可以使用扩展元数据中指定的可信密钥作为锚来创建PKIX信任引擎，或者（默认情况下）使用元数据中的数据或来自ExtendedMetadata中覆盖的值的显式信任引擎来创建显式信任引擎。 信任引擎用于验证SSL连接。
     * Based on the settings in the extended metadata either creates a PKIX trust engine with trusted keys specified
     * in the extended metadata as anchors or (by default) an explicit trust engine using data from the metadata or
     * from the values overridden in the ExtendedMetadata. The trust engine is used to verify SSL connections.
     *
     * @param samlContext context to populate
     */
    protected void populateSSLTrustEngine(SAMLMessageContext samlContext) {
        TrustEngine<X509Credential> engine;
        if ("pkix".equalsIgnoreCase(samlContext.getLocalExtendedMetadata().getSslSecurityProfile())) {
            engine = new PKIXX509CredentialTrustEngine(pkixResolver, pkixTrustEvaluator, new BasicX509CredentialNameEvaluator());
        } else {
            engine = new ExplicitX509CertificateTrustEngine(metadataResolver);
        }
        samlContext.setLocalSSLTrustEngine(engine);
    }

    /**
     * Metadata manager provides information about all available IDP and SP entities.
     *
     * @param metadata metadata mangaer
     */
    @Autowired
    public void setMetadata(MetadataManager metadata) {
        this.metadata = metadata;
    }

    /**
     * Key manager provides information about private certificate and trusted keys provide in addition to
     * cryptographic material present in entity metadata documents.
     *
     * @param keyManager key manager
     */
    @Autowired
    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * Sets resolver used to populate data for PKIX trust engine. Trust anchors are internally cached. They get populated
     * using configured MetadataResolver and enhanced with trustedKeys from the ExtendedMetadata.
     *
     * System uses default configuration when property is not set.
     *
     * Default implementation (org.springframework.security.saml.trust.PKIXInformationResolver) loads trust anchors
     * from both metadata and extended metadata of the peer entity. In case ExtendedMetadata doesn't define any
     * trustedKeys (property trustedKeys is null which is the default), system will use all certificates available
     * in the configured keyStore as trust anchors.
     *
     * @param pkixResolver pkix resolver
     * @see org.springframework.security.saml.trust.PKIXInformationResolver
     */
    public void setPkixResolver(PKIXValidationInformationResolver pkixResolver) {
        this.pkixResolver = pkixResolver;
    }

    /**
     * Trust evaluator is responsible for verifying whether to trust certificate based on PKIX verification.
     *
     * System uses default configuration when property is not set.
     *
     * Default implementation (org.springframework.security.saml.trust.CertPathPKIXTrustEvaluator) uses Java CertPath API
     * to perform the verification. The default implementation can be constructed with an instance of
     * org.opensaml.xml.security.x509.CertPathPKIXValidationOptions which further customizes the PKIX process, e.g. in
     * regard to certificate expiration checking. It is also possible to customize the security provider to use for
     * loading of the CertPath API factories.
     *
     * @param pkixTrustEvaluator pkix trust evaluator
     * @see org.springframework.security.saml.trust.CertPathPKIXTrustEvaluator
     */
    public void setPkixTrustEvaluator(PKIXTrustEvaluator pkixTrustEvaluator) {
        this.pkixTrustEvaluator = pkixTrustEvaluator;
    }

    /**
     * Sets resolver used to populate trusted credentials from XML and Extended metadata. Metadata resolver
     * is used as the only resolver for MetaIOP security profile. It is also used for loading of trusted anchors in
     * the PKIX profile.
     *
     * System uses default configuration when property is not set.
     *
     * Default implementation (org.springframework.security.saml.trust.MetadataCredentialResolver) populates
     * trusted certificates from both peer metadata and peer extended metadata (properties signingKey, encryptionKey
     * and tlsKey).
     *
     * @param metadataResolver metaiop resolver
     * @see org.springframework.security.saml.trust.MetadataCredentialResolver
     */
    public void setMetadataResolver(MetadataCredentialResolver metadataResolver) {
        this.metadataResolver = metadataResolver;
    }

    /**
     * Implementation of the SAML message storage factory providing custom mechanism for storage
     * of SAML messages such as http session, cookies or no storage at all.
     *
     * @param storageFactory storage factory
     */
    @Autowired(required = false)
    public void setStorageFactory(SAMLMessageStorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    /**
     * Verifies that required entities were autowired or set and initializes resolvers used to construct trust engines.
     */
    public void afterPropertiesSet() throws ServletException {

        Assert.notNull(keyManager, "Key manager must be set");
        Assert.notNull(metadata, "Metadata must be set");
        Assert.notNull(storageFactory, "MessageStorageFactory must be set");

        if (metadataResolver == null) {
            MetadataCredentialResolver resolver = new org.springframework.security.saml.trust.MetadataCredentialResolver(metadata, keyManager);
            resolver.setMeetAllCriteria(false);
            resolver.setUnevaluableSatisfies(true);
            this.metadataResolver = resolver;
        }

        if (pkixResolver == null) {
            pkixResolver = new PKIXInformationResolver(metadataResolver, metadata, keyManager);
        }

        if (pkixTrustEvaluator == null) {
            pkixTrustEvaluator = new CertPathPKIXTrustEvaluator();
        }

    }

}
