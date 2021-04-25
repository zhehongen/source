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

import java.io.Serializable;
import java.util.Set;

import org.springframework.security.saml.SAMLConstants;

/**类包含描述SAML实体的其他信息。 元数据既可以用于本地实体（=使用SAML扩展作为已部署应用程序的一部分可访问的实体），也可以用于远程实体（=用户可以与IDP进行交互的实体）。
 * Class contains additional information describing a SAML entity. Metadata can be used both for local entities
 * (= the ones accessible as part of the deployed application using the SAML Extension) and remote entities (= the ones
 * user can interact with like IDPs).
 *
 * @author Vladimir Schaefer
 */
public class ExtendedMetadata implements Serializable, Cloneable {
//说明：完全从数据库读取，是不是就可以实现多租户了？
    /**值的设置确定该实体是本地部署（在当前安装中托管）还是它是其他地方部署的实体。
     * Setting of the value determines whether the entity is deployed locally (hosted on the current installation) or
     * whether it's an entity deployed elsewhere.
     */
    private boolean local;

    /**用于构造众所周知的元数据地址并根据传入请求确定目标实体的本地别名。
     * Local alias of the entity used for construction of well-known metadata address and determining target
     * entity from incoming requests.唯一别名，用于根据使用的URL标识所选的本地服务提供商
     */
    private String alias;

    /**当为真时，将在SSO之前调用IDP发现。 仅对本地实体有效。
     * When true IDP discovery will be invoked before SSO. Only valid for local entities.
     */
    private boolean idpDiscoveryEnabled;

    /**应根据请求将IDP发现服务用户的URL重定向到，以确定要使用的IDP。 值可以覆盖本地SP元数据中的设置。 仅对本地实体有效。
     * URL of the IDP Discovery service user should be redirected to upon request to determine which IDP to use.
     * Value can override settings in the local SP metadata. Only valid for local entities.
     */
    private String idpDiscoveryURL;

    /**发现服务应将响应发送回我们的发现请求的URL。 仅对本地实体有效。
     * URL where the discovery service should send back response to our discovery request. Only valid for local
     * entities.
     */
    private String idpDiscoveryResponseURL;

    /**指示是否应将增强型客户端/代理配置文件用于支持它的请求。 仅对本地实体有效。
     * Indicates whether Enhanced Client/Proxy profile should be used for requests which support it. Only valid for
     * local entities.
     */
    private boolean ecpEnabled;

    /**用于信任验证的配置文件，默认情况下为MetaIOP。 仅与本地实体相关。
     * Profile used for trust verification, MetaIOP by default. Only relevant for local entities.
     */
    private String securityProfile = "metaiop";

    /**用于SSL / TLS信任验证的配置文件，默认为PKIX。 仅与本地实体相关
     * Profile used for SSL/TLS trust verification, PKIX by default. Only relevant for local entities.
     */
    private String sslSecurityProfile = "pkix";

    /**用于验证SSL连接的主机名验证程序，例如 用于ArtifactResolution。
     * Hostname verifier to use for verification of SSL connections, e.g. for ArtifactResolution.
     */
    private String sslHostnameVerification = "default";

    /**
     * Key (stored in the local keystore) used for signing/verifying signature of messages sent/coming from this
     * entity. For local entities private key must be available, for remote entities only public key is required.
     */
    private String signingKey;

    /**
     * Algorithm used for creation of digital signatures of this entity. At the moment only used for metadata signatures.
     * Only valid for local entities.
     */
    private String signingAlgorithm;

    /**
     * Flag indicating whether to sign metadata for this entity. Only valid for local entities.
     */
    private boolean signMetadata;

    /**
     * Name of generator for KeyInfo elements in metadata and signatures. At the moment only used for metadata signatures.
     * Only valid for local entities.
     */
    private String keyInfoGeneratorName = SAMLConstants.SAML_METADATA_KEY_INFO_GENERATOR;

    /**
     * Key (stored in the local keystore) used for encryption/decryption of messages coming/sent from this entity. For local entities
     * private key must be available, for remote entities only public key is required.
     */
    private String encryptionKey;

