/* Copyright 2011 Vladimir Schaefer
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
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**通过类，可以将正常实体元数据加载委托给所选提供程序，同时使用扩展的元数据增强数据。
 * Class enables delegation of normal entity metadata loading to the selected provider while enhancing data with
 * extended metadata.
 */
public class ExtendedMetadataDelegate extends AbstractMetadataDelegate implements ExtendedMetadataProvider {
//说明：将MetadataProvider和ExtendedMetadata关联起来了，后续可以直接使用
    // Class logger
    protected final Logger log = LoggerFactory.getLogger(ExtendedMetadataDelegate.class);

    /**
     * When true metadata will only be accepted if correctly signed.
     */
    private boolean metadataRequireSignature = false;

    /**设置为true时，将使用带有metadataTrustedKeys作为锚点的PKIX验证元数据签名是否可信。
     * When true metadata signature will be verified for trust using PKIX with metadataTrustedKeys
     * as anchors.
     */
    private boolean metadataTrustCheck = true;

    /**确定是否应始终将证书吊销检查作为PKIX验证的一部分进行。 撤销由底层的JCE实施评估，并且取决于配置，其中可能包括所涉及证书的CRL和OCSP验证。
     * Determines whether check for certificate revocation should always be done as part of the PKIX validation.
     * Revocation is evaluated by the underlaying JCE implementation and depending on configuration may include
     * CRL and OCSP verification of the certificate in question.
     */
    private boolean forceMetadataRevocationCheck = false;

    /**存储在KeyManager中的密钥，可用于验证（这个）元数据的签名是否受信任。 如果未设置，则存储在keyManager中的任何密钥均被视为可信密钥。
     * Keys stored in the KeyManager which can be used to verify whether signature of the metadata is trusted.
     * If not set any key stored in the keyManager is considered as trusted.
     */
    private Set<String> metadataTrustedKeys = null;//说明：这个元数据只信任这个字段中设置的key

    /**map不包含任何值时使用的元数据。
     * Metadata to use in case map doesn't contain any value.
     */
    private ExtendedMetadata defaultMetadata;//说明：永远不可能为空
//说明：一个元数据文档里面包含多个元数据才有用，在我们这里根本不会有这种需求啊。不一定啊是不是可以把所有的扩展元数据通过一个ExtendedMetadataDelegate加载？最大问题是这孙子是私有的
    /**这是关键，一个id对应一个扩展元数据配置
     * EntityID specific metadata.
     */
    private Map<String, ExtendedMetadata> extendedMetadataMap;//说明：没发现有啥意义。Extended metadata for specific IDPs

    /**标志指示（委派的）元数据已经包含执行所包含的元数据的签名和信任验证所需的所有信息。
     * Flag indicates that delegated metadata already contains all information required to perform signature
     * and trust verification of the included metadata.
     */
    private boolean trustFiltersInitialized;

    /**
     * Uses provider for normal entity data, for each entity available in the delegate returns given defaults.
     *
     * @param delegate delegate with available entities
     */
    public ExtendedMetadataDelegate(MetadataProvider delegate) {
        this(delegate, null, null);
    }

    /**使用提供者提供普通实体数据，对于委托中可用的每个实体，返回给定的默认值。
     * Uses provider for normal entity data, for each entity available in the delegate returns given defaults.
     *
     * @param delegate        delegate with available entities不理解
     * @param defaultMetadata default extended metadata, can be null
     */
    public ExtendedMetadataDelegate(MetadataProvider delegate, ExtendedMetadata defaultMetadata) {
        this(delegate, defaultMetadata, null);
    }

    /**
     * Uses provider for normal entity data, tries to locate extended metadata by search in the map.
     *
     * @param delegate            delegate with available entities
     * @param extendedMetadataMap map, can be null
     */
    public ExtendedMetadataDelegate(MetadataProvider delegate, Map<String, ExtendedMetadata> extendedMetadataMap) {
        this(delegate, null, extendedMetadataMap);
    }

    /**没从代码里面体现出来
     * Uses provider for normal entity data, tries to locate extended metadata by search in the map, in case it's not found
     * uses the default.
     *
     * @param delegate            delegate with available entities
     * @param defaultMetadata     default extended metadata, can be null
     * @param extendedMetadataMap map, can be null
     */
    public ExtendedMetadataDelegate(MetadataProvider delegate, ExtendedMetadata defaultMetadata, Map<String, ExtendedMetadata> extendedMetadataMap) {
        super(delegate);
        if (defaultMetadata == null) {
            this.defaultMetadata = new ExtendedMetadata();//所以说defaultMetadata永远不可能为空
        } else {
            this.defaultMetadata = defaultMetadata;
        }
        this.extendedMetadataMap = extendedMetadataMap;
    }


