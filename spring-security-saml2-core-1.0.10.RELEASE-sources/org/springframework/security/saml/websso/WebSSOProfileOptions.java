/* Copyright 2009 Vladimir Schafer
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
package org.springframework.security.saml.websso;

import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**JavaBean包含允许定制发送到IDP的SAML请求消息的属性。
 * JavaBean contains properties allowing customization of SAML request message sent to the IDP.
 *
 * @author Vladimir Schafer
 */
public class WebSSOProfileOptions implements Serializable, Cloneable {
//说明：可以影响到AuthnRequest。似乎沒啥用
    private String binding;
    private Set<String> allowedIDPs;
    private String providerName;
    private Integer assertionConsumerIndex;

    // Name ID policy
    private String nameID;
    private Boolean allowCreate;
    private Boolean passive = false;
    private Boolean forceAuthn = false;
    private Boolean includeScoping = true;
    private Integer proxyCount = 2;

    private String relayState;
    private Collection<String> authnContexts;
    private AuthnContextComparisonTypeEnumeration authnContextComparison = AuthnContextComparisonTypeEnumeration.EXACT;

    private Set<String> requesterIds;

    public WebSSOProfileOptions() {
    }

    public WebSSOProfileOptions(String binding) {
        this.binding = binding;
    }

    public String getBinding() {
        return binding;
    }

    /**
     * Sets binding to be used for for sending SAML message to IDP.
     *
     * @param binding binding value
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_POST_BINDING_URI
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_REDIRECT_BINDING_URI
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_PAOS_BINDING_URI
     * @see org.springframework.security.saml.SAMLConstants#SAML2_HOK_WEBSSO_PROFILE_URI
     */
    public void setBinding(String binding) {
        this.binding = binding;
    }

    /**设置在身份验证过程中，IdP是否应避免与用户进行交互。 布尔值将被编组为“ true”或“ false”，如果为null，则将从请求中省略该值。
     * Sets whether the IdP should refrain from interacting with the user during the authentication process. Boolean
     * values will be marshalled to either "true" or "false".
     *如果允许被动身份验证，则为true；否则为false，否则为null以忽略该字段
     * @return true if passive authentication is allowed, false otherwise, null will omit the passive parameter from request
     */
    public Boolean getPassive() {
        return passive;
    }

    /**
     * Sets whether the IdP should refrain from interacting with the user during the authentication process. Boolean
     * values will be marshalled to either "true" or "false", value will be omitted from request when null..
     *
     * @param passive true if passive authentication is allowed, false otherwise, null to omit the field
     */
    public void setPassive(Boolean passive) {
        this.passive = passive;
    }

    public Boolean getForceAuthN() {
        return forceAuthn;
    }

    public void setForceAuthN(Boolean forceAuthN) {
        this.forceAuthn = forceAuthN;
    }

    /**
     * True if scoping element should be included in the requests sent to IDP.
     *
     * @return true if scoping should be included, scoping won't be included when null or false
     */
    public Boolean isIncludeScoping() {
        return includeScoping;
    }

    public void setIncludeScoping(Boolean includeScoping) {
        this.includeScoping = includeScoping;
    }

    /**
     * @return null to skip proxyCount, 0 to disable proxying, &gt;0 to allow proxying
     */
    public Integer getProxyCount() {
        return proxyCount;
    }

    /**确定要在AuthnRequest中作用域的proxyCount属性中使用的值。 如果value为null，则proxyCount属性将被省略。 使用0禁用代理，或者使用值> 0指定允许的跃点数。
     * Determines value to be used in the proxyCount attribute of the scope in the AuthnRequest. In case value is null
     * the proxyCount attribute is omitted. Use zero to disable proxying or value &gt;0 to specify how many hops are allowed.
     * <p>
     * Property includeScoping must be enabled for this value to take any effect.
     * </p>
     *
     * @param proxyCount null to skip proxyCount in the AuthnRequest, 0 to disable proxying, &gt;0 to allow proxying
     */
    public void setProxyCount(Integer proxyCount) {
        this.proxyCount = proxyCount;
    }

    public Collection<String> getAuthnContexts() {
        return authnContexts;
    }

    public void setAuthnContexts(Collection<String> authnContexts) {
        this.authnContexts = authnContexts;
    }