    /**用于验证SSL / TLS连接的密钥。 对于本地实体，在指定时将密钥包含在生成的元数据中。 对于远程实体，在指定密钥以及使用MetaIOP安全配置文件时，密钥用于SSL / TLS的服务器身份验证。
     * Key used for verification of SSL/TLS connections. For local entities key is included in the generated metadata when specified.
     * For remote entities key is used to for server authentication of SSL/TLS when specified and when MetaIOP security profile is used.
     */
    private String tlsKey;

    /**
     * Keys used as anchors for trust verification when PKIX mode is enabled for the local entity. In case value is null
     * all keys in the keyStore will be treated as trusted.
     */
    private Set<String> trustedKeys;

    /**
     * SAML specification mandates that incoming LogoutRequests must be authenticated.
     */
    private boolean requireLogoutRequestSigned = true;

    /**
     * Flag indicating whether incoming LogoutResposne messages must be authenticated.
     */
    private boolean requireLogoutResponseSigned;

    /**
     * If true received artifactResolve messages will require a signature, sent artifactResolve will be signed.
     */
    private boolean requireArtifactResolveSigned = true;

    /**
     * Flag indicating whether to support unsolicited responses (IDP-initialized SSO). Only valid for remote
     * entities.
     */
    private boolean supportUnsolicitedResponse = true;

    /**
     * Algorithm used for creation of digest method of this entity. At the moment only used for metadata signatures.
     * Only valid for local entities.
     */
    private String digestMethodAlgorithm;

    /**
     * Security profile to use for this local entity - MetaIOP (default) or PKIX.
     *
     * @return profile
     */
    public String getSecurityProfile() {//看过了
        return securityProfile;
    }

    /**设置用于验证签名和加密的配置文件。 可以使用以下配置文件：MetaIOP配置文件（默认情况下）：使用来自相关实体的元数据文档的加密数据。 在这种模式下，不进行证书有效性或吊销检查。 必须预先知道所有密钥。PKIX配置文件：当可以使用配置为对等锚的对等方的受信任密钥使用PKIX验证凭据时，签名被视为受信任。此设置仅与本地实体相关。
     * Sets profile used for verification of signatures and encryption. The following profiles are available:
     * <p>
     * MetaIOP profile (by default):
     * <br>
     * Uses cryptographic data from the metadata document of the entity in question. No checks for validity
     * or revocation of certificates is done in this mode. All keys must be known in advance.
     * <p>
     * PKIX profile:
     * <br>
     * Signatures are deemed as trusted when credential can be verified using PKIX with trusted keys of the peer
     * configured as trusted anchors.
     * <p>
     * This setting is only relevant for local entities.
     *
     * @param securityProfile profile to use - PKIX when set to "pkix", MetaIOP otherwise
     */
    public void setSecurityProfile(String securityProfile) {
        this.securityProfile = securityProfile;
    }

    /**
     * Security profile used for SSL/TLS connections of the local entity.
     *
     * @return profile
     */
    public String getSslSecurityProfile() {
        return sslSecurityProfile;
    }

    /**设置用于验证SSL / TLS连接的配置文件。 可以使用以下配置文件：PKIX配置文件（默认情况下），值“ pkix”：当可以使用配置为对等方的信任密钥的PKIX来验证凭据时，将签名视为可信。MetaIOP配置文件，任何其他值：使用加密 来自相关实体的元数据文档的数据。 在这种模式下，不进行证书有效性或吊销检查。 必须事先知道所有密钥。在SAMLContextProviderImpl＃populateSSLTrustEngine中强制执行逻辑。 值不区分大小写。此设置仅与本地实体有关。
     * Sets profile used for verification of SSL/TLS connections. The following profiles are available:
     * <p>
     * PKIX profile (by default), value "pkix":
     * <br>
     * Signatures are deemed as trusted when credential can be verified using PKIX with trusted keys of the peer
     * configured as trusted anchors.
     * <p>
     * MetaIOP profile, any other value:
     * <br>
     * Uses cryptographic data from the metadata document of the entity in question. No checks for validity
     * or revocation of certificates is done in this mode. All keys must be known in advance.
     * <p>
     * Logic is enforced in SAMLContextProviderImpl#populateSSLTrustEngine. Values are case insensitive.
     * <p>
     * This setting is only relevant for local entities.
     *
     * @param sslSecurityProfile profile to use - PKIX when set to "pkix", MetaIOP otherwise
     */
    public void setSslSecurityProfile(String sslSecurityProfile) {
        this.sslSecurityProfile = sslSecurityProfile;
    }