    /**尝试加载给定实体的扩展元数据。 使用以下算法：验证是否可以使用委托来定位entityId（换句话说，请确保对于没有基本属性的实体，我们不返回扩展的metdata）。 如果扩展的元数据可用并且包含entityId的值，则将其返回。 否则返回默认元数据
     * Tries to load extended metadata for the given entity. The following algorithm is used:
     * <ol>
     * <li>Verifies that entityId can be located using the delegate (in other words makes sure we don't return extended metdata
     * for entities we don't have the basic ones for</li>
     * <li>In case extended metadata is available and contains value for the entityId it is returned</li>
     * <li>Returns default metadata otherwise</li>
     * </ol>
     *
     * @param entityID entity to load metadata for
     * @return extended metadata or null in case no default is given and entity can be located or is not present in the delegate
     * @throws MetadataProviderException error
     */
    public ExtendedMetadata getExtendedMetadata(String entityID) throws MetadataProviderException {//吊，文档上有
//说明：在构造函数里面已经配置死了。在这里也就是取出来而已。也就相当于一个get方法。
        EntityDescriptor entityDescriptor = getEntityDescriptor(entityID);//看过了
        if (entityDescriptor == null) {
            return null;//看过了。。请确保对于没有基本属性的实体，我们不返回扩展的metdata
        }

        ExtendedMetadata extendedMetadata = null;

        if (extendedMetadataMap != null) {
            extendedMetadata = extendedMetadataMap.get(entityID);//我应该不会配置什么map
        }

        if (extendedMetadata == null) {
            return defaultMetadata;
        } else {
            return extendedMetadata;
        }

    }

    /**方法执行（它委托给的提供者的）初始化。
     * Method performs initialization of the provider it delegates to.
     *
     * @throws MetadataProviderException in case initialization fails
     */
    public void initialize() throws MetadataProviderException {
        if (getDelegate() instanceof AbstractMetadataProvider) {
            log.debug("Initializing delegate");
            AbstractMetadataProvider provider = (AbstractMetadataProvider) getDelegate();
            provider.initialize();
        } else {//说明：被委托的必须是一个metadataprovider
            log.debug("Cannot initialize delegate, doesn't extend AbstractMetadataProvider");
        }
    }

    /**
     * Method destroys the metadata delegate.
     */
    public void destroy() {
        if (getDelegate() instanceof AbstractMetadataProvider) {
            log.debug("Destroying delegate");
            AbstractMetadataProvider provider = (AbstractMetadataProvider) getDelegate();
            provider.destroy();
        } else {//说明：被委托的必须是一个metadataprovider
            log.debug("Cannot destroy delegate, doesn't extend AbstractMetadataProvider");
        }
    }

    /**如果设置，则返回可用于验证元数据签名是否受信任的密钥集。 如果未设置，则可以使用已配置的KeyManager中的任何密钥来验证信任。
     默认情况下，该值为null。
     * If set returns set of keys which can be used to verify whether signature of the metadata is trusted. When
     * not set any of the keys in the configured KeyManager can be used to verify trust.
     * <p>
     * By default the value is null.
     *
     * @return trusted keys or null
     */
    public Set<String> getMetadataTrustedKeys() {
        return metadataTrustedKeys;
    }

    /**KeyManager中存在的密钥别名集，可用于验证元数据实体上的签名是否受信任。 设置为null时，可以使用KeyManager的任何密钥来验证信任。
     * Set of aliases of keys present in the KeyManager which can be used to verify whether signature on metadata entity
     * is trusted. When set to null any key of KeyManager can be used to verify trust.
     *
     * @param metadataTrustedKeys keys or null
     */
    public void setMetadataTrustedKeys(Set<String> metadataTrustedKeys) {
        this.metadataTrustedKeys = metadataTrustedKeys;
    }

    /**
     * Flag indicating whether metadata must be signed.
     * <p>
     * By default signature is not required.
     *
     * @return signature flag
     */
    public boolean isMetadataRequireSignature() {
        return metadataRequireSignature;
    }

    /**设置为true时，只有在正确签名和验证后，才能接受来自此提供程序的元数据。 具有无效签名或由不受信任的凭证签名的元数据将被忽略。
     * When set to true metadata from this provider should only be accepted when correctly signed and verified. Metadata with
     * an invalid signature or signed by a not-trusted credential will be ignored.
     *
     * @param metadataRequireSignature flag to set
     */
    public void setMetadataRequireSignature(boolean metadataRequireSignature) {
        this.metadataRequireSignature = metadataRequireSignature;
    }

    public boolean isMetadataTrustCheck() {
        return metadataTrustCheck;
    }

    public void setMetadataTrustCheck(boolean metadataTrustCheck) {
        this.metadataTrustCheck = metadataTrustCheck;
    }

    public boolean isForceMetadataRevocationCheck() {
        return forceMetadataRevocationCheck;
    }

    /**确定是否应始终将证书吊销检查作为PKIX验证的一部分进行。 撤消由底层JCE实施评估，并且视配置而定，可能包括所涉及证书的CRL和OCSP验证。设置为false时，撤消仅在MetadataManager包含CRL时执行
     * Determines whether check for certificate revocation should always be done as part of the PKIX validation.
     * Revocation is evaluated by the underlaying JCE implementation and depending on configuration may include
     * CRL and OCSP verification of the certificate in question.
     * <p>
     * When set to false revocation is only performed when MetadataManager includes CRLs
     *
     * @param forceMetadataRevocationCheck revocation flag
     */
    public void setForceMetadataRevocationCheck(boolean forceMetadataRevocationCheck) {
        this.forceMetadataRevocationCheck = forceMetadataRevocationCheck;
    }

    protected boolean isTrustFiltersInitialized() {
        return trustFiltersInitialized;
    }

    protected void setTrustFiltersInitialized(boolean trustFiltersInitialized) {
        this.trustFiltersInitialized = trustFiltersInitialized;
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }

}