    /**
     * Clones the current object.
     *
     * @return clone
     */
    @Override
    public WebSSOProfileOptions clone() {
        try {
            return (WebSSOProfileOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Invalid cloning support", e);
        }
    }

    /**
     * NameID to used or null to omit NameIDPolicy from request.
     *
     * @return name ID
     */
    public String getNameID() {
        return nameID;
    }

    /**设置时，确定将作为发送到IDP的AuthnRequest的一部分请求哪个NameIDPolicy。
     * When set determines which NameIDPolicy will be requested as part of the AuthnRequest sent to the IDP.
     *
     * @see org.opensaml.saml2.core.NameIDType#EMAIL
     * @see org.opensaml.saml2.core.NameIDType#TRANSIENT
     * @see org.opensaml.saml2.core.NameIDType#PERSISTENT
     * @see org.opensaml.saml2.core.NameIDType#X509_SUBJECT
     * @see org.opensaml.saml2.core.NameIDType#KERBEROS
     * @see org.opensaml.saml2.core.NameIDType#UNSPECIFIED
     *
     * @param nameID name ID
     */
    public void setNameID(String nameID) {
        this.nameID = nameID;
    }

    public Boolean isAllowCreate() {
        return allowCreate;
    }

    /**指示IDP是否可以基于当前身份验证请求创建新用户的标志。 空值将忽略请求中的字段。
     * Flag indicating whether IDP can create new user based on the current authentication request. Null value will
     * omit field from the request.
     *
     * @param allowCreate allow create
     */
    public void setAllowCreate(Boolean allowCreate) {
        this.allowCreate = allowCreate;
    }

    /**
     * @return comparison mode to use by default mode minimum is used
     */
    public AuthnContextComparisonTypeEnumeration getAuthnContextComparison() {
        return authnContextComparison;
    }

    /**
     * Sets comparison to use for WebSSO requests. No change for null values.
     *
     * @param authnContextComparison context to set
     */
    public void setAuthnContextComparison(AuthnContextComparisonTypeEnumeration authnContextComparison) {
        if (authnContextComparison != null) {
            this.authnContextComparison = authnContextComparison;
        }
    }

    public Set<String> getAllowedIDPs() {
        return allowedIDPs;
    }

    /**允许处理创建的AuthnRequest的IDP列表。 请求将被发送到的IDP会自动添加。 如果值为null，则allowedIDP将不包括在Scoping元素中。必须启用属性includeScoping才能使此值生效。
     * List of IDPs which are allowed to process the created AuthnRequest. IDP the request will be sent to is added
     * automatically. In case value is null the allowedIDPs will not be included in the Scoping element.
     * <p>启用的IDP可以处理创建的authnRequest，如果为null，则跳过scoptin中的属性
     * Property includeScoping must be enabled for this value to take any effect.
     * </p>
     *
     * @param allowedIDPs IDPs enabled to process the created authnRequest, null to skip the attribute from scoptin
     */
    public void setAllowedIDPs(Set<String> allowedIDPs) {
        this.allowedIDPs = allowedIDPs;
    }

    /**
     * Human readable name of the local entity.
     *
     * @return entity name
     */
    public String getProviderName() {
        return providerName;
    }

    /**设置ECP配置文件中使用的本地实体的可读名称。
     * Sets human readable name of the local entity used in ECP profile.
     *
     * @param providerName provider name
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Integer getAssertionConsumerIndex() {
        return assertionConsumerIndex;
    }

    /**设置后，确定assertionConsumerService以及IDP应该发送响应的绑定。 默认情况下，服务是自动确定的。 可以在该服务提供商的元数据中找到可用的索引。
     * When set determines assertionConsumerService and binding to which should IDP send response. By default
     * service is determined automatically. Available indexes can be found in metadata of this service provider.
     *
     * @param assertionConsumerIndex index
     */
    public void setAssertionConsumerIndex(Integer assertionConsumerIndex) {
        this.assertionConsumerIndex = assertionConsumerIndex;
    }

    public String getRelayState() {
        return relayState;
    }

    /**中继状态作为身份验证请求的一部分发送到IDP。 身份验证成功后，IDP将返回该值并将其提供给SAMLCredential。
     * Relay state sent to the IDP as part of the authentication request. Value will be returned by IDP and made available
     * in the SAMLCredential after successful authentication.
     *
     * @param relayState relay state
     */
    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    public Set<String> getRequesterIds() {
        return requesterIds;
    }

    /**标识请求者代表其进行操作的一组请求实体。 用于在发生代理时与请求者链进行通信。必须启用属性includeScoping才能使此值生效。
     * Identifies the set of requesting entities on whose behalf the requester is acting. Used to communicate
     * the chain of requesters when proxying occurs.
     * <p>
     * Property includeScoping must be enabled for this value to take any effect.
     * </p>
     *
     * @param requesterIds the names of the requester请求者的名称
     */
    public void setRequesterIds(Set<String> requesterIds) {
        this.requesterIds = requesterIds;
    }
}