    /**
     * Hostname verifier for SSL connections.
     *
     * @return hostname verifier
     */
    public String getSslHostnameVerification() {
        return sslHostnameVerification;
    }

    /**设置主机名验证程序以用于SSL连接验证。 可以使用以下值：默认值：org.apache.commons.ssl.HostnameVerifier.DEFAULT defaultAndLocalhost：org.apache.commons.ssl.HostnameVerifier.DEFAULT_AND_LOCALHOST严格：org.apache.commons.ssl.HostnameVerifier.STRICT allowAll：org.apache.commons.ssl.HostnameVerifier.STRICT .ALLOW_ALL，不执行任何验证。在SAMLContextProviderImpl＃populateSSLHostnameVerifier中强制执行逻辑。 值不区分大小写。 无法识别的值恢复为默认设置。此设置仅与本地实体有关。
     * Sets hostname verifier to use for verification of SSL connections. The following values are available:
     * <p>
     * default: org.apache.commons.ssl.HostnameVerifier.DEFAULT
     * <br>
     * defaultAndLocalhost: org.apache.commons.ssl.HostnameVerifier.DEFAULT_AND_LOCALHOST
     * <br>
     * strict: org.apache.commons.ssl.HostnameVerifier.STRICT
     * <br>
     * allowAll: org.apache.commons.ssl.HostnameVerifier.ALLOW_ALL, doesn't perform any validation
     * <p>
     * Logic is enforced in SAMLContextProviderImpl#populateSSLHostnameVerifier. Values are case insensitive.
     * Unrecognized value revert to default setting.
     * <p>
     * This setting is only relevant for local entities.
     *
     * @param sslHostnameVerification hostname verification type flag
     */
    public void setSslHostnameVerification(String sslHostnameVerification) {
        this.sslHostnameVerification = sslHostnameVerification;
    }

    /**对于远程实体，该值应为null？为什么？
     * Returns alias. Value should be null for remote entities.
     *
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    /**别名用于将目标实体标识为URL的一部分。 它仅适用于本地实体。 别名只能使用ASCII字符。如果本地实体的别名为null，则必须将其设置为默认值以便访问。别名对于每个本地entityId必须唯一。
     * Alias is used to identify a destination entity as part of the URL. It only applies to local entities. Only
     * ASCII characters can be used as alias.
     * <p>
     * In case the alias is null on a local entity it must be set as a default
     * to be accessible.
     * <p>
     * Alias must be unique for each local entityId.
     *
     * @param alias alias value
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Signing key used for signing messages or verifying signatures of this entity.
     *
     * @return signing key, default if null
     */
    public String getSigningKey() {
        return signingKey;//对消息进行签名或验证（私钥签名，公钥验证）
    }

    /**
     * Sets signing key to be used for interaction with the current entity. In case the entity is local the keyStore
     * must contain a private and public key with the given name. For remote entities only public key is required.
     * <p>
     * Value can be used to override credential contained in the remote metadata.
     *
     * @param signingKey key for creation/verification of signatures
     */
    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }

    /**
     * Encryption key used for encrypting messages send to the remote entity or decrypting data sent to the local one.
     *
     * @return encryption key, default if null
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

    /**设置用于与当前实体进行交互的加密密钥。 如果实体是本地实体，则keyStore必须包含具有给定名称的私钥，该私钥将用于解密传入消息。 对于远程实体，仅需要公钥，并将其用于对发送的数据进行加密。值可用于覆盖远程元数据中包含的凭据。
     * Sets encryption key to be used for interaction with the current entity. In case the entity is local the keyStore
     * must contain a private key with the given name which will be used for decryption incoming message.
     * For remote entities only public key is required and will be used for encryption of the sent data.
     * <p>
     * Value can be used to override credential contained in the remote metadata.
     *
     * @param encryptionKey key for creation/verification of signatures
     */
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Flag indicating whether entity in question requires logout request to be signed.
     *
     * @return signature flag
     */
    public boolean isRequireLogoutRequestSigned() {
        return requireLogoutRequestSigned;
    }

    /**
     * If true logoutRequests received will require a signature, sent logoutRequests will be signed.
     *
     * @param requireLogoutRequestSigned logout request signature flag
     */
    public void setRequireLogoutRequestSigned(boolean requireLogoutRequestSigned) {
        this.requireLogoutRequestSigned = requireLogoutRequestSigned;
    }

    /**
     * Flag indicating whether entity in question requires logout response to be signed.
     *
     * @return signature flag
     */
    public boolean isRequireLogoutResponseSigned() {
        return requireLogoutResponseSigned;
    }

    /**
     * If true logoutResponses received will require a signature, sent logoutResponses will be signed.
     *
     * @param requireLogoutResponseSigned logout response signature flag
     */
    public void setRequireLogoutResponseSigned(boolean requireLogoutResponseSigned) {
        this.requireLogoutResponseSigned = requireLogoutResponseSigned;
    }

    /**
     * Flag indicating whether entity in question requires artifact resolve messages to be signed.
     *
     * @return signature flag
     */
    public boolean isRequireArtifactResolveSigned() {
        return requireArtifactResolveSigned;
    }

    /**如果为true，则收到的artifactResolve消息将需要签名，发送的artifactResolve将被签名。
     * If true received artifactResolve messages will require a signature, sent artifactResolve will be signed.
     *
     * @param requireArtifactResolveSigned artifact resolve signature flag
     */
    public void setRequireArtifactResolveSigned(boolean requireArtifactResolveSigned) {
        this.requireArtifactResolveSigned = requireArtifactResolveSigned;
    }

    /**在本地实体上指定时，用于针对远程对等方认证实例的密钥。 在远程实体上指定该密钥后，将在使用SSL / TLS与该实体进行通信的过程中将密钥添加为信任锚(公钥)。
     * Key used to authenticate instance against remote peers when specified on local entity. When specified on
     * remote entity the key is added as a trust anchor during communication with the entity using SSL/TLS.
     *
     * @return tls key
     */
    public String getTlsKey() {
        return tlsKey;
    }

    /** 对于本地实体，表示用于使用SSL / TLS连接针对对等服务器认证该实例的密钥的别名。 如果未设置，则没有密钥可用于客户端身份验证。 别名必须与包含私钥且为X509类型的密钥相关联。 对于远程实体，表示将用作SSL / TLS连接的信任锚的密钥。
     * For local entities denotes alias of the key used to authenticate this instance against peer servers using SSL/TLS connections. When
     * not set no key will be available for client authentication. Alias must be associated with a key containing a private key and being
     * of X509 type. For remote entities denotes key to be used as a trust anchor for SSL/TLS connections.
     *
     * @param tlsKey tls key
     */
    public void setTlsKey(String tlsKey) {//本质来说，就是https使用的。sp作为客户端，idp作为服务端
        this.tlsKey = tlsKey;
    }

    /**可信密钥可用于启用PKIX验证的实体的签名和服务器SSL / TLS验证。 未启用PKIX安全性时，将忽略该值。 如果value为null，则keyStore中的所有密钥都将被视为可信密钥。
     * Trusted keys usable for signature and server SSL/TLS verification for entities with PKIX verification enabled.
     * Value is ignored when PKIX security is not enabled. In case value is null all keys in the keyStore will be
     * treated as trusted.
     *
     * @return trusted keys
     */
    public Set<String> getTrustedKeys() {
        return trustedKeys;
    }

    /**密钥集，用作来自该实体的消息的PKIX验证的锚点。 仅适用于远程实体，并且在本地实体启用PKIX配置文件时使用。当未指定可信密钥时，keyManager中的所有密钥都将视为可信密钥。此设置仅与远程实体相关。
     * Set of keys used as anchors for PKIX verification of messages coming from this entity. Only applicable for
     * remote entities and used when local entity has the PKIX profile enabled.
     * <p>
     * When no trusted keys are specified all keys in the keyManager are treated as trusted.
     * <p>
     * This setting is only relevant for remote entities.
     *
     * @param trustedKeys keys
     */
    public void setTrustedKeys(Set<String> trustedKeys) {
        this.trustedKeys = trustedKeys;
    }

    public boolean isLocal() {
        return local;
    }

    /**设置为true时，实体被视为本地部署，并且将能够在由所选别名确定的端点上接受消息。
     * When set to true entity is treated as locally deployed and will be able to accept messages on endpoints determined
     * by the selected alias.
     *
     * @param local true when entity is deployed locally
     */
    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getIdpDiscoveryURL() {
        return idpDiscoveryURL;
    }

    /**初始化本地SP的IDP发现协议时要调用的URL。
     * URL to invoke while initializing IDP Discovery protocol for the local SP.
     *
     * @param idpDiscoveryURL IDP discovery URL
     */
    public void setIdpDiscoveryURL(String idpDiscoveryURL) {
        this.idpDiscoveryURL = idpDiscoveryURL;
    }

    public String getIdpDiscoveryResponseURL() {
        return idpDiscoveryResponseURL;
    }

    /**居然是本地的idp发现服务实现
     * When set our local IDP Discovery implementation will send response back to Service Provider on this address.
     * Value should be set in situations when public address of the SP differs from values seen by the application sever.
     *
     * @param idpDiscoveryResponseURL discovery response URL
     */
    public void setIdpDiscoveryResponseURL(String idpDiscoveryResponseURL) {
        this.idpDiscoveryResponseURL = idpDiscoveryResponseURL;
    }

    /**如果为true，则将在初始化WebSSO之前调用IDP发现，除非在SAMLContext中已指定IDP。
     * When true IDP discovery will be invoked before initializing WebSSO, unless IDP is already specified inside
     * SAMLContext.
     *
     * @return true when idp discovery is enabled
     */
    public boolean isIdpDiscoveryEnabled() {
        return idpDiscoveryEnabled;
    }

    public void setIdpDiscoveryEnabled(boolean idpDiscoveryEnabled) {
        this.idpDiscoveryEnabled = idpDiscoveryEnabled;
    }

    public void setEcpEnabled(boolean ecpEnabled) {
        this.ecpEnabled = ecpEnabled;
    }

    public boolean isEcpEnabled() {
        return ecpEnabled;
    }

    /**
     * Gets the signing algorithm to use when signing the SAML messages.
     * This can be used, for example, when a strong algorithm is required (e.g. SHA 256 instead of SHA 128).
     *
     * Value only applies to local entities.
     *
     * At the moment the value is only used for signatures on metadata.
     *
     * @return A signing algorithm URI, if set. Otherwise returns null.
     * @see org.opensaml.xml.signature.SignatureConstants
     */
    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    /**设置对SAML消息进行签名时要使用的签名算法。 例如，当需要强大的算法时（例如SHA 256而不是SHA 128），可以使用此方法。 如果此属性为null，则将改用org.opensaml.xml.security.credential.Credential默认算法。 值仅适用于本地实体。 目前，该值仅用于元数据上的签名。 典型值为：https://www.w3.org/2000/09/xmldsig#rsa-sha1 https://www.w3.org/2001/04/xmldsig-more#rsa-sha256 https：// www。 w3.org/2001/04/xmldsig-more#rsa-sha512
     * ----------Sets the signing algorithm to use when signing the SAML messages.
     * This can be used, for example, when a strong algorithm is required (e.g. SHA 256 instead of SHA 128).
     * If this property is null, then the {@link org.opensaml.xml.security.credential.Credential} default algorithm will be used instead.
     *
     * Value only applies to local entities.
     *
     * At the moment the value is only used for signatures on metadata.
     *
     * Typical values are:
     * https://www.w3.org/2000/09/xmldsig#rsa-sha1
     * https://www.w3.org/2001/04/xmldsig-more#rsa-sha256
     * https://www.w3.org/2001/04/xmldsig-more#rsa-sha512
     *
     * @param signingAlgorithm The new signing algorithm to use
     * @see org.opensaml.xml.signature.SignatureConstants
     */
    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    /**
     * Sets KeyInfoGenerator used to create KeyInfo elements in metadata and digital signatures. Only valid
     * for local entities.
     *
     * @param keyInfoGeneratorName generator name
     */
    public void setKeyInfoGeneratorName(String keyInfoGeneratorName) {
        this.keyInfoGeneratorName = keyInfoGeneratorName;
    }

    /**在默认KeyInfoGeneratorManager中注册的KeyInfoGenerator的名称。 用于在元数据和签名中生成KeyInfo元素。
     * Name of the KeyInfoGenerator registered at default KeyInfoGeneratorManager. Used to generate
     * KeyInfo elements in metadata and signatures.
     *密钥信息生成器名称
     * @return key info generator name
     * @see org.opensaml.Configuration#getGlobalSecurityConfiguration()
     * @see org.opensaml.xml.security.SecurityConfiguration#getKeyInfoGeneratorManager()
     */
    public String getKeyInfoGeneratorName() {
        return keyInfoGeneratorName;
    }

    /**
     * Flag indicating whether local metadata will be digitally signed.
     *
     * @return metadata signing flag
     */
    public boolean isSignMetadata() {
        return signMetadata;
    }

    /**设置为true时，将为该实体生成的元数据由签名证书进行数字签名。 仅适用于本地实体。
     * When set to true metadata generated for this entity will be digitally signed by the signing certificate.
     * Only applies to local entities.
     *
     * @param signMetadata metadata signing flag
     */
    public void setSignMetadata(boolean signMetadata) {
        this.signMetadata = signMetadata;
    }

    /**
     * @return true when system should accept unsolicited response messages from this remote entity
     */
    public boolean isSupportUnsolicitedResponse() {
        return supportUnsolicitedResponse;
    }

    /**设置为true时，系统将支持从该远程实体接收未经请求的SAML响应消息（IDP初始化的单点登录）。 禁用后，此类消息将被拒绝。 默认情况下，未经请求的响应是启用的。
     * When set to true system will support reception of Unsolicited SAML Response messages (IDP-initialized single
     * sign-on) from this remote entity. When disabled such messages will be rejected.
     *
     * Unsolicited Responses are by default enabled.
     *
     * @param supportUnsolicitedResponse unsolicited response flag
     */
    public void setSupportUnsolicitedResponse(boolean supportUnsolicitedResponse) {
        this.supportUnsolicitedResponse = supportUnsolicitedResponse;
    }

    /**
     * Clones the existing metadata object.
     *
     * @return clone of the metadata
     */
    @Override
    public ExtendedMetadata clone() {
        try {
            return (ExtendedMetadata) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Extended metadata not cloneable", e);
        }
    }

    /**
     * Returns digest method algorithm value
     * @return String
     */
    public String getDigestMethodAlgorithm()
    {
        return digestMethodAlgorithm;
    }

    /**设置对SAML消息进行签名时要使用的摘要方法算法。 例如，当需要强大的算法时（例如SHA 256而不是SHA 128），可以使用此方法。 如果此属性为null，则将改用org.opensaml.xml.Configuration默认算法。 值仅适用于本地实体。 目前，该值仅用于元数据上的签名。 典型值为：https://www.w3.org/2001/04/xmlenc#sha1 https://www.w3.org/2001/04/xmlenc#sha256 https://www.w3.org/2001/ 04 / xmlenc＃sha384 https://www.w3.org/2001/04/xmlenc#sha512 https://www.w3.org/2001/04/xmlenc#ripemd160
     *————————————————Sets the digest method algorithm to use when signing the SAML messages.
     * This can be used, for example, when a strong algorithm is required (e.g. SHA 256 instead of SHA 128).
     * If this property is null, then the {@link org.opensaml.xml.Configuration} default algorithm will be used instead.
     *
     * Value only applies to local entities.
     *
     * At the moment the value is only used for signatures on metadata.
     *
     * Typical values are:
     * https://www.w3.org/2001/04/xmlenc#sha1
     * https://www.w3.org/2001/04/xmlenc#sha256
     * https://www.w3.org/2001/04/xmlenc#sha384
     * https://www.w3.org/2001/04/xmlenc#sha512
     * https://www.w3.org/2001/04/xmlenc#ripemd160
     *
     * @param digestMethodAlgorithm The new digest method algorithm to use
     * @see org.opensaml.xml.signature.SignatureConstants
     */
    public void setDigestMethodAlgorithm(String digestMethodAlgorithm)
    {
        this.digestMethodAlgorithm = digestMethodAlgorithm;
    }
}
